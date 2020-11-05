package com.blueshark.lightcast.ui.episodeoverview

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Html
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.blueshark.lightcast.BaseActivity
import com.blueshark.lightcast.MainActivity
import com.blueshark.lightcast.R
import com.blueshark.lightcast.consts.DISTANCE_TRIGGER_REFRESH
import com.blueshark.lightcast.consts.REFRESH_LOADING_TIME
import com.blueshark.lightcast.model.Audio
import com.blueshark.lightcast.model.Episode
import com.blueshark.lightcast.networking.getPlayTime
import com.blueshark.lightcast.ui.base.BaseFragment
import com.blueshark.lightcast.utils.Util
import com.blueshark.lightcast.viewmodel.EpisodeOverviewViewModel
import kotlinx.android.synthetic.main.empty_layout.*
import kotlinx.android.synthetic.main.fragment_episode_overview.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EpisodeOverviewFragment : BaseFragment<Episode>(), SwipeRefreshLayout.OnRefreshListener {

    private var podcastId : Long = -1
    private var episodeId : String? = null
    private val args: EpisodeOverviewFragmentArgs by navArgs()

    private val episodeOverviewAdapter = EpisodeOverviewAdapter(null)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        podcastId = args.podcastId
        episodeId = args.episodeId

        viewModel = ViewModelProvider(this, viewModelFactory {
            EpisodeOverviewViewModel(
                podcastId,
                episodeId!!
            )
        }).get(EpisodeOverviewViewModel::class.java)

        viewModel.getNewRepository()?.observe(viewLifecycleOwner, Observer<Episode?> {
            if (it != null) {
                emptyView.visibility = View.GONE
                episodeOverviewAdapter.add(it)
            } else {
                emptyView.visibility = View.VISIBLE
            }
            progressLoader.visibility = View.GONE
            refreshLayout.isEnabled = true
        })

        episodeOverviewAdapter.onListenLaterClick = {
            (requireActivity() as BaseActivity).addListenLater(it.podcastId!!, it.episodeId!!)
        }

        episodeOverviewAdapter.onSubscribeClick = {
            (requireActivity() as BaseActivity).addSubscription(it)
        }

        episodeOverviewAdapter.onTranscribeClick = {
            //TODO
        }

        episodeOverviewAdapter.onPlayClick = {
            GlobalScope.launch {
                val inprogressEpisodeDto = getPlayTime(it.episodeId!!)
                (requireActivity() as BaseActivity).playEpisode(
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
                        lastPlayTime = Util.getMillisecondsFromSecondString(inprogressEpisodeDto?.playtime)
                    )
                )
            }
        }

        episodeOverviewAdapter.onPodcastTitleClick = {
            val action = EpisodeOverviewFragmentDirections.actionEpisodeToPodcastOverview(it)
            findNavController().navigate(action)
        }
        return inflater.inflate(R.layout.fragment_episode_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (episodeOverviewAdapter.episode != null) {
            progressLoader.visibility = View.GONE
            refreshLayout.isEnabled = true
        } else {
            progressLoader.visibility = View.VISIBLE
            refreshLayout.isEnabled = false
        }

        episodeOverviewAdapter.context = context

        val mLayoutManager = LinearLayoutManager(context)

        recyclerView.apply {
            layoutManager = mLayoutManager
            adapter = episodeOverviewAdapter
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
            getString(R.string.episode_overview)
        )
        super.onResume()
    }

    override fun onRefresh() {
        progressLoader.visibility = View.VISIBLE
        viewModel.refresh()
        Handler().postDelayed({
            refreshLayout?.isRefreshing = false
        }, REFRESH_LOADING_TIME)
    }

//    private fun updateUI(episode: Episode?) {
//        if (episode != null) {
//            emptyView.visibility = View.GONE
//            val picasso = Picasso.get()
//            picasso.load(episode.thumbnail)
//                .placeholder(R.drawable.default_album_art)
//                .error(R.drawable.default_album_art)
//                .into(headerImageView)
//
//            podcastTitleTextView?.text = episode.podcastName
//            authorNameTextView?.text = episode.authorName
//            releaseDateTextView?.text = getDateTime(episode.releaseDate)
//            episodeNameTextView?.text = episode.title
//
//            try {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    descriptionTextView.text =
//                        Html.fromHtml(episode.summary, Html.FROM_HTML_MODE_COMPACT)
//                } else {
//                    descriptionTextView.text = Html.fromHtml(episode.summary)
//                }
//            } catch (e: Exception) {
//                descriptionTextView.text = ""
//            }
//        } else {
//            emptyView.visibility = View.VISIBLE
//        }
//    }
    companion object{
        private const val TAG = "EpisodeOverviewFragment"
    }
}