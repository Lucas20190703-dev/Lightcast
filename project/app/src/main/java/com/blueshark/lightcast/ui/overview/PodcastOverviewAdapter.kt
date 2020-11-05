package com.blueshark.lightcast.ui.overview

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.blueshark.lightcast.BaseActivity
import com.blueshark.lightcast.MainActivity
import com.blueshark.lightcast.R
import com.blueshark.lightcast.listener.AddListenLaterListener
import com.blueshark.lightcast.listener.AddSubscribeListener
import com.blueshark.lightcast.listener.RequestDataReceiver
import com.blueshark.lightcast.model.Episode
import com.blueshark.lightcast.model.PodcastOverview
import com.blueshark.lightcast.model.SearchResult
import com.blueshark.lightcast.model.TopPodcast
import com.blueshark.lightcast.utils.Util.getDateTime
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import net.steamcrafted.materialiconlib.MaterialIconView
import org.json.JSONObject
import java.io.Reader

class PodcastOverviewAdapter(var podcastOverView : PodcastOverview?, var episodeCount : Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {

    var context: Context? = null

    var onItemClick: ((Episode) -> Unit)? = null
    var onQuickViewClick : ((Episode) -> Unit)? = null

    var onPlayClick : ((Episode) -> Unit)? = null
    var onListenLaterClick : ((Episode) -> Unit)? = null
    var onTranscribeClick : ((Episode) -> Unit)? = null
    var onSubscribeClick : ((Long) -> Unit)? = null

    private var topPodcast = podcastOverView?.topPodcast

    private var episodes = topPodcast?.episodes ?: ArrayList(0)

    override fun getItemCount(): Int = episodes.size + 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER ->
                PodcastOverviewSectionHeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.section_header_overview, parent, false))
            VIEW_TYPE_ITEM ->
                PodcastOverviewViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_overview_item, parent, false))
            else -> error("Unhandled viewType=$viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val viewType = getItemViewType(position)) {
            VIEW_TYPE_HEADER -> (holder as PodcastOverviewSectionHeaderViewHolder).bind(
                podcastOverView?.topPodcast
            )
            VIEW_TYPE_ITEM -> (holder as PodcastOverviewViewHolder).bind(episodes[position - 1])
            else -> error("Unhandled viewType=$viewType")
        }
    }

    override fun getItemViewType(position: Int) = when (position) {
        0 -> VIEW_TYPE_HEADER
        else -> VIEW_TYPE_ITEM
    }

    fun add(searchResult: SearchResult) {
        if (searchResult.podcastOverview != null) {
            val prevCount = itemCount - 1
            if (prevCount == 0 || prevCount >= searchResult.offset) {
                podcastOverView = searchResult.podcastOverview
                topPodcast = podcastOverView!!.topPodcast
                episodes.clear()
                episodes.addAll(topPodcast!!.episodes!!)
                episodeCount = searchResult.episodeCount
                notifyDataSetChanged()
            } else {
                podcastOverView = searchResult.podcastOverview
                topPodcast = podcastOverView!!.topPodcast
                episodes.addAll(topPodcast!!.episodes!!)
                episodeCount = searchResult.episodeCount
                notifyItemRangeInserted(itemCount, topPodcast!!.episodes!!.size)
            }
        }
    }

    fun clear() {
        this.podcastOverView = null
        topPodcast = null
        episodes.clear()
        notifyDataSetChanged()
    }

    inner class PodcastOverviewSectionHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), AddSubscribeListener {

        private var mPodcastTitle: TextView? = null
        private var mPodcastSubTitle: TextView? = null
        private var mEpisodeCount: TextView? = null
        private var mDescription: TextView? = null
        private var mHeaderImageView: ImageView? = null
        private var mSubscribeButton: AppCompatButton? = null

        init {
            mHeaderImageView = itemView.findViewById(R.id.headerImageView)
            mPodcastTitle = itemView.findViewById(R.id.episodeTitleTextView)
            mEpisodeCount = itemView.findViewById(R.id.episodeCountTextView)
            mDescription = itemView.findViewById(R.id.descriptionTextView)
            mPodcastSubTitle = itemView.findViewById(R.id.podcastSubTitleTextView)
            mSubscribeButton = itemView.findViewById(R.id.subscribeButton)

            (context as BaseActivity).addSubscribeEventListener(this)
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: TopPodcast?) {
            val picasso = Picasso.get()
            picasso.load(item?.thumbnail)
                .placeholder(R.drawable.default_album_art)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .error(R.drawable.default_album_art)
                .into(mHeaderImageView)
            mPodcastTitle?.text = item?.name
            mPodcastSubTitle?.text = item?.authorName ?: "Unknown"

            mEpisodeCount?.text = when {
                episodeCount < 1 -> "No Episode"
                episodeCount == 1 -> "$episodeCount Episode"
                else -> "$episodeCount Episodes"
            }
            mDescription?.text = item?.description

            if (item!= null && item.isSubscribed) {
                mSubscribeButton?.text = "Subscribed"
                mSubscribeButton?.isSelected = true
            } else {
                mSubscribeButton?.text = "+ Subscribe"
                mSubscribeButton?.isSelected = false
            }
            mSubscribeButton?.setOnClickListener {
                if (item != null && !item.isSubscribed) {
                    mSubscribeButton?.text = "Subscribing..."
                    onSubscribeClick?.invoke(topPodcast?.podcastId!!)
                } else {
                    mSubscribeButton?.text = "Subscribed"
                    mSubscribeButton?.isSelected = true
                    (context as BaseActivity).showSnakbar("Already subscribed.")
                }
            }
        }

        override fun onSubscribeSuccess() {
            (context as BaseActivity).runOnUiThread {
                mSubscribeButton?.text = "Subscribed"
                mSubscribeButton?.isSelected = true
            }
        }

        override fun onSubscribeFailed(message: String?) {
            (context as BaseActivity).runOnUiThread {
                mSubscribeButton?.text = "+ Subscribe"
                mSubscribeButton?.isSelected = false
            }
        }
    }

    inner class PodcastOverviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), AddListenLaterListener{
        private var mReleaseDate : TextView? = null
        private var mEpisodeName: TextView? = null
        private var mDuration : TextView? = null
        private var mQuickViewButton : MaterialIconView? = null
        private var mPlayButton : MaterialIconView? = null
        private var mListenLaterButton : MaterialIconView? = null
        private var mTranscribeButton : MaterialIconView? = null


        init {
            mReleaseDate = itemView.findViewById(R.id.releaseDateTextView)
            mEpisodeName = itemView.findViewById(R.id.episodeNameTextView)
            mDuration = itemView.findViewById(R.id.durationTextView)
            mQuickViewButton = itemView.findViewById(R.id.quickViewButton)
            mListenLaterButton = itemView.findViewById(R.id.listenLaterButton)
            mPlayButton = itemView.findViewById(R.id.playButton)
            mTranscribeButton = itemView.findViewById(R.id.transcribeButton)

            itemView.setOnClickListener{
                onItemClick?.invoke(episodes[adapterPosition - 1])
            }

            mQuickViewButton?.setOnClickListener {
                onQuickViewClick?.invoke(episodes[adapterPosition - 1])
            }

            mPlayButton?.setOnClickListener {
                onPlayClick?.invoke(episodes[adapterPosition - 1])
            }

            mTranscribeButton?.setOnClickListener {
                onTranscribeClick?.invoke(episodes[adapterPosition - 1])
            }

            (context as BaseActivity).addListenLaterEventListener(this)
        }

        fun bind(item: Episode?) {
            mReleaseDate?.text = getDateTime(item?.releaseDate)
            mEpisodeName?.text = item?.title
            mDuration?.text = item?.duration

            if (item?.episodeExtendedProperties != null && item.episodeExtendedProperties!!.listenLater!!){
                mListenLaterButton?.setOnClickListener {
                    onListenLaterClick?.invoke(episodes[adapterPosition - 1 ])
                }
            } else {
                mListenLaterButton?.setOnClickListener {
                    (context as BaseActivity).showSnakbar("Already added.")
                }
            }
        }

        override fun onListenLaterSuccess() {
            TODO("Not yet implemented")
        }

        override fun onListenLaterFailed(message: String?) {
            TODO("Not yet implemented")
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 4815
        private const val VIEW_TYPE_ITEM = 1623
    }
}