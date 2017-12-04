package com.hx.hongbao

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils
import com.hx.hongbao.gen.UserNoteDao
import com.hx.hongbao.table.UserNote
import org.jetbrains.anko.toast

object Utils {

    fun getUser(name: String, dao: UserNoteDao?): UserNote? =
            dao?.queryBuilder()?.where(UserNoteDao.Properties.Name.eq(name))?.unique()

    fun getUserScore(name: String, dao: UserNoteDao?): Int {
        val note = dao?.queryBuilder()?.where(UserNoteDao.Properties.Name.eq(name))?.unique()
        return note?.score ?: 0
    }

    fun getUserList(dao: UserNoteDao?): MutableList<UserNote> {
        val userList = dao?.queryBuilder()?.orderDesc(UserNoteDao.Properties.Score)?.list()
        return userList ?: mutableListOf()
    }

    fun updateUser(note: UserNote, dao: UserNoteDao?) {
        val oldNote = getUser(note.name, dao)
        if (oldNote == null) {
            dao?.insert(note)
        } else {
            dao?.update(note)
        }
    }

    fun updateUserScore(name: String, score: Int, dao: UserNoteDao?) {
        val user = getUser(name, dao)
        user?.let {
            it.score += score
            dao?.update(it)
        }
    }

    /**
     * 复制文本到粘贴板
     */
     fun copyText(context: Context, text: String) {
        if (TextUtils.isEmpty(text)) return
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.primaryClip = ClipData.newPlainText("", text)
        context.toast("已复制到粘贴板")
    }
}