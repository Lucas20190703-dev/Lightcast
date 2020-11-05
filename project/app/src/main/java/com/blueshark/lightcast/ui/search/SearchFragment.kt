package com.blueshark.lightcast.ui.search

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.blueshark.lightcast.BaseActivity
import com.blueshark.lightcast.MainActivity
import com.blueshark.lightcast.R
import com.blueshark.lightcast.consts.DISTANCE_TRIGGER_REFRESH
import com.blueshark.lightcast.consts.REFRESH_LOADING_TIME
import com.blueshark.lightcast.listener.LoadMoreRecyclerViewListener
import com.blueshark.lightcast.listener.ScrollDirection
import com.blueshark.lightcast.model.*
import com.blueshark.lightcast.networking.getPlayTime
import com.blueshark.lightcast.ui.base.BaseFragment
import com.blueshark.lightcast.ui.component.showEpisodeDialog
import com.blueshark.lightcast.utils.Util
import com.blueshark.lightcast.viewmodel.SearchViewModel
import kotlinx.android.synthetic.main.empty_layout.*
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.emptyView
import kotlinx.android.synthetic.main.fragment_search.progressLoader
import kotlinx.android.synthetic.main.fragment_search.refreshLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SearchFragment : BaseFragment<ListData<SearchResultItem>>(), SwipeRefreshLayout.OnRefreshListener {

    private val args: SearchFragmentArgs? by navArgs()
    private lateinit var searchAdapter : SearchAdapter
    private lateinit var loadMoreRecyclerViewListener: LoadMoreRecyclerViewListener
    private var query : String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (args != null) {
            query = args!!.query
        }
        viewModel = ViewModelProvider(this, viewModelFactory {
            SearchViewModel(query)
        }).get(SearchViewModel::class.java)
        viewModel.getNewRepository()?.observe(viewLifecycleOwner, Observer<ListData<SearchResultItem>?> {
            if (it == null && searchAdapter.itemCount == 0) {
                emptyView.visibility=View.VISIBLE
            } else {
                emptyView.visibility=View.GONE
                searchAdapter.add(it!!)
            }
            progressLoader.visibility = View.GONE
            refreshLayout.isEnabled = true
        })

        (viewModel as SearchViewModel).getTotalResultRepository().observe(viewLifecycleOwner, Observer<Int> {
            if (result_count != null) {
                if (it < 2) {
                    result_count.text = "$it result"
                } else {
                    result_count.text = "$it results"
                }
            }
        })

        searchAdapter = SearchAdapter(ArrayList<SearchResultItem>(0))

        searchAdapter.onGotoPodcast = {
            val action = SearchFragmentDirections.actionSearchToPodcastOverview(it.podcastId!!)
            findNavController().navigate(action)
        }

        searchAdapter.onGotoEpisode = {
            if (it.episode != null) {
                val action = SearchFragmentDirections.actionSearchToEpisodeOverview(it.podcastId!!, it.episode!!.episodeId!!)
                findNavController().navigate(action)
            }
        }

        searchAdapter.onQuickViewClick = {
            if (it.episode != null) {
                showEpisodeDialog(
                    context = context,
                    viewLifecycleOwner = viewLifecycleOwner,
                    viewGroup = container,
                    episodeRequestParam = EpisodeRequestParam(it.podcastId!!, it.episode!!.episodeId!!)
                )
            }
        }

        searchAdapter.onTranscribeClick = {
            //TODO: transcribe
        }

        searchAdapter.onListenLaterClick = {
            if (it.episode != null) {
                (requireActivity() as BaseActivity).addListenLater(it.podcastId!!, it.episode!!.episodeId!!)
            }
        }

        searchAdapter.onPlayClick = {
            if (it.episode != null) {
                GlobalScope.launch {
                    val inprogressEpisodeDto = getPlayTime(it.episode!!.episodeId!!)
                    (activity as BaseActivity).playEpisode(
                        Audio(
                            url = it.episode!!.fileUrl,
                            title = it.episode!!.title,
                            author = it.authorName,
                            thumbnail = it.episode!!.thumbnail,
                            podcastId = it.podcastId,
                            episodeId = it.episode!!.episodeId,
                            podcastName = it.name,
                            releaseDate = it.publishDate,
                            duration = null,
                            durationInMilliSeconds = Util.getMillisecondsFromSecondString(inprogressEpisodeDto?.playtime),
                            lastPlayTime = Util.getMillisecondsFromSecondString(inprogressEpisodeDto?.timeLeft)
                        )
                    )
                }
            }
        }

        retainInstance = true
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressLoader.visibility = View.VISIBLE
        refreshLayout.isEnabled = false
        queryTextView.text = "\"$query\""

        val mLayoutManager = LinearLayoutManager(context)

        loadMoreRecyclerViewListener = object : LoadMoreRecyclerViewListener(mLayoutManager,
            1
        ) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                //viewModel.loadData(totalItemsCount)
            }

            override fun onScrolling(direction: ScrollDirection) {
                when(direction) {
                    ScrollDirection.UP -> {
                        (activity as MainActivity).toggleActionBarAndBottomNavigation(true)
                    }
                    ScrollDirection.DOWN -> {
                        (activity as MainActivity).toggleActionBarAndBottomNavigation(false)
                    }
                }
            }
        }

        searchRecyclerView.apply {
            layoutManager = mLayoutManager
            adapter = searchAdapter
            addOnScrollListener(loadMoreRecyclerViewListener)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            val dividerItemDecoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL).apply {
                setDrawable(ContextCompat.getDrawable(context, R.drawable.list_item_divider)!!)
            }
            addItemDecoration(dividerItemDecoration)
        }

        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setDistanceToTriggerSync(DISTANCE_TRIGGER_REFRESH)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            refreshLayout.setColorSchemeColors(
                requireContext().getColor(android.R.color.holo_blue_light),
                requireContext().getColor(android.R.color.holo_orange_light),
                requireContext().getColor(android.R.color.holo_green_light),
                requireContext().getColor(android.R.color.holo_red_light)
            )
        } else {
            refreshLayout.setColorSchemeColors(
                resources.getColor(android.R.color.holo_blue_light),
                resources.getColor(android.R.color.holo_orange_light),
                resources.getColor(android.R.color.holo_green_light),
                resources.getColor(android.R.color.holo_red_light)
            )
        }

        reloadButton.setOnClickListener {
            emptyView.visibility=View.GONE
            progressLoader.visibility = View.VISIBLE
            viewModel.refresh()
        }
    }

    override fun onResume() {
        (activity as MainActivity).setDrawerEnabled(false)
        (activity as MainActivity).setupToolbar(
            R.drawable.ic_arrow_back_white_24dp,
            getString(R.string.search)
        )
        super.onResume()
    }

    override fun onRefresh() {
        viewModel.refresh()
        Handler().postDelayed({
            refreshLayout.isRefreshing = false
        }, REFRESH_LOADING_TIME)
    }
}