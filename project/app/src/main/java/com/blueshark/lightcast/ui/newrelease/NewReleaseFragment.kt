package com.blueshark.lightcast.ui.newrelease

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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
import com.blueshark.lightcast.listener.ScrollDirection
import com.blueshark.lightcast.listener.LoadMoreRecyclerViewListener
import com.blueshark.lightcast.model.*
import com.blueshark.lightcast.networking.getPlayTime
import com.blueshark.lightcast.ui.base.BaseFragment
import com.blueshark.lightcast.ui.component.showEpisodeDialog
import com.blueshark.lightcast.utils.Util
import com.blueshark.lightcast.viewmodel.NewReleaseViewModel
import kotlinx.android.synthetic.main.empty_layout.*
import kotlinx.android.synthetic.main.fragment_new_release.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NewReleaseFragment : BaseFragment<ListData<Release>>(), SwipeRefreshLayout.OnRefreshListener{

    private lateinit var newReleaseAdapter : NewReleaseAdapter
    private lateinit var loadMoreRecyclerViewListener: LoadMoreRecyclerViewListener

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        viewModel = ViewModelProvider(this, viewModelFactory {
                NewReleaseViewModel()
            }).get(NewReleaseViewModel::class.java)
        viewModel.getNewRepository()?.observe(viewLifecycleOwner, Observer<ListData<Release>?> {
            if (it == null  && newReleaseAdapter.itemCount == 0) {
                emptyView.visibility=View.VISIBLE
            } else {
                emptyView.visibility=View.GONE
                newReleaseAdapter.add(it!!)
            }
            progressLoader.visibility = View.GONE
            refreshLayout.isEnabled = true
        })

        newReleaseAdapter = NewReleaseAdapter(ArrayList(0))

        newReleaseAdapter.onItemClick = {release ->
            Log.d(TAG, release.authorName)
            val action = NewReleaseFragmentDirections.actionNewReleaseToPodcastOverview(release.podcastId)
            findNavController().navigate(action)
        }

        newReleaseAdapter.onEpisodeClick = {release ->
            val action = NewReleaseFragmentDirections.actionNewReleaseToEpisodeOverview(release.podcastId, release.episodeId)
            findNavController().navigate(action)
        }

        newReleaseAdapter.onQuickViewClick = {
            showEpisodeDialog(context = context,
                viewLifecycleOwner = viewLifecycleOwner,
                viewGroup = container,
                episodeRequestParam = EpisodeRequestParam(it.podcastId, it.episodeId)
            )
        }

        retainInstance = true
        return inflater.inflate(R.layout.fragment_new_release, container, false)
    }

    override fun onResume() {
        (activity as MainActivity).setDrawerEnabled(true)
        (activity as MainActivity).setupToolbar(
            R.drawable.ic_menu_white_24dp,
            getString(R.string.new_release)
        )
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView node initialized here
        if (newReleaseAdapter.itemCount == 0) {
            progressLoader.visibility = View.VISIBLE
            refreshLayout.isEnabled = false
        } else {
            progressLoader.visibility = View.GONE
            refreshLayout.isEnabled = true
        }

        val mLayoutManager = LinearLayoutManager(context)

        loadMoreRecyclerViewListener = object : LoadMoreRecyclerViewListener(mLayoutManager, START_INDEX) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                viewModel.loadData(totalItemsCount)
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

        recyclerView.apply {
            layoutManager = mLayoutManager
            adapter = newReleaseAdapter
            addOnScrollListener(loadMoreRecyclerViewListener)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)

            val dividerItemDecoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL).apply {
                setDrawable(ContextCompat.getDrawable(context, R.drawable.list_item_divider)!!)
            }
            addItemDecoration(dividerItemDecoration)
            isNestedScrollingEnabled = true
        }

        refreshLayout?.setOnRefreshListener(this)
        refreshLayout?.setDistanceToTriggerSync(DISTANCE_TRIGGER_REFRESH)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            refreshLayout?.setColorSchemeColors(
                requireContext().getColor(android.R.color.holo_blue_light),
                requireContext().getColor(android.R.color.holo_orange_light),
                requireContext().getColor(android.R.color.holo_green_light),
                requireContext().getColor(android.R.color.holo_red_light)
            )
        } else {
            @Suppress("DEPRECATION")
            refreshLayout?.setColorSchemeColors(
                resources.getColor(android.R.color.holo_blue_light),
                resources.getColor(android.R.color.holo_orange_light),
                resources.getColor(android.R.color.holo_green_light),
                resources.getColor(android.R.color.holo_red_light)
            )
        }

        newReleaseAdapter.onListenLaterClick = {
            (requireActivity() as BaseActivity).addListenLater(it.podcastId, it.episodeId)
        }

        newReleaseAdapter.onPlayClick = {
            GlobalScope.launch {
                val inprogressEpisodeDto = getPlayTime(it.episodeId)
                (activity as BaseActivity).playEpisode(
                    Audio(
                        url = it.audioFilePath,
                        title = it.episodeTitle,
                        author = it.authorName,
                        thumbnail = it.imageUrl,
                        podcastId = it.podcastId,
                        episodeId = it.episodeId,
                        podcastName = it.podcastTitle,
                        releaseDate = it.releaseDate,
                        duration = it.duration,
                        durationInMilliSeconds = Util.getMillisecondsFromSecondString(inprogressEpisodeDto?.playtime),
                        lastPlayTime =  Util.getMillisecondsFromSecondString(inprogressEpisodeDto?.timeLeft)
                    )
                )
            }
        }

        reloadButton.setOnClickListener {
            emptyView.visibility=View.GONE
            progressLoader.visibility = View.VISIBLE
            viewModel.refresh()
        }
    }

    override fun onRefresh() {
        viewModel.refresh()
        Handler().postDelayed({
            refreshLayout?.isRefreshing = false
        }, REFRESH_LOADING_TIME)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main, menu)
    }

    companion object {
        private var START_INDEX = 0
        private const val TAG = "NewReleaseFragment"
    }
}
