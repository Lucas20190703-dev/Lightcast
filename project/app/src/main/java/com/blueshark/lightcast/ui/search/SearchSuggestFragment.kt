package com.blueshark.lightcast.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blueshark.lightcast.MainActivity
import com.blueshark.lightcast.R
import com.blueshark.lightcast.listener.LoadMoreRecyclerViewListener
import com.blueshark.lightcast.listener.ScrollDirection
import com.blueshark.lightcast.model.ListData
import com.blueshark.lightcast.model.PodcastOverview
import com.blueshark.lightcast.ui.base.BaseFragment
import com.blueshark.lightcast.utils.hideKeyboard
import com.blueshark.lightcast.viewmodel.SearchSuggestViewModel
import kotlinx.android.synthetic.main.fragment_search_suggester.*

class SearchSuggestFragment : BaseFragment<ListData<PodcastOverview>>() {
    private lateinit var suggestAdapter: SearchSuggestAdapter
    private lateinit var loadMoreRecyclerViewListener: LoadMoreRecyclerViewListener

    private var query : String? = null
    private var forceStop : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = ViewModelProvider(this, viewModelFactory {
            SearchSuggestViewModel()
        }).get(SearchSuggestViewModel::class.java)

        viewModel.getNewRepository()?.observe(viewLifecycleOwner, Observer<ListData<PodcastOverview>?> {
            if (it == null) {
                progressLoader.visibility = View.GONE
            } else {
                if (!forceStop) {
                    progressLoader.visibility = View.GONE
                    suggestAdapter.add(it)
                } else {
                    forceStop = false
                }
            }
        })

        suggestAdapter = SearchSuggestAdapter(ArrayList(0))
        suggestAdapter.onItemClick = {podcast ->
            hideKeyboard()
            (requireActivity() as MainActivity).navigateSearchPodcast(podcast)
        }

        return inflater.inflate(R.layout.fragment_search_suggester, container, false)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressLoader.visibility = View.VISIBLE
        val mLayoutManager = LinearLayoutManager(context)
        loadMoreRecyclerViewListener = object : LoadMoreRecyclerViewListener(mLayoutManager,
            1,
            3
        ) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                onQuery(page, query)
            }

            override fun onScrolling(direction: ScrollDirection) {
            }
        }

        recyclerView.apply {
            layoutManager = mLayoutManager
            adapter = suggestAdapter
            addOnScrollListener(loadMoreRecyclerViewListener)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            isNestedScrollingEnabled = true
        }
    }

    fun onQuery(page: Int, query : String?) {
        progressLoader.visibility = View.VISIBLE
        this.query = query
        if (page == 1) {
            loadMoreRecyclerViewListener.initialize()
        }
        viewModel.loadData(page, 0, query)
    }

    fun initialize() {
        forceStop = true
        suggestAdapter.clear()
    }
}