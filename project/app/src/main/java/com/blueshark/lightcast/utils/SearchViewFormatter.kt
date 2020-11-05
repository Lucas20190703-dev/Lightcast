package com.blueshark.lightcast.utils

import android.content.res.Resources
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener


class SearchViewFormatter {
    private var mSearchBackGroundResource = 0
    private var mSearchIconResource = 0
    private var mSearchIconInside = false
    private var mSearchIconOutside = false
    private var mSearchVoiceIconResource = 0
    private var mSearchTextColorResource = 0
    private var mSearchHintColorResource = 0
    private var mSearchHintText = ""
    private var mInputType = Int.MIN_VALUE
    private var mSearchCloseIconResource = 0
    private var mEditorActionListener: OnEditorActionListener? = null
    private var mResources: Resources? = null
    private var mSearchView : SearchView? = null

    private var mSearchTextViewPaddingStart = 0
    private var mSearchTextViewPaddingTop = 0
    private var mSearchTextViewPaddingEnd = 0
    private var mSearchTextViewPaddingBottom = 0

    fun setSearchBackGroundResource(searchBackGroundResource: Int): SearchViewFormatter {
        mSearchBackGroundResource = searchBackGroundResource
        return this
    }

    fun setSearchIconResource(
        searchIconResource: Int,
        inside: Boolean,
        outside: Boolean
    ): SearchViewFormatter {
        mSearchIconResource = searchIconResource
        mSearchIconInside = inside
        mSearchIconOutside = outside
        return this
    }

    fun setSearchVoiceIconResource(searchVoiceIconResource: Int): SearchViewFormatter {
        mSearchVoiceIconResource = searchVoiceIconResource
        return this
    }

    fun setSearchTextColorResource(searchTextColorResource: Int): SearchViewFormatter {
        mSearchTextColorResource = searchTextColorResource
        return this
    }

    fun setSearchHintColorResource(searchHintColorResource: Int): SearchViewFormatter {
        mSearchHintColorResource = searchHintColorResource
        return this
    }

    fun setSearchHintText(searchHintText: String): SearchViewFormatter {
        mSearchHintText = searchHintText
        return this
    }

    fun setInputType(inputType: Int): SearchViewFormatter {
        mInputType = inputType
        return this
    }

    fun setSearchCloseIconResource(searchCloseIconResource: Int): SearchViewFormatter {
        mSearchCloseIconResource = searchCloseIconResource
        return this
    }

    fun setEditorActionListener(editorActionListener: OnEditorActionListener?): SearchViewFormatter {
        mEditorActionListener = editorActionListener
        return this
    }

    fun setTextViewPadding(start: Int, top: Int, end: Int, bottom: Int) : SearchViewFormatter {
        mSearchTextViewPaddingStart = start
        mSearchTextViewPaddingTop = top
        mSearchTextViewPaddingEnd = end
        mSearchTextViewPaddingBottom = bottom
        return this
    }

    fun format(searchView: SearchView?) {
        if (searchView == null) {
            return
        }
        mSearchView = searchView
        mResources = searchView.context.resources
        var id: Int
        if (mSearchBackGroundResource != 0) {
            //id = getIdentifier("search_plate")
            var view = searchView.findViewById<View>(androidx.appcompat.R.id.search_plate)
            view.setBackgroundResource(mSearchBackGroundResource)
            //id = getIdentifier("submit_area")
            view = searchView.findViewById(androidx.appcompat.R.id.submit_area)
            view.setBackgroundResource(mSearchBackGroundResource)
        }
        if (mSearchVoiceIconResource != 0) {
            //id = getIdentifier("search_voice_btn")
            val view =
                searchView.findViewById<View>(androidx.appcompat.R.id.search_voice_btn) as ImageView
            view.setImageResource(mSearchVoiceIconResource)
        }
        if (mSearchCloseIconResource != 0) {
            //id = getIdentifier("search_close_btn")
            val view =
                searchView.findViewById<View>(androidx.appcompat.R.id.search_close_btn) as ImageView
            view.setImageResource(mSearchCloseIconResource)
        }
        //id = getIdentifier("search_src_text")
        val view = searchView.findViewById<View>(androidx.appcompat.R.id.search_src_text) as TextView
        if (mSearchTextColorResource != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                view.setTextColor(searchView.context.getColor(mSearchTextColorResource))
            } else {
                view.setTextColor(mResources!!.getColor(mSearchTextColorResource))
            }

        }

        if (mSearchTextViewPaddingStart != 0 || mSearchTextViewPaddingTop != 0 ||
                mSearchTextViewPaddingEnd != 0 || mSearchTextViewPaddingBottom != 0) {
            view.setPadding(mSearchTextViewPaddingStart, mSearchTextViewPaddingTop,
                mSearchTextViewPaddingEnd, mSearchTextViewPaddingBottom)
        }

        if (mSearchHintColorResource != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                view.setHintTextColor(searchView.context.getColor(mSearchHintColorResource))
            } else {
                view.setHintTextColor(mResources!!.getColor(mSearchHintColorResource))
            }
        }
        if (mInputType > Int.MIN_VALUE) {
            view.inputType = mInputType
        }
        if (mSearchIconResource != 0) {
            var imageView =
                searchView.findViewById<View>(androidx.appcompat.R.id.search_mag_icon) as ImageView  // getIdentifier("search_mag_icon")
            if (mSearchIconInside) {
                val searchIconDrawable = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                        searchView.context.getDrawable(mSearchIconResource)!!
                    else ->
                        mResources!!.getDrawable(mSearchIconResource)!!
                }
                val size = (view.textSize * 1.25f).toInt()
                searchIconDrawable.setBounds(0, 0, size, size)
                val hintBuilder = SpannableStringBuilder("   ")
                hintBuilder.append(mSearchHintText) // we can't append view.getHint() because that includes the old icon
                hintBuilder.setSpan(
                    ImageSpan(searchIconDrawable),
                    1,
                    2,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                view.hint = hintBuilder
                imageView.layoutParams = LinearLayout.LayoutParams(0, 0)
            }
            if (mSearchIconOutside) {
                //val searchImgId = getIdentifier("search_button")
                imageView =
                    searchView.findViewById<View>(androidx.appcompat.R.id.search_button) as ImageView
                imageView.setImageResource(mSearchIconResource)
            }
        }
        if (mEditorActionListener != null) {
            view.setOnEditorActionListener(mEditorActionListener)
        }
    }

    private fun getIdentifier(literalId: String?): Int {
        return mSearchView?.context?.resources?.getIdentifier(String.format("android:id/%s", literalId), null, null)!!

//        return mResources!!.getIdentifier(
//            String.format("android:id/%s", literalId),
//            null,
//            null
//        )
    }
}