package com.blueshark.lightcast.ui.episodeoverview

import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View


abstract class TouchableSpan(
    private val mNormalTextColor: Int,
    private val mBackgroundColor: Int,
    private val mPressedTextColor: Int,
    private val mPressedBackgroundColor: Int
) : ClickableSpan() {
    private var mIsPressed = false
    fun setPressed(isSelected: Boolean) {
        mIsPressed = isSelected
    }
    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.color = if (mIsPressed) mPressedTextColor else mNormalTextColor
        ds.bgColor = if (mIsPressed) mPressedBackgroundColor else mBackgroundColor
        ds.isUnderlineText = false
    }
}