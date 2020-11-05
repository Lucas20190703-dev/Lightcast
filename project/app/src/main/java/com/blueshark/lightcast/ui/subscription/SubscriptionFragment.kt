package com.blueshark.lightcast.ui.subscription

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.blueshark.lightcast.MainActivity
import com.blueshark.lightcast.R
import com.blueshark.lightcast.consts.DISTANCE_TRIGGER_REFRESH
import com.blueshark.lightcast.consts.REFRESH_LOADING_TIME
import com.blueshark.lightcast.listener.ScrollDirection
import com.blueshark.lightcast.listener.LoadMoreRecyclerViewListener
import com.blueshark.lightcast.model.ListData
import com.blueshark.lightcast.model.Subscription
import com.blueshark.lightcast.ui.base.BaseFragment
import com.blueshark.lightcast.viewmodel.SubscriptionViewModel
import kotlinx.android.synthetic.main.empty_layout.*
import kotlinx.android.synthetic.main.fragment_subscription.*

class SubscriptionFragment : BaseFragment<ListData<Subscription>>(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var subscriptionAdapter: SubscriptionAdapter
    private lateinit var loadMoreRecyclerViewListener: LoadMoreRecyclerViewListener

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this, viewModelFactory {
            SubscriptionViewModel()
        }).get(SubscriptionViewModel::class.java)
        viewModel.getNewRepository()?.observe(viewLifecycleOwner, Observer<ListData<Subscription>?> {
            if (it == null) {
                emptyView.visibility=View.VISIBLE
            } else {
                emptyView.visibility=View.GONE
                subscriptionAdapter.add(it)
            }
            progressLoader.visibility = View.GONE
            refreshLayout.isEnabled = true
        })

        subscriptionAdapter = SubscriptionAdapter(ArrayList(0))

        subscriptionAdapter.onItemClick = {
            val action = SubscriptionFragmentDirections.actionSubscriptionToPodcastOverview(it.podcastId)
            findNavController().navigate(action)
        }

        return inflater.inflate(R.layout.fragment_subscription, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (subscriptionAdapter?.itemCount == 0) {
            progressLoader.visibility = View.VISIBLE
            refreshLayout.isEnabled = false
        } else {
            progressLoader.visibility = View.GONE
            refreshLayout.isEnabled = true
        }
        val mLayoutManager = GridLayoutManager(context, NUM_COLUMN)

        loadMoreRecyclerViewListener = object : LoadMoreRecyclerViewListener(mLayoutManager, 0) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {

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
            adapter = subscriptionAdapter
            addOnScrollListener(loadMoreRecyclerViewListener)
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

    override fun onRefresh() {
        viewModel.refresh()
        Handler().postDelayed({
            refreshLayout.isRefreshing = false
        }, REFRESH_LOADING_TIME)
    }

    override fun onResume() {
        (activity as MainActivity).setDrawerEnabled(true)
        (activity as MainActivity).setupToolbar(
            R.drawable.ic_menu_white_24dp,
            getString(R.string.subscriptions)
        )
        super.onResume()
    }
    companion object{
        fun newInstance(): SubscriptionFragment =
            SubscriptionFragment()

        private const val NUM_COLUMN = 3
        private const val TAG = "SubscriptionFragment"
    }
}
