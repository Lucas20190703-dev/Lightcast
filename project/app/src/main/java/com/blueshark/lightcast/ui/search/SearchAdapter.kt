package com.blueshark.lightcast.ui.search

import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.blueshark.lightcast.R
import com.blueshark.lightcast.model.ListData
import com.blueshark.lightcast.model.SearchResultItem
import com.blueshark.lightcast.utils.Util
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso

class SearchAdapter(var list : ArrayList<SearchResultItem>)
    : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>(){

    var onGotoPodcast: ((SearchResultItem) -> Unit)? = null
    var onGotoEpisode : ((SearchResultItem) -> Unit)? = null

    var onQuickViewClick : ((SearchResultItem) -> Unit)? = null
    var onPlayClick : ((SearchResultItem) -> Unit)? = null
    var onListenLaterClick : ((SearchResultItem) -> Unit)? = null
    var onTranscribeClick : ((SearchResultItem) -> Unit)? = null
    var onSubscribeClick : ((SearchResultItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_search_item, parent, false)
        return SearchViewHolder(view)
    }

    override fun getItemCount() : Int = list.size

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val podcastItem: SearchResultItem = list[position]
        holder.bind(podcastItem)
    }

    fun add(listData : ListData<SearchResultItem>) {
        val prevCount = itemCount
        if (prevCount == 0 || listData.offset == 1) { //in this case offset means page number
            list.clear()
            list.addAll(listData.data)
            notifyDataSetChanged()
        } else {
            list.addAll(listData.data)
            notifyItemRangeInserted(prevCount, listData.data.size)
        }
    }

    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var mEpisodeName : TextView? = null
        private var mEpisodeThumbnail : AppCompatImageView? = null
        private var mPodcastName : TextView? = null
        private var mPublishDate : TextView? = null
        private var mAuthorName : TextView? = null
        private var mDuration : TextView? = null
        private var mPodcastDescription : TextView? = null
        private var mPlayButton : AppCompatImageButton? = null
        private var mSubscribeButton : AppCompatImageButton? = null
        private var mTranscribeButton : AppCompatImageButton? = null
        private var mListenLaterButton : AppCompatImageButton? = null
        private var mQuickViewLayout : FrameLayout? = null
        private var mEpisodeDescription : TextView? = null

        init {
            mEpisodeName = itemView.findViewById(R.id.episodeNameTextView)
            mEpisodeThumbnail = itemView.findViewById(R.id.avatarImageView)
            mPodcastName = itemView.findViewById(R.id.podcastNameTextView)
            mPublishDate = itemView.findViewById(R.id.publishDateTextView)
            mAuthorName = itemView.findViewById(R.id.authorNameTextView)
            mDuration = itemView.findViewById(R.id.durationTextView)
            mPodcastDescription = itemView.findViewById(R.id.podcastDescriptionTextView)
            mPlayButton = itemView.findViewById(R.id.playAppCompatImageButton)
            mSubscribeButton = itemView.findViewById(R.id.subscribeAppCompatImageButton)
            mTranscribeButton = itemView.findViewById(R.id.transcribeAppCompatImageButton)
            mListenLaterButton = itemView.findViewById(R.id.listenLaterAppCompatImageButton)
            mQuickViewLayout = itemView.findViewById(R.id.quickViewButtonLayout)
            mEpisodeDescription = itemView.findViewById(R.id.episodeDescriptionTextView)


            itemView.setOnClickListener {
                onGotoEpisode?.invoke(list[adapterPosition])
            }

            mPodcastName?.setOnClickListener {
                onGotoPodcast?.invoke(list[adapterPosition])
            }

            mPublishDate?.setOnClickListener {
                onGotoPodcast?.invoke(list[adapterPosition])
            }

            mAuthorName?.setOnClickListener {
                onGotoPodcast?.invoke(list[adapterPosition])
            }

            mPlayButton?.setOnClickListener {
                onPlayClick?.invoke(list[adapterPosition])
            }

            mSubscribeButton?.setOnClickListener {
                onSubscribeClick?.invoke(list[adapterPosition])
            }

            mTranscribeButton?.setOnClickListener {
                onTranscribeClick?.invoke(list[adapterPosition])
            }

            mListenLaterButton?.setOnClickListener {
                onListenLaterClick?.invoke(list[adapterPosition])
            }

            mQuickViewLayout?.setOnClickListener {
                onQuickViewClick?.invoke(list[adapterPosition])
            }

        }

        fun bind(item : SearchResultItem) {
            Picasso.get().load(item.episode?.thumbnail)
                .resize(128, 128)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .placeholder(R.drawable.default_album_art)
                .into(mEpisodeThumbnail)

            val episodeName = item.episode?.title ?: ""
            val podcastName = item.name ?: ""
            val publishedDate = Util.getDateTime(item.publishDate) ?: "Unknown"
            val author = item.authorName ?: "Unknown"
            val duration = item.episode?.duration ?: "00:00:00"
            mEpisodeName?.text = episodeName ?: ""
            mPodcastName?.text = "Podcast Name: $podcastName"
            mPublishDate?.text = "Published: $publishedDate"
            mAuthorName?.text = "Author: $author"
            mDuration?.text = "Duration: $duration"
            mPodcastDescription?.text = item.description
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mEpisodeDescription?.text =
                        Html.fromHtml(item.episode?.summary, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    mEpisodeDescription?.text = Html.fromHtml(item.episode?.summary);
                }
            } catch (e: Exception) {
                e.printStackTrace()
                mEpisodeDescription?.text = ""
            }

        }
    }
}