package com.blueshark.lightcast.ui.overview

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
import com.blueshark.lightcast.consts.*
import com.blueshark.lightcast.listener.ScrollDirection
import com.blueshark.lightcast.listener.LoadMoreRecyclerViewListener
import com.blueshark.lightcast.listener.RequestDataReceiver
import com.blueshark.lightcast.model.Audio
import com.blueshark.lightcast.model.EpisodeRequestParam
import com.blueshark.lightcast.model.SearchResult
import com.blueshark.lightcast.networking.getPlayTime
import com.blueshark.lightcast.service.MusicPlayerRemote
import com.blueshark.lightcast.ui.base.BaseFragment
import com.blueshark.lightcast.ui.component.showEpisodeDialog
import com.blueshark.lightcast.utils.Util
import com.blueshark.lightcast.viewmodel.PodcastOverviewViewModel
import kotlinx.android.synthetic.main.fragment_overview.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.Reader

class   PodcastOverviewFragment : BaseFragment<SearchResult>(), SwipeRefreshLayout.OnRefreshListener{

    private val args: PodcastOverviewFragmentArgs? by navArgs()
    private var podcastId : Long = -1

    private lateinit var podcastOverViewAdapter : PodcastOverviewAdapter
    private lateinit var loadMoreRecyclerViewListener: LoadMoreRecyclerViewListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (args != null) {
            podcastId = args!!.podcastId
        }

        podcastOverViewAdapter = PodcastOverviewAdapter(null, 0)
        podcastOverViewAdapter.onItemClick = {episode ->
            val action = PodcastOverviewFragmentDirections.actionPodcastToEpisodeOverview(episode.podcastId!!, episode.episodeId!!)
            findNavController().navigate(action)
        }

        podcastOverViewAdapter.onQuickViewClick = {
            showEpisodeDialog(context = context,
                viewLifecycleOwner = viewLifecycleOwner,
                viewGroup = container,
                episodeRequestParam = EpisodeRequestParam(it.podcastId!!, it.episodeId!!))
        }

        podcastOverViewAdapter.onListenLaterClick = {
            (requireActivity() as BaseActivity).addListenLater(it.podcastId!!, it.episodeId!!)
        }

        podcastOverViewAdapter.onSubscribeClick = {
            (requireActivity() as BaseActivity).addSubscription(it)
        }

        podcastOverViewAdapter.onPlayClick = {
            GlobalScope.launch {
                val inprogressEpisodeDto = getPlayTime(it.episodeId!!)
                (activity as BaseActivity).playEpisode(
                    Audio(
                        url = it.fileUrl,
                        title = it.title,
                        author = it.authorName,
                        thumbnail = it.thumbnail,
                        podcastId = it.podcastId,
                        episodeId = it.episodeId,
                        podcastName = it.podcastName,
                        releaseDate = it.releaseDate,
                        duration = it.duration,
                        durationInMilliSeconds = Util.getMillisecondsFromSecondString(inprogressEpisodeDto?.playtime),
                        lastPlayTime = Util.getMillisecondsFromSecondString(inprogressEpisodeDto?.timeLeft)
                    )
                )
            }

        }

        viewModel = ViewModelProvider(this, viewModelFactory {
            PodcastOverviewViewModel(
                podcastId
            )
        }).get(PodcastOverviewViewModel::class.java)

        viewModel.getNewRepository()?.observe(viewLifecycleOwner, Observer<SearchResult?> {
            if (it == null) {
                emptyView.visibility = View.VISIBLE

                (activity as MainActivity).showSnakbar(getString(R.string.data_loading_failed))
            } else {
                emptyView.visibility = View.GONE
                podcastOverViewAdapter.add(it)
            }
            progressLoaderLayout.visibility = View.GONE
            progressLoader.visibility = View.GONE
            refreshLayout.isEnabled = true
        })
        return inflater.inflate(R.layout.fragment_overview, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (podcastOverViewAdapter == null) {
            progressLoaderLayout.visibility = View.VISIBLE
            progressLoader.visibility = View.VISIBLE
            refreshLayout.isEnabled = false
        } else {
            progressLoaderLayout.visibility = View.GONE
            progressLoader.visibility = View.GONE
            refreshLayout.isEnabled = true
        }

        podcastOverViewAdapter.context = context
        val mLayoutManager = LinearLayoutManager(context)

        loadMoreRecyclerViewListener = object : LoadMoreRecyclerViewListener(mLayoutManager, START_INDEX) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                viewModel.loadData(totalItemsCount)
            }

            override fun onScrolling(direction: ScrollDirection) {
            }
        }

        recyclerView.apply {
            layoutManager = mLayoutManager
            adapter = podcastOverViewAdapter
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
    }

    override fun onResume() {
        (activity as MainActivity).setDrawerEnabled(false)
        (activity as MainActivity).setupToolbar(
            R.drawable.ic_arrow_back_white_24dp,
            getString(R.string.podcast_overview)
        )
        super.onResume()
    }

    override fun onRefresh() {
        viewModel?.refresh()
        Handler().postDelayed({
            refreshLayout.isRefreshing = false
        }, REFRESH_LOADING_TIME)
    }

    companion object{
        private const val TAG = "PodcastOverview"
        private const val START_INDEX = 0
    }
}