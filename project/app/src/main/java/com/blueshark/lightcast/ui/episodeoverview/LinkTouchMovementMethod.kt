package com.blueshark.lightcast.ui.episodeoverview

import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.MotionEvent
import android.widget.TextView


class LinkTouchMovementMethod : LinkMovementMethod() {
    private var mPressedSpan: TouchableSpan? = null
    private var mPrevPressedSpan : TouchableSpan? = null

    private var mPrevSpannable : Spannable? = null

    override fun onTouchEvent(
        textView: TextView,
        spannable: Spannable,
        event: MotionEvent
    ): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (mPrevSpannable != null && mPrevPressedSpan != null) {
                mPrevPressedSpan!!.setPressed(false)
                mPrevPressedSpan = null
                Selection.removeSelection(mPrevSpannable)
            }
            mPressedSpan = getPressedSpan(textView, spannable, event)
            if (mPressedSpan != null) {
                mPressedSpan!!.setPressed(true)
                Selection.setSelection(
                    spannable, spannable.getSpanStart(mPressedSpan),
                    spannable.getSpanEnd(mPressedSpan)
                )
                mPrevPressedSpan = mPressedSpan
                mPrevSpannable = spannable
            }

        } else if (event.action == MotionEvent.ACTION_UP) {
            super.onTouchEvent(textView, spannable, event)
        }
        else if (event.action == MotionEvent.ACTION_MOVE) {
//            val touchedSpan: TouchableSpan? = getPressedSpan(textView, spannable, event)
//            if (mPressedSpan != null && touchedSpan !== mPressedSpan) {
//                mPressedSpan!!.setPressed(false)
//                mPressedSpan = null
//                Selection.removeSelection(spannable)
//            }
            super.onTouchEvent(textView, spannable, event)
        } else {
            //if (mPressedSpan != null) {
                //mPressedSpan!!.setPressed(false)
                super.onTouchEvent(textView, spannable, event)
            //}
            //mPressedSpan = null
            //Selection.removeSelection(spannable)
        }
        return true
    }

    private fun getPressedSpan(
        textView: TextView,
        spannable: Spannable,
        event: MotionEvent
    ): TouchableSpan? {
        var x = event.x.toInt()
        var y = event.y.toInt()
        x -= textView.totalPaddingLeft
        y -= textView.totalPaddingTop
        x += textView.scrollX
        y += textView.scrollY
        val layout = textView.layout
        val line = layout.getLineForVertical(y)
        val off = layout.getOffsetForHorizontal(line, x.toFloat())
        val link: Array<TouchableSpan> =
            spannable.getSpans(off, off, TouchableSpan::class.java)
        var touchedSpan: TouchableSpan? = null
        if (link.isNotEmpty()) {
            touchedSpan = link[0]
        }
        return touchedSpan
    }
}