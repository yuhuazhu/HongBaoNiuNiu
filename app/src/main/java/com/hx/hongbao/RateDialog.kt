package com.hx.hongbao

import android.app.DialogFragment
import android.app.FragmentManager
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView

/**
 * Created by yhz on 2017/9/26.
 */
class RateDialog : DialogFragment {

    var listener: Callback? = null
    constructor()

    constructor(listener: Callback) {
        this.listener = listener
    }

    var targetHead: ImageView? = null
    var targetName: TextView? = null
    var contentTv: TextView? = null
    var contentIv: ImageView? = null
    var cancel: TextView? = null
    var send: TextView? = null
    var type: Type = Type.IMAGE
    var bitmap: Bitmap? = null
    var title: String? = null
    var name: String? = null
    var head: String? = null

    interface Callback {
        fun onWinRate()
        fun onAllRate()
    }

    enum class Type {
        IMAGE,
        WEB
    }
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = inflater?.inflate(R.layout.dialog_rate, container)
//        targetHead = view?.findViewById(R.id.target_head) as ImageView
//        targetName = view.findViewById(R.id.target_name) as TextView
//        contentTv = view.findViewById(R.id.content_tv) as TextView
//        contentIv = view.findViewById(R.id.content_iv) as ImageView
//        send = view.findViewById(R.id.send) as TextView
//        cancel = view.findViewById(R.id.cancel) as TextView
//        if (type == Type.IMAGE) {
//            contentTv?.visibility = View.GONE
//            contentIv?.setImageBitmap(bitmap)
//            send?.setOnClickListener {
//                dismiss()
//                listener?.onWinRate()
//            }
//        } else {
//            contentIv?.visibility = View.GONE
//            contentTv?.text = "[链接]$title"
//            send?.setOnClickListener {
//                dismiss()
//                listener?.onSendWeb()
//            }
//        }
//        if (!TextUtils.isEmpty(head)) {
//            Glide.with(activity).load(head).apply(RequestOptions().override(80, 80)).into(targetHead)
//        }
//        targetName?.text = name
//        cancel?.setOnClickListener { dismiss() }
        return view
    }

    fun showImage(name: String, head: String, type: Type, bitmap: Bitmap, manager: FragmentManager?, tag: String?) {
        this.name = name
        this.head = head
        this.type = type
        this.bitmap = bitmap
        show(manager, tag)
    }
}