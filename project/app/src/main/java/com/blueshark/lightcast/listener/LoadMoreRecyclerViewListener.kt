package com.blueshark.lightcast.listener

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


abstract class LoadMoreRecyclerViewListener(private val layoutManager: LinearLayoutManager,
                                            private val startingPageIndex: Int, private val visibleThreshold : Int = 8) : RecyclerView.OnScrollListener() {

    private var currentPage : Int
    private var previousTotalItemCount : Int
    private var loading : Boolean

    private var mLastFirstVisibleItem : Int

    init {
        currentPage = startingPageIndex
        previousTotalItemCount = 0
        mLastFirstVisibleItem = 0
        loading = true
    }

    fun initialize(){
        currentPage = startingPageIndex
        previousTotalItemCount = 0
        mLastFirstVisibleItem = 0
        loading = true
    }

    private fun getLastVisibleItem(lastVisibleItemPositions: IntArray): Int {
        var maxSize = 0
        for (i in lastVisibleItemPositions.indices) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i]
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i]
            }
        }
        return maxSize
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val totalItemCount = layoutManager.itemCount
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        val currentFirstVisibleItem = layoutManager.findFirstVisibleItemPosition()
        if (currentFirstVisibleItem > -1 && currentFirstVisibleItem > mLastFirstVisibleItem) {
            //onScrolling(Direction.DOWN)
            mLastFirstVisibleItem = currentFirstVisibleItem
        } else if (currentFirstVisibleItem > -1 && currentFirstVisibleItem < mLastFirstVisibleItem) {
            //onScrolling(Direction.UP)
            mLastFirstVisibleItem = currentFirstVisibleItem
        }

        if (totalItemCount < previousTotalItemCount) {
            this.currentPage = this.startingPageIndex
            this.previousTotalItemCount = totalItemCount
            if (totalItemCount == 0) {
                this.loading = true
            }
        }
        if (loading && totalItemCount > previousTotalItemCount) {
            loading = false
            previousTotalItemCount = totalItemCount
        }

        if (!loading && lastVisibleItemPosition + visibleThreshold > totalItemCount) {
            currentPage++
            onLoadMore(currentPage, totalItemCount, recyclerView)
            loading = true
        }
    }


    // Defines the process for actually loading more data based on page
    abstract fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?)
    abstract fun onScrolling(direction: ScrollDirection)
}