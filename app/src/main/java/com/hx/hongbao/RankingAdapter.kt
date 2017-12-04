package com.hx.hongbao

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

/**
 * Created by yhz on 2017/11/22.
 */
class RankingAdapter(private val context: Context) : BaseAdapter() {
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        // TODO
        View.inflate(context, R.layout.item_ranking, null)
        return View.inflate(context, R.layout.item_ranking, null)
    }

    override fun getItem(p0: Int): Any {
        // TODO
        return 0
    }

    override fun getItemId(p0: Int): Long {
        // TODO
        return 0
    }

    override fun getCount(): Int {
        // TODO
        return 0
    }
}