package com.blueshark.lightcast.ui.search

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.AccelerateInterpolator
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.blueshark.lightcast.R
import com.blueshark.lightcast.utils.SearchViewFormatter
import kotlin.math.max


class SearchActivity : AppCompatActivity() {

    private lateinit var rootLayout : View
    private lateinit var searchToolbar : Toolbar
    private lateinit var searchView : SearchView

    private var revealX = 0
    private var revealY = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        rootLayout = findViewById(R.id.root_layout)
        searchToolbar = findViewById(R.id.searchToolbar)

        if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_X) &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_Y)) {

            rootLayout.visibility = View.INVISIBLE

            revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0)
            revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0)

            val viewTreeObserver = rootLayout.viewTreeObserver
            if (viewTreeObserver.isAlive) {
                viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        revealActivity(revealX, revealY)
                        rootLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
            }
        } else {
            rootLayout.visibility = View.VISIBLE;
        }

        searchToolbar.findViewById<ImageButton>(R.id.homeButton).setOnClickListener {
            unRevealActivity();
        }

        searchView = searchToolbar.findViewById(R.id.searchView)
        SearchViewFormatter()
            .setSearchBackGroundResource(R.drawable.search_view_background)
            .setSearchIconResource(R.drawable.ic_search_gray_24dp, inside = true, outside = false)
            .setSearchTextColorResource(R.color.color_text_dark)
            .setSearchHintColorResource(R.color.colorPrimary)
            .setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE)
            .setTextViewPadding(40, 0, 0, 0 )
            .setSearchCloseIconResource(R.drawable.ic_search_clear_24dp)
            .format(searchView)

        searchView.queryHint = getString(R.string.search)
        searchView.setIconifiedByDefault(false)  //focus search view


    }

    private fun revealActivity(x: Int, y: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val finalRadius = (max(
                rootLayout.width,
                rootLayout.height
            ) * 1.1).toFloat()

            // create the animator for this view (the start radius is zero)
            val circularReveal: Animator =
                ViewAnimationUtils.createCircularReveal(rootLayout, x, y, 0f, finalRadius)
            circularReveal.duration = 400
            circularReveal.interpolator = AccelerateInterpolator()

            // make the view visible and start the animation
            rootLayout.visibility = View.VISIBLE
            circularReveal.start()
        } else {
            finish()
        }
    }

    private fun unRevealActivity() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            finish()
        } else {
            val finalRadius = (max(
                rootLayout.width,
                rootLayout.height
            ) * 1.1).toFloat()
            val circularReveal =
                ViewAnimationUtils.createCircularReveal(
                    rootLayout, revealX, revealY, finalRadius, 0f
                )
            circularReveal.duration = 400
            circularReveal.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    rootLayout.visibility = View.INVISIBLE
                    finish()
                }
            })
            circularReveal.start()
        }
    }

    companion object{
        const val EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X"
        const val EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y"
    }
}