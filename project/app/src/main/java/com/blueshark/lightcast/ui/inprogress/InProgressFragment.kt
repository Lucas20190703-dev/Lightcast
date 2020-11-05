package com.blueshark.lightcast.ui.inprogress

import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
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
import com.blueshark.lightcast.ui.base.BaseFragment
import com.blueshark.lightcast.ui.component.showEpisodeDialog
import com.blueshark.lightcast.utils.Util
import com.blueshark.lightcast.viewmodel.InProgressViewModel
import kotlinx.android.synthetic.main.empty_layout.*
import kotlinx.android.synthetic.main.fragment_in_progress.*
import kotlinx.android.synthetic.main.fragment_new_release.recyclerView
import kotlinx.android.synthetic.main.fragment_new_release.refreshLayout

class InProgressFragment : BaseFragment<ListData<InProgress>>(), SwipeRefreshLayout.OnRefreshListener{

    private lateinit var inProgressAdapter: InProgressAdapter
    private lateinit var loadMoreRecyclerViewListener: LoadMoreRecyclerViewListener


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        viewModel = ViewModelProvider(this, viewModelFactory {
            InProgressViewModel()
        }).get(InProgressViewModel::class.java)

        viewModel.getNewRepository()?.observe(viewLifecycleOwner, Observer<ListData<InProgress>?>{
            if (it == null  && inProgressAdapter.itemCount == 0) {
                emptyView.visibility=View.VISIBLE
            } else {
                emptyView.visibility=View.GONE
                inProgressAdapter.add(it!!)
            }
            progressLoader.visibility = View.GONE
            refreshLayout.isEnabled = true
        })

        inProgressAdapter = InProgressAdapter(ArrayList<InProgress>(0))
        inProgressAdapter.onItemClick = {
            val action = InProgressFragmentDirections.actionInProgressToPodcastOverview(it.podcastId)
            findNavController().navigate(action)
        }

        inProgressAdapter.onEpisodeClick = {
            val action = InProgressFragmentDirections.actionInProgressToEpisodeOverview(
                it.podcastId,
                it.episodeId,
                Util.getMillisecondsFromSecondString(it.timeLeft).toLong()
            )
            findNavController().navigate(action)
        }

        inProgressAdapter.onQuickViewClick = {
            showEpisodeDialog(context = context,
                viewLifecycleOwner = viewLifecycleOwner,
                viewGroup = container,
                episodeRequestParam = EpisodeRequestParam(it.podcastId, it.episodeId)
            )
        }

        inProgressAdapter.onListenLaterClick = {
            (requireActivity() as BaseActivity).addListenLater(it.podcastId, it.episodeId)
        }

        inProgressAdapter.onPlayClick = {
            (activity as BaseActivity).playEpisode(
                Audio(
                    url = it.fileUrl,
                    title = it.episodeName,
                    author = null,
                    thumbnail = it.img,
                    podcastId = it.podcastId,
                    episodeId = it.episodeId,
                    podcastName = it.podcastName,
                    releaseDate = it.releaseDate,
                    duration = null,
                    lastPlayTime = Util.getMillisecondsFromSecondString(it.timeLeft)
                )
            )
        }

        retainInstance = true
        return inflater.inflate(R.layout.fragment_in_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (inProgressAdapter?.itemCount == 0) {
            progressLoader.visibility = View.VISIBLE
            refreshLayout.isEnabled = false
        } else {
            progressLoader.visibility = View.GONE
            refreshLayout.isEnabled = true
        }
        val mLayoutManager = LinearLayoutManager(context)

        loadMoreRecyclerViewListener = object : LoadMoreRecyclerViewListener(mLayoutManager,
            START_INDEX,
            6
        ) {
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
            adapter = inProgressAdapter
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
        (activity as MainActivity).setDrawerEnabled(true)
        (activity as MainActivity).setupToolbar(
            R.drawable.ic_menu_white_24dp,
            getString(R.string.in_progress)
        )
        super.onResume()
    }

    override fun onRefresh() {
        viewModel.refresh()
        Handler().postDelayed({
            refreshLayout.isRefreshing = false
        }, REFRESH_LOADING_TIME)
    }

    companion object{
        fun newInstance(): InProgressFragment =
            InProgressFragment()

        private const val TAG = "InProgressFragment"
        private const val START_INDEX = 0
    }
}
