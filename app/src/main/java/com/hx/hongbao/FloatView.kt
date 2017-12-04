package com.hx.hongbao

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.view.WindowManager
import kotlinx.android.synthetic.main.float_layout.view.*


/**
 * 悬浮窗
 */
class FloatView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
        LinearLayout(context, attrs, defStyleAttr, defStyleRes), View.OnClickListener {

    private val wm = context.applicationContext.getSystemService("window") as WindowManager
    private val wmParams = MyApplication.app.getwmParams()
    private var clickX = 0f
    private var clickY = 0f
    private val TAG = "FloatView"
    private var listener: OnCmdListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.float_layout, this)
        float_id.setOnClickListener(this)
        zhuang_score.setOnClickListener(this)
        all_score.setOnClickListener(this)
        up_score.setOnClickListener(this)
        down_zhuang.setOnClickListener(this)
        start_qiang.setOnClickListener(this)
        stop_qiang.setOnClickListener(this)
        start_add.setOnClickListener(this)
        stop_add.setOnClickListener(this)
        calculate.setOnClickListener(this)
        send_result.setOnClickListener(this)
        set_rate.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0) {
            float_id -> {
                if (layout.visibility == View.VISIBLE) {
                    layout.visibility = View.GONE
                } else {
                    layout.visibility = View.VISIBLE
                }
            }
            all_score -> listener?.排行榜()
            up_score -> listener?.上分()
            down_zhuang -> listener?.下庄()
            start_qiang -> listener?.开始抢庄()
            stop_qiang -> listener?.停止抢庄()
            start_add -> listener?.开始下注()
            stop_add -> listener?.停止下注()
            calculate -> listener?.计算分数()
            send_result -> listener?.发送结果()
            set_rate -> listener?.设置抽水()
            zhuang_score -> listener?.设置庄底()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                //getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
                val dx = (event.rawX - clickX).toInt()
                //减25为状态栏的高度
                val dy = (event.rawY - clickY - 50).toInt()
                wmParams.x = dx
                wmParams.y = dy
                //刷新
                wm.updateViewLayout(this, wmParams)
            }
            MotionEvent.ACTION_DOWN -> {
                clickX = event.x
                clickY = event.y
            }
            MotionEvent.ACTION_UP -> {
                clickX = 0f
                clickY = 0f
            }
            else -> {
            }
        }
        return false
    }

    fun setOnCmdListener(listener: OnCmdListener) {
        this.listener = listener
    }
}