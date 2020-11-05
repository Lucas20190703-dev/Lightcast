package com.blueshark.lightcast.ui.episodeoverview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.blueshark.lightcast.BaseActivity
import com.blueshark.lightcast.R
import com.blueshark.lightcast.listener.AddListenLaterListener
import com.blueshark.lightcast.listener.AddSubscribeListener
import com.blueshark.lightcast.model.Audio
import com.blueshark.lightcast.model.Episode
import com.blueshark.lightcast.model.Segment
import com.blueshark.lightcast.model.TextSegment
import com.blueshark.lightcast.service.MusicPlayerRemote
import com.blueshark.lightcast.utils.Util
import com.blueshark.lightcast.utils.Util.getDateTime
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso


class EpisodeOverviewAdapter(var episode : Episode? ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var context: Context? = null

    var onItemClick: ((Episode) -> Unit)? = null
    var onQuickViewClick : ((Episode) -> Unit)? = null

    var onPlayClick : ((Episode) -> Unit)? = null
    var onListenLaterClick : ((Episode) -> Unit)? = null
    var onTranscribeClick : ((Episode) -> Unit)? = null
    var onSubscribeClick : ((Long) -> Unit)? = null
    var onPodcastTitleClick : ((Long) -> Unit)? = null

    var isTranscribed = episode?.transcribed ?: false

    private var segmentByTime = episode?.segmentsByTime ?: ArrayList(0)

    private var mIndexWordMap = HashMap<Int, TextSegment>()
    private var mAdapterPosToLengthMap = HashMap<Int, Int>() // key : adapterposition value: text length untill adapterposition

    override fun getItemCount(): Int {
        if (episode == null) {
            return 2
        }
        else if (episode?.episodeExtendedProperties?.transcribe!! || episode?.transcribed!!) {
            return segmentByTime.size + 1  // header
        }
        return segmentByTime.size + 2  // header and footer
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER ->
                EpisodeOverviewHeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.section_header_episode_overview, parent, false))
            VIEW_TYPE_ITEM ->
                EpisodeOverviewViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_episode_overview_item, parent, false))
            VIEW_TYPE_FOOTER ->
                EpisodeOverviewFooterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.footer_episode_overview, parent, false))
            else -> error("Unhandled viewType=$viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val viewType = getItemViewType(position)) {
            VIEW_TYPE_HEADER -> (holder as EpisodeOverviewHeaderViewHolder).bind(episode)
            VIEW_TYPE_ITEM -> (holder as EpisodeOverviewViewHolder).bind(segmentByTime[position - 1])
            VIEW_TYPE_FOOTER -> (holder as EpisodeOverviewFooterViewHolder).bind(episode)
            else -> error("Unhandled viewType=$viewType")
        }
    }

    override fun getItemViewType(position: Int) = when (position) {
        0 -> VIEW_TYPE_HEADER
        else -> {
            if (isTranscribed) VIEW_TYPE_ITEM
            else VIEW_TYPE_FOOTER
        }
    }

    fun add(episode: Episode) {
        isTranscribed = episode.transcribed ?: false
        this.episode = episode
        if (episode.transcribed!! && episode.segmentsByTime != null) {
            segmentByTime.clear()
            segmentByTime.addAll(episode.segmentsByTime!!)
        }
        notifyDataSetChanged()
        prepare()
    }

    fun clear() {
        isTranscribed = false
        segmentByTime.clear()
        episode = null
        notifyDataSetChanged()
    }

    private fun prepare() {
        if (episode == null || !isTranscribed || episode!!.segmentsByTime == null) {
            return
        }
        mIndexWordMap.clear()
        mAdapterPosToLengthMap.clear()

        var totalLength = 0
        var nIdx = 0
        episode!!.segmentsByTime!!.forEach {
            if (it.textBySegments != null) {
                mAdapterPosToLengthMap[nIdx] = totalLength
                it.textBySegments!!.forEach { textSegment ->
                    mIndexWordMap[totalLength] = textSegment
                    totalLength += textSegment.text!!.length
                }
                nIdx += 1
            }
        }
    }

    inner class EpisodeOverviewHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        AddSubscribeListener, AddListenLaterListener {

        private var mEpisodeAvatar : AppCompatImageView? = null
        private var mPodcastTitleLayout : LinearLayout? = null
        private var mPodcastTitle: TextView? = null
        private var mAuthorName : TextView? = null
        private var mReleaseDate : TextView? = null
        private var mEpisodeTitle : TextView? = null
        private var mDuration : TextView? = null
        private var mDescription: TextView? = null
        private var mSummary : TextView? = null

        private var mListenerButton : AppCompatImageButton? = null
        private var mTranscribeButton : AppCompatImageButton? = null
        private var mSubscribeButton: AppCompatImageButton? = null
        private var mPlayButton : AppCompatImageButton? = null


        init {
            mEpisodeAvatar = itemView.findViewById(R.id.headerImageView)
            mPodcastTitleLayout = itemView.findViewById(R.id.podcastTitleLayout)
            mPodcastTitle = itemView.findViewById(R.id.podcastTitleTextView)
            mAuthorName = itemView.findViewById(R.id.authorNameTextView)
            mReleaseDate = itemView.findViewById(R.id.durationTextView)
            mEpisodeTitle = itemView.findViewById(R.id.episodeNameTextView)
            mDuration = itemView.findViewById(R.id.durationTextView)
            mDescription = itemView.findViewById(R.id.descriptionTextView)
            mSummary = itemView.findViewById(R.id.summaryTextView)

            mListenerButton = itemView.findViewById(R.id.listenLaterIconButton)
            mTranscribeButton = itemView.findViewById(R.id.transcribeIconButton)
            mSubscribeButton = itemView.findViewById(R.id.subscribeButton)
            mPlayButton = itemView.findViewById(R.id.playIconButton)

            (context as BaseActivity).addSubscribeEventListener(this)
            (context as BaseActivity).addListenLaterEventListener(this)

            mDescription?.movementMethod = LinkMovementMethod.getInstance()
            mSummary?.movementMethod = LinkMovementMethod.getInstance()


        }

        @SuppressLint("SetTextI18n")
        fun bind(item: Episode?) {
            val picasso = Picasso.get()
            picasso.load(item?.thumbnail)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .placeholder(R.drawable.default_album_art)
                .error(R.drawable.default_album_art)
                .into(mEpisodeAvatar)
            mPodcastTitle?.text = item?.podcastName
            val an = item?.authorName ?: "Unknown"
            mAuthorName?.text = "$an - "
            mReleaseDate?.text = getDateTime(item?.releaseDate)
            mEpisodeTitle?.text = item?.title
            mDuration?.text = item?.duration

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mDescription?.text =
                        Html.fromHtml(item?.description, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    mDescription?.text = Html.fromHtml(item?.description)
                }
            } catch (e: Exception) {
                mDescription?.text = ""
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mSummary?.text =
                        Html.fromHtml(item?.summary, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    mSummary?.text = Html.fromHtml(item?.summary)
                }
            } catch (e: Exception) {
                mSummary?.text = ""
            }

            mSubscribeButton?.setOnClickListener {
                if (item != null) {
                    if (item.episodeExtendedProperties?.subscribe!!) {
                        (context as BaseActivity).showSnakbar("Already subscribed.")
                    } else {
                        onSubscribeClick?.invoke(item.podcastId!!)
                    }
                }
            }

            mListenerButton?.setOnClickListener {
                if (item != null) {
                    if (item.episodeExtendedProperties?.listenLater!!) {
                        (context as BaseActivity).showSnakbar("Already added.")
                    } else {
                        onListenLaterClick?.invoke(item)
                    }
                }
            }

            mTranscribeButton?.setOnClickListener {
                if (item != null) {
                    if(item.episodeExtendedProperties?.transcribe!!) {
                        (context as BaseActivity).showSnakbar("Already transcribed.")
                    } else {
                        onTranscribeClick?.invoke(item)
                    }
                }
            }
            mPlayButton?.setOnClickListener {
                if (item != null) {
                    onPlayClick?.invoke(item)
                }
            }

            mPodcastTitleLayout?.setOnClickListener {
                if (item != null) {
                    item.podcastId?.let { it1 -> onPodcastTitleClick?.invoke(it1) }
                }
            }
        }

        override fun onSubscribeSuccess() {
            TODO("Not yet implemented")
        }

        override fun onSubscribeFailed(message: String?) {
            TODO("Not yet implemented")
        }

        override fun onListenLaterSuccess() {
            TODO("Not yet implemented")
        }

        override fun onListenLaterFailed(message: String?) {
            TODO("Not yet implemented")
        }
    }

    private val linkedTouchMovementMethod = LinkTouchMovementMethod()
    inner class EpisodeOverviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private var mCaptionText : TextView? = null
        private var mTranscriptText : TextView? = null

        init {
            mCaptionText = itemView.findViewById(R.id.captionTextView)
            mTranscriptText = itemView.findViewById(R.id.transcriptionTextView)
        }

        fun bind(item: Segment?) {
            mCaptionText?.text = item?.caption
            val spannableString = makeSpannableString(item!!)
            mTranscriptText?.text = spannableString
            mTranscriptText?.movementMethod = linkedTouchMovementMethod
        }

        private fun makeSpannableString(item : Segment): SpannableStringBuilder? {
            var totalLength = 0

            if (item.textBySegments != null) {
                val spannableStringBuilder = SpannableStringBuilder()
                item.textBySegments!!.forEach {
                    val currentWordLength = it.text!!.length
                    spannableStringBuilder.append(it.text!!)
                    spannableStringBuilder.setSpan(
                        object : TouchableSpan(
                            Color.DKGRAY,
                            Color.TRANSPARENT,
                            Color.WHITE,
                            Color.BLUE
                        ) {
                            override fun onClick(widget: View) {
                                val selectionStart = (widget as TextView).selectionStart
                                val prevSegmentLength = mAdapterPosToLengthMap[adapterPosition -1] ?: 0
                                val word = mIndexWordMap[prevSegmentLength + selectionStart]
                                if (MusicPlayerRemote.isServiceConnected()) {
                                    MusicPlayerRemote.playSongWith(
                                        Audio(
                                            url = episode?.fileUrl,
                                            title = episode?.title,
                                            author = episode?.authorName,
                                            thumbnail = episode?.thumbnail,
                                            podcastId = episode?.podcastId,
                                            episodeId = episode?.episodeId,
                                            podcastName = episode?.podcastName,
                                            releaseDate = episode?.releaseDate,
                                            duration = episode?.duration,
                                            durationInMilliSeconds = Util.getMillisecondsFromSecondString(episode?.duration),
                                            lastPlayTime = Util.getMillisecondsFromHMSMSString(word?.startTime)
                                        )
                                    )
                                }
                            }
                        },
                        totalLength,
                        totalLength + currentWordLength,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                    totalLength += currentWordLength
                }
                return spannableStringBuilder
            }
            return null
        }
    }

    inner class EpisodeOverviewFooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var mTranscribeButton : AppCompatButton? = null

        init {
            mTranscribeButton = itemView.findViewById(R.id.transcribeButton)
        }

        fun bind(item: Episode?) {
            if (item != null) {
                mTranscribeButton?.setOnClickListener {
                    onTranscribeClick?.invoke(episode!!)
                }
            } else {
                mTranscribeButton?.setOnClickListener(null)
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 8101
        private const val VIEW_TYPE_ITEM = 8201
        private const val VIEW_TYPE_FOOTER = 8301
    }
}