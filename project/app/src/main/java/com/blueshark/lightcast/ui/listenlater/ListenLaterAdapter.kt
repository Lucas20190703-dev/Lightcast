package com.blueshark.lightcast.ui.listenlater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blueshark.lightcast.R
import com.blueshark.lightcast.model.ListData
import com.blueshark.lightcast.model.ListenLater
import com.blueshark.lightcast.utils.Util.getDateTime
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import net.steamcrafted.materialiconlib.MaterialIconView

class ListenLaterAdapter(var list: ArrayList<ListenLater>)
    : RecyclerView.Adapter<ListenLaterAdapter.ListenLaterViewHolder>() {

    var onItemClick: ((ListenLater) -> Unit)? = null
    var onEpisodeClick: ((ListenLater) -> Unit)? = null

    var onQuickViewClick : ((ListenLater) -> Unit)? = null
    var onPlayClick : ((ListenLater) -> Unit)? = null
    var onTranscribeClick : ((ListenLater) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListenLaterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_listen_later_item, parent, false)
        return ListenLaterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListenLaterViewHolder, position: Int) {
        val listenLaterItem: ListenLater = list[position]
        holder.bind(listenLaterItem)
    }

    override fun getItemCount(): Int = list.size

    fun add(listData: ListData<ListenLater>) {
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

    inner class ListenLaterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var mAvatar : ImageView? = null
        private var mEpisodeName: TextView? = null
        private var mReleaseDate: TextView? = null
        private var mPodcastName: TextView? = null
        private var mAddedDate : TextView? = null
        private var mDuration: TextView? = null

        private var mQuickViewButton : MaterialIconView? = null
        private var mPlayButton : MaterialIconView? = null
        private var mTranscribeButton : MaterialIconView? = null

        init {
            mAvatar = itemView.findViewById(R.id.listenLaterAvatar)
            mEpisodeName = itemView.findViewById(R.id.episodeNameTextView)
            mReleaseDate = itemView.findViewById(R.id.releasedDateTextView)
            mPodcastName = itemView.findViewById(R.id.podcastNameTextView)
            mDuration = itemView.findViewById(R.id.durationText)
            mAddedDate = itemView.findViewById(R.id.addedDate)

            mQuickViewButton = itemView.findViewById(R.id.quickViewButton)
            mPlayButton = itemView.findViewById(R.id.playButton)
            mTranscribeButton = itemView.findViewById(R.id.transcribeButton)
            itemView.setOnClickListener {
                onItemClick?.invoke(list[adapterPosition])
            }

            mEpisodeName?.setOnClickListener {
                onEpisodeClick?.invoke(list[adapterPosition])
            }

            mPlayButton?.setOnClickListener{
                onPlayClick?.invoke(list[adapterPosition])
            }

            mTranscribeButton?.setOnClickListener {
                onTranscribeClick?.invoke(list[adapterPosition])
            }

            mQuickViewButton?.setOnClickListener {
                onQuickViewClick?.invoke(list[adapterPosition])
            }
        }

        fun bind(item: ListenLater) {
            val picasso = Picasso.get()
            picasso.load(item.img)
                .placeholder(R.drawable.default_album_art)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .error(R.drawable.default_album_art)
                .into(mAvatar)
            mEpisodeName?.text = item.episodeName
            mPodcastName?.text = item.podcastName
            mReleaseDate?.text = getDateTime(item.releaseDate)
            mAddedDate?.text = getDateTime(item.createdDate)
            mDuration?.text = item.playtime
        }
    }
}
