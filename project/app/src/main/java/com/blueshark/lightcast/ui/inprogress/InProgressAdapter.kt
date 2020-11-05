package com.blueshark.lightcast.ui.inprogress

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blueshark.lightcast.R
import com.blueshark.lightcast.model.InProgress
import com.blueshark.lightcast.model.ListData
import com.blueshark.lightcast.utils.Util.getDateTime
import com.blueshark.lightcast.utils.Util.getTimeFormatFromSecondString
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import net.steamcrafted.materialiconlib.MaterialIconView

class InProgressAdapter(var list: ArrayList<InProgress>)
    : RecyclerView.Adapter<InProgressAdapter.InProgressViewHolder>() {

    var onItemClick: ((InProgress) -> Unit)? = null
    var onEpisodeClick: ((InProgress) -> Unit)? = null

    var onQuickViewClick : ((InProgress) -> Unit)? = null
    var onPlayClick : ((InProgress) -> Unit)? = null
    var onListenLaterClick : ((InProgress) -> Unit)? = null
    var onTranscribeClick : ((InProgress) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InProgressViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_in_progress_item, parent, false)
        return InProgressViewHolder(view)
    }

    override fun onBindViewHolder(holder: InProgressViewHolder, position: Int) {
        val inProgressItem: InProgress = list[position]
        holder.bind(inProgressItem)
    }

    override fun getItemCount(): Int = list.size

    fun add(listData: ListData<InProgress>) {
        val prevCount = itemCount
        if (prevCount == 0 || prevCount > listData.offset) {
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

    inner class InProgressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var mAvatar : ImageView? = null
        private var mEpisodeName: TextView? = null
        private var mReleaseDate: TextView? = null
        private var mPodcastName: TextView? = null
        private var mLastPlayDate : TextView? = null
        private var mLastPlayTime : TextView? = null
        private var mDuration: TextView? = null

        private var mQuickViewButton : MaterialIconView? = null
        private var mPlayButton : MaterialIconView? = null
        private var mListenLaterButton : MaterialIconView? = null
        private var mTranscribeButton : MaterialIconView? = null

        init {
            mAvatar = itemView.findViewById(R.id.inProgressAvatar)
            mEpisodeName = itemView.findViewById(R.id.episodeNameTextView)
            mReleaseDate = itemView.findViewById(R.id.releaseDateTextView)
            mPodcastName = itemView.findViewById(R.id.podcastNameTextView)
            mLastPlayDate = itemView.findViewById(R.id.lastPlayDate)
            mDuration = itemView.findViewById(R.id.durationText)
            mLastPlayTime = itemView.findViewById(R.id.lastPlayTimeTextView)

            mQuickViewButton = itemView.findViewById(R.id.quickViewButton)
            mPlayButton = itemView.findViewById(R.id.playButton)
            mListenLaterButton = itemView.findViewById(R.id.listenLaterButton)
            mTranscribeButton = itemView.findViewById(R.id.transcribeButton)

            itemView.setOnClickListener {
                onItemClick?.invoke(list[absoluteAdapterPosition])
            }

            mEpisodeName?.setOnClickListener {
                onEpisodeClick?.invoke(list[absoluteAdapterPosition])
            }

            mPlayButton?.setOnClickListener{
                onPlayClick?.invoke(list[absoluteAdapterPosition])
            }

            mListenLaterButton?.setOnClickListener{
                onListenLaterClick?.invoke(list[absoluteAdapterPosition])
            }

            mTranscribeButton?.setOnClickListener {
                onTranscribeClick?.invoke(list[absoluteAdapterPosition])
            }

            mQuickViewButton?.setOnClickListener {
                onQuickViewClick?.invoke(list[adapterPosition])
            }
        }

        fun bind(item: InProgress) {
            val picasso = Picasso.get()
            picasso.load(item.img)
                .placeholder(R.drawable.default_album_art)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .error(R.drawable.default_album_art)
                .into(mAvatar)
            mEpisodeName?.text = item.episodeName
            mPodcastName?.text = item.podcastName
            mReleaseDate?.text = getDateTime(item.releaseDate)
            mLastPlayDate?.text = getDateTime(item.createdDate)
            mLastPlayTime?.text = getTimeFormatFromSecondString(item.timeLeft)
            mDuration?.text = item.playtime
        }
    }
}
