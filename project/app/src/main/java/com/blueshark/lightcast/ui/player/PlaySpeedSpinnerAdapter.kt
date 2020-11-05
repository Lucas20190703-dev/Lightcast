package com.blueshark.lightcast.ui.player

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.blueshark.lightcast.R


class PlaySpeedSpinnerAdapter(
    private var context: Context,
    private var fruit: Array<String>
) : BaseAdapter() {

    override fun getCount(): Int {
        return fruit.size
    }

    override fun getItem(i: Int): Any? {
        return null
    }

    override fun getItemId(i: Int): Long {
        return 0
    }

    override fun getView(i: Int, view: View, viewGroup: ViewGroup?): View {
        val v = LayoutInflater.from(context).inflate(R.layout.cell_play_speed_spinner, null)
        val names = v.findViewById(R.id.text1) as TextView
        names.text = fruit[i]
        return v
    }
}