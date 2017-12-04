package com.hx.hongbao

import android.accessibilityservice.AccessibilityService
import android.app.Application
import android.app.Dialog
import android.content.*
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

import com.hx.hongbao.gen.UserNoteDao
import com.hx.hongbao.table.UserNote
import org.jetbrains.anko.toast
import android.os.Bundle
import android.os.IBinder
import android.view.Window
import android.widget.*
import com.hailian.qmyg.IMessage
import kotlin.properties.Delegates


class HongBaoService : AccessibilityService(), View.OnClickListener, OnCmdListener {

    lateinit var wmParams: WindowManager.LayoutParams
    //创建浮动窗口设置布局参数的对象
    lateinit var mWindowManager: WindowManager
    var clickX = 0f
    var clickY = 0f

    val 豹子 = 15
    val 满牛 = 14
    val 顺子 = 13
    val 对子 = 12
    val 金牛 = 11

    // 红包详情列表
    private val hongBaoDetailItem = "com.hailian.qmyg:id/lv_record"
    // 红包详情列表
    private val hongBaoDetailName = "com.hailian.qmyg:id/item_luck_name"
    // 红包详情列表
    private val hongBaoDetailMoney = "com.hailian.qmyg:id/item_luck_money"
    // 红包详情列表
    private val hongBaoDetailTime = "com.hailian.qmyg:id/item_luck_time"
    // 聊天页面的标题
    private val TITLE = "com.hailian.qmyg:id/title"
    // 输入文字的区域
    private val INPUT = "com.hailian.qmyg:id/input"
    // 发送的imagebutton
    private val SENDBTN = "com.hailian.qmyg:id/btn_send"
    // 用户详情的名字
    private val USERNAME = "com.hailian.qmyg:id/remark"
    // 聊天的listview
    private val LIST = "com.hailian.qmyg:id/chat_list"
    // 昵称和消息
    private val NAME = "com.hailian.qmyg:id/sender"
    // 昵称和消息
    private val MESSAGE = "com.hailian.qmyg:id/chat_tv"
    // 群名字
    private var groupName = ""
    // 庄
    private var master = ""
    private var 庄底 = 0
    private var 抢庄的最小底分 = 100
    private var 加庄最小分数 = 10
    private var 庄赢抽水百分比 = 0.1f
    private var 下庄抽水百分比 = 0.1f
    // 下注的map
    private val betMap = LinkedHashMap<String, Int>()
    // 结果的map
    private val resultMap = LinkedHashMap<String, ResultInfo>()
    // 结果
    private var result = ""
    // 旧的时间
    private var oldTime = 0L
    // 当前时间
    private var currentTime = 0L

    // 1 抢庄
    // 2 下注
    private var type = 0
    private val 空闲 = 0
    private val 抢庄中 = 1
    private val 已有庄 = 2
    private val 下注中 = 3
    private val 已下注 = 4

    private var dao: UserNoteDao? = null
    private var fv by Delegates.notNull<FloatView>()
    private var message: IMessage? = null
    private val conn = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            // TODO
            message = null
        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            // TODO
            Log.e("HongBaoService", "onServiceConnected")
            message = IMessage.Stub.asInterface(p1)
        }

    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val root = rootInActiveWindow ?: return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            parseText(root)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.e("onServiceConnected", "onServiceConnected")
        createFloatView()
        val service = Intent("com.hailian.qmyg.service.MessageService")
        service.`package` = "com.hailian.qmyg"
        bindService(service, conn, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(conn)
        mWindowManager.removeView(fv)
    }

    override fun onInterrupt() {

    }

    private fun createFloatView() {
        fv = FloatView(this)
        fv.setOnCmdListener(this)
        wmParams = MyApplication.app.getwmParams()
        //获取的是WindowManagerImpl.CompatModeWrapper
        mWindowManager = application.getSystemService(Application.WINDOW_SERVICE) as WindowManager
        //设置window type
        wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        //设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.START or Gravity.TOP
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        wmParams.x = 0
        wmParams.y = 0
        //设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        mWindowManager.addView(fv, wmParams)
        dao = GreenDaoManager.instance?.session?.userNoteDao
    }

    override fun onClick(p0: View?) {
        // TODO
    }

    override fun 排行榜() {
        showRankingDialog()
    }

    override fun 上分() {
        showAddScoreDialog()
    }

    override fun 下分() {
    }

    override fun 下庄() {
        if (type != 已有庄) {
            toast("请先开始抢庄！")
            return
        }
        val user = Utils.getUser(master, dao)
        val score = user?.score ?: 0
        val 下庄抽水 = (庄底 * 下庄抽水百分比).toInt()
        val allScore = score + 庄底 - 下庄抽水
        Utils.updateUserScore(master, 庄底 - 下庄抽水, dao)
        Log.e("parseText", "下庄")
        var temp = "$master 下庄成功\n"
        temp += "当前庄底：$庄底 \n"
        temp += "下庄抽水：$下庄抽水 \n"
        temp += "玩家积分：$allScore"
        inputAndSend(temp)
        庄底 = 0
        master = ""
        type = 空闲
    }

    override fun 开始抢庄() {
        if (type > 抢庄中) {
            toast("已经有庄了，请先下庄后再抢庄！")
            return
        }
        inputAndSend("开始抢庄，${抢庄的最小底分}起标，${加庄最小分数}起加")
        oldTime = System.currentTimeMillis() / 1000
        Log.e("parseText", "开始抢庄")
        master = ""
        庄底 = 0
        type = 抢庄中
    }

    override fun 停止抢庄() {
        if (type != 抢庄中) {
            toast("请先开始抢庄！")
            return
        }
        currentTime = System.currentTimeMillis() / 1000
        val list = message?.getMessage(groupName, oldTime, currentTime)
        Log.e("停止抢庄", list?.toString() ?: "")
        list?.forEach {
            val speaker = it.split(",")
            parseQiangZhuang(speaker[0], speaker[1])
        }
        Utils.updateUserScore(master, -庄底, dao)
        val temp = "本局庄家为：$master  底分为：$庄底"
        inputAndSend(temp)
        type = 已有庄
    }

    override fun 开始下注() {
        if (type != 已有庄) {
            toast("请抢完庄后再开始下注！")
            return
        }
        inputAndSend("开始下注，10起下注")
        oldTime = System.currentTimeMillis() / 1000
//        Log.e("开始下注", "oldTime = " + oldTime)
        Log.e("parseText", "开始下注")
        betMap.clear()
        type = 下注中
    }

    override fun 停止下注() {
        if (type != 下注中) {
            toast("请开始下注后再停止！")
            return
        }
        currentTime = System.currentTimeMillis() / 1000
//        Log.e("停止下注", "currentTimeMillis = " + currentTime)
        val list = message?.getMessage(groupName, oldTime, currentTime)
        Log.e("停止下注", list?.toString() ?: "")
        list?.forEach {
            val speaker = it.split(",")
            if (master != speaker[0]) {
                parseXiaZhu(speaker[0], speaker[1])
            }
        }
        Log.e("betMap", betMap.toString())
        var sb = "下注玩家\n"
        var all = 0
        betMap.forEach { (key, value) ->
            all += value
            sb += key + "下注" + value + "\n"
        }
        sb += "共有${betMap.size}人下注，总下注数为：$all"
        sb += "请发${betMap.size + 1}个包"
        inputAndSend(sb)
        // 庄家不需要下注，所以分是0分，但是后面计算需要用到，所以加到map里
        betMap.put(master, 0)
        Log.e("下注结果", sb)
        type = 已下注
    }

    override fun 计算分数() {
        if (type != 已下注) {
            toast("请先下注！")
            return
        }
        getDetail()
    }

    override fun 发送结果() {
        inputAndSend(result)
        type = 已有庄
    }

    override fun 设置抽水() {
        // TODO
        showRateDialog()
    }

    override fun 设置庄底() {
        // TODO
        showSetZhuangScoreDialog()
    }

    fun showRankingDialog() {
        val dialog = Dialog(this)
        val view = View.inflate(this, R.layout.dialog_ranking, null)
        val listView = view.findViewById(R.id.list) as ListView
        val adapter = ArrayAdapter<String>(this, R.layout.item_ranking, R.id.tv)
        val list = Utils.getUserList(dao)
        list.forEach { it -> adapter.add(it.name + "    " + it.score) }
        listView.adapter = adapter
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(view)
        dialog.window.setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT))
        dialog.show()
    }

    fun showRateDialog() {
        val dialog = Dialog(this)
        val view = View.inflate(this, R.layout.dialog_rate, null)
        val et1 = view.findViewById(R.id.edit1) as EditText
        val et2 = view.findViewById(R.id.edit2) as EditText
        val btn = view.findViewById(R.id.btn) as Button
        btn.setOnClickListener {
            val a = et1.text.toString()
            val b = et2.text.toString()
            if (a.isBlank() || b.isBlank()) return@setOnClickListener
            if (a.toInt() in 0..100) {
                庄赢抽水百分比 = a.toInt() / 100f
            }
            if (b.toInt() in 0..100) {
                下庄抽水百分比 = b.toInt() / 100f
            }
            toast("庄赢抽水百分比=$庄赢抽水百分比, 下庄抽水百分比=$下庄抽水百分比")
            dialog.dismiss()
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(view)
        dialog.window.setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT))
        dialog.show()
    }

    fun showAddScoreDialog() {
        val root = rootInActiveWindow ?: return
        val node = root.findAccessibilityNodeInfosByViewId(USERNAME).firstOrNull() ?: return
        val name = node.text.toString()
        val dialog = Dialog(this)
        val view = View.inflate(this, R.layout.dialog_add_score, null)
        val tv = view.findViewById(R.id.player) as TextView
        val et = view.findViewById(R.id.edit) as EditText
        val btn = view.findViewById(R.id.btn) as Button
        val score = Utils.getUserScore(name, dao)
        tv.text = "上分玩家：$name  \n当前分数：$score"
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(view)
        dialog.window.setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT))
        dialog.show()
        btn.setOnClickListener {
            try {
                val tempScore = et.text.toString().toInt()
                if (tempScore != 0) {
                    Utils.updateUser(UserNote(name, score + tempScore), dao)
                    toast("上分成功，" + name + "当前分数：" + Utils.getUserScore(name, dao))
                    dialog.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun showSetZhuangScoreDialog() {
        val dialog = Dialog(this)
        val view = View.inflate(this, R.layout.dialog_set_zhuang_score, null)
        val et1 = view.findViewById(R.id.edit1) as EditText
        val et2 = view.findViewById(R.id.edit2) as EditText
        val btn = view.findViewById(R.id.btn) as Button
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(view)
        dialog.window.setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT))
        dialog.show()
        btn.setOnClickListener {
            val a = et1.text.toString()
            val b = et2.text.toString()
            if (a.isBlank() || b.isBlank()) return@setOnClickListener
            if (a.toInt() > 0 && b.toInt() > 0) {
                抢庄的最小底分 = a.toInt()
                加庄最小分数 = b.toInt()
                toast("抢庄的最小底分=$抢庄的最小底分, 加庄最小分数=$加庄最小分数")
                dialog.dismiss()
            } else {
                toast("请两个都设置大于零的数")
            }
        }
    }

    /**
     * 解析文本
     */
    private fun parseText(rootInActiveWindow: AccessibilityNodeInfo) {
//        val chatList = rootInActiveWindow.findAccessibilityNodeInfosByViewId(LIST) ?: listOf()
//        Log.e("parseText", "chatList.childCount = " + chatList[0].childCount)
        val titleList = rootInActiveWindow.findAccessibilityNodeInfosByViewId(TITLE) ?: listOf()
        if (titleList.isNotEmpty()) {
            groupName = titleList[0].text?.toString() ?: return
        }
        val nameList = rootInActiveWindow.findAccessibilityNodeInfosByViewId(NAME) ?: listOf()
//        if (titleList.isEmpty()) {
//            fv.visibility = View.GONE
//        } else {
//            fv.visibility = View.VISIBLE
//        }
        val messageList = rootInActiveWindow.findAccessibilityNodeInfosByViewId(MESSAGE) ?: listOf()
        if (messageList.isEmpty()) return
        val name = if (nameList.isEmpty()) "" else nameList[nameList.lastIndex].text?.toString() ?: ""
        val message = messageList[messageList.lastIndex].text.toString()
        val count = messageList[messageList.lastIndex].parent.parent.childCount
        if (count == 3) {
            when (type) {
                抢庄中 ->
                    //抢庄
                    try {
                        if (message.contains("开始抢庄")) return
                        val byteContent = message.toByteArray().filter {
                            // 数字 0～9
                            it in 48..57
                        }
                        val str = String(byteContent.toByteArray())
                        if (str.isBlank()) return
                        val f = str.toInt()
                        Log.e("name=$name", "score=$f")
                        if (f < 抢庄的最小底分) {
                            inputAndSend("$name 抢庄分数小于最小底分!")
                        }
                    } catch (e: Exception) {
                        Log.e("抢庄错误", e.toString())
                    }
                下注中 -> {
                    //下注
                    try {
                        val bet: Int
                        // TODO 没这个人会return
                        val score = Utils.getUserScore(name, dao)
                        if (message.contains("梭哈")) {
                            val byteContent = message.toByteArray().filter {
                                // 数字 0～9
                                it in 48..57
                            }
                            val str = String(byteContent.toByteArray())
                            if (str.isBlank()) return
                            bet = str.toInt()
                            if (bet < 抢庄的最小底分 || bet > score) {
                                if (bet < 100) {
                                    inputAndSend("$name 梭哈最少100起，请重新下注!")
                                } else {
                                    inputAndSend("$name 积分不够，请重新下注!")
                                }
                            }
                        } else {
                            val byteContent = message.toByteArray().filter {
                                // 数字 0～9
                                it in 48..57
                            }
                            val str = String(byteContent.toByteArray())
                            if (str.isBlank()) return
                            bet = str.toInt()
                            if (score < bet * 15) {
                                inputAndSend("$name 积分不足，下注失败，请联系群主!")
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("下注Exception", e.toString())
                    }
                }
            }
        }
    }

    private fun parseXiaZhu(name: String, message: String) {
        Log.e("下注", message)
        try {
            val bet: Int
            // TODO 没这个人会return
            val score = Utils.getUserScore(name, dao)
            if (message.contains("梭哈")) {
                val byteContent = message.toByteArray().filter {
                    // 数字 0～9
                    it in 48..57
                }
                val str = String(byteContent.toByteArray())
                if (str.isBlank()) return
                bet = str.toInt()
                if (bet in 100..score) {
                    betMap.put(name, bet)
                }
            } else {
                val byteContent = message.toByteArray().filter {
                    // 数字 0～9
                    it in 48..57
                }
                val str = String(byteContent.toByteArray())
                if (str.isBlank()) return
                bet = str.toInt()
                if (score >= bet * 15) {
                    betMap.put(name, bet)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("下注Exception", e.toString())
        }
    }

    private fun parseQiangZhuang(name: String, message: String) {
        try {
            if (message.contains("开始抢庄")) return
            val byteContent = message.toByteArray().filter {
                // 数字 0～9
                it in 48..57
            }
            val str = String(byteContent.toByteArray())
            if (str.isBlank()) return
            val f = str.toInt()
            if (f < 抢庄的最小底分) return
            if ((f - 庄底) < 加庄最小分数) return
            庄底 = f
            master = name
            Log.e("抢庄", "name=$master,庄底=$庄底")
        } catch (e: Exception) {
            Log.e("抢庄错误", e.toString())
        }
    }

    /**
     * 获取红包详情
     */
    private fun getDetail() {
        val rootView = rootInActiveWindow ?: return
        val node = rootView.findAccessibilityNodeInfosByViewId(hongBaoDetailItem).firstOrNull()
        if (node == null) {
            toast("请去红包详情页计算！")
            return
        }
        for (index in 0 until node.childCount) {
            val hongBaoInfo = ResultInfo()
            // child 0 昵称 child 1 抢红包时间  child 2 红包金额
            val name = node.getChild(index).getChild(0).text.toString()
            val time = node.getChild(index).getChild(1).text.toString()
            val price = node.getChild(index).getChild(2).text.toString()
            hongBaoInfo.name = name
            hongBaoInfo.time = time
            hongBaoInfo.money = price.replace("元", "")
            parseMoney(hongBaoInfo)
        }
        calculateScore()
        Log.e("resultMap.toString()", resultMap.toString())
    }

    private fun parseMoney(resultInfo: ResultInfo) {
        val odds: Int
        val array = resultInfo.money.split(".")
        val 小数点前 = array[0]
        val 小数点后 = array[1]
        val charArray = 小数点后.toCharArray()
        val 个 = 小数点前.last().toString().toInt()
        val 角 = charArray[0].toString().toInt()
        val 分: Int
        分 = if (charArray.size <= 1) {
            0
        } else {
            charArray[1].toString().toInt()
        }
        Log.e("计算", "个=$个,角=$角,分=$分")

        val 小数点后两位相等 = 角 == 分
        val 个位大于零 = 个 > 0
        val 点数 = 个 + 角 + 分
        val 普通牛 = if (点数 > 10) 点数 % 10 else 点数
        Log.e("普通牛", "普通牛=$普通牛")

        if (resultInfo.梭哈) {
            // 如果是梭哈直接比点数
            odds = 个 + 角 + 分
        } else {
            if (小数点后两位相等) {
                // 豹子 or 满牛 or 对子 or 普通牛
                odds = when {
                    0 == 角 -> 满牛
                    个 == 角 -> 豹子
                    else -> 对子
                }
            } else {
                // 顺子 or 金牛 or 普通牛
                odds = if (个位大于零) {
                    if (角 in 2..8 && 个 + 1 == 角 && 分 - 1 == 角) {
                        顺子
                    } else {
                        普通牛
                    }
                } else {
                    if (个 + 分 == 0) {
                        金牛
                    } else {
                        普通牛
                    }
                }
            }
        }
        Log.e("resultInfo.name", resultInfo.name)
        resultInfo.下注 = betMap[resultInfo.name] ?: return
        resultInfo.倍率 = odds
        resultMap.put(resultInfo.name, resultInfo)
        Log.e("resultInfo.toString()", resultInfo.toString())
    }

    /**
     * 计算分数
     */
    private fun calculateScore() {
        val winList = arrayListOf<ResultInfo>()
        val loserList = arrayListOf<ResultInfo>()
        var masterWCount = 0
        var masterWScore = 0
        var masterLCount = 0
        var masterLScore = 0
        result = ""
        result = "计算结果\n"
        val masterResult = resultMap[master] ?: return
        // 获得庄家赢多少家多少分，输多少家多少分和闲家赢的列表，输的列表
        for ((key, value) in resultMap) {
            if (key != master) {
                val win: Boolean = if (masterResult.倍率 == value.倍率) {
                    masterResult.getAllMoney() < value.getAllMoney()
                } else {
                    masterResult.倍率 < value.倍率
                }
                if (value.梭哈) {
                    value.输赢 = value.下注
                    if (win) {
                        masterLCount++
                        masterLScore += value.输赢
                        winList.add(value)
                    } else {
                        masterWCount++
                        masterWScore += value.输赢
                        loserList.add(value)
                    }
                } else {
                    if (win) {
                        masterLCount++
                        value.输赢 = value.下注 * value.倍率
                        masterLScore += value.输赢
                        winList.add(value)
                    } else {
                        masterWCount++
                        value.输赢 += value.下注 * masterResult.倍率
                        masterWScore += value.输赢
                        loserList.add(value)
                    }
                }
            }
        }
        Log.e("loserList", loserList.toString())
        Log.e("winList", winList.toString())
        // 获得庄家的总的输赢分数
        val score = masterWScore - masterLScore
        if (庄底 + score < 0) {
            // 有人喝水
            winList.sortByDescending { it.倍率 }
            loserList.sortByDescending { it.倍率 }
        } else {
            // 够赔 or 赢了

        }
        result += "━━━以下${loserList.size}位输了━━━" + "\n"
        for (loser in loserList) {
            result += "${loser.name}抢:${loser.money} 押:${loser.下注} ${loser.倍率}倍 输${loser.输赢}\n"
            val note = Utils.getUser(loser.name, dao) ?: return
            val oldScore = note.score
            val newScore = oldScore - loser.输赢
            note.score = newScore
            result += "上局$oldScore 本局$newScore\n"
            Utils.updateUser(note, dao)
        }
        var temp庄底 = 庄底
        result += "━━━以下${winList.size}位赢了━━━" + "\n"
        for (winer in winList) {
            // 玩家赢的分数
            val newScore: Int
            val note = Utils.getUser(winer.name, dao) ?: return
            // 数据库里没这个分数的话为0
            val oldScore = note.score
            // 当前庄底剩下的钱 - 玩家赢的钱 < 0 等于不够赔
            if (temp庄底 - winer.输赢 < 0) {
                newScore = if (temp庄底 == 0) {
                    // 喝水
                    0
                } else {
                    // 剩下的全赔里
                    temp庄底
                }
            } else {
                // 庄底够赔玩家
                temp庄底 -= winer.输赢
                newScore = winer.输赢
            }
            result += "${winer.name}抢:${winer.money} 押:${winer.下注} ${winer.倍率}倍 ${if (newScore == 0) "喝水$newScore" else "赢$newScore"}\n"
            result += "上局$oldScore 本局${oldScore + newScore}\n"
            note.score = oldScore + newScore
            Utils.updateUser(note, dao)
        }
        result += "━━━庄家榜━━━\n"
        result += "本局庄家: $master\n"
        result += "庄家抢包: ${masterResult.money}   倍率:${masterResult.倍率}\n"
        result += "庄输赢家: 输$masterLCount 家 赢$masterWCount 家\n"
        result += "上局庄钱$庄底\n"

        // TODO 抽水百分比
        val detail: String
        var 抽水情况 = 0
        detail = if (score > 0) {
            val a: String
            抽水情况 = (score * 庄赢抽水百分比).toInt()
            "赢$score"
        } else {
            "输${Math.abs(score)}"
        }
        result += "本局情况: $detail" + "\n"
        result += "本局抽水: $抽水情况" + "\n"
        庄底 += score - 抽水情况
        result += "庄钱剩余：$庄底\n"
        result += "━━━━━━" + "\n"
        Log.e("计算结果", result)
        toast("计算完毕，请返回发送结果")
    }

    /**
     * 自动发送
     */
    private fun inputAndSend(str: String) {
        val root = rootInActiveWindow ?: return
        val input = root.findAccessibilityNodeInfosByViewId(INPUT).firstOrNull() ?: return
        val arguments = Bundle()
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, str)
        input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        val btn = root.findAccessibilityNodeInfosByViewId(SENDBTN).firstOrNull() ?: return
        btn.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }
}
