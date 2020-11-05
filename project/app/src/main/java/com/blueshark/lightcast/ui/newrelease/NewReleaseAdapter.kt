package com.blueshark.lightcast.ui.newrelease

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blueshark.lightcast.R
import com.blueshark.lightcast.model.ListData
import com.blueshark.lightcast.model.Release
import com.blueshark.lightcast.utils.Util.getDateTime
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import net.steamcrafted.materialiconlib.MaterialIconView

class NewReleaseAdapter(var list: ArrayList<Release>)
    : RecyclerView.Adapter<NewReleaseAdapter.NewReleaseViewHolder>() {

    var onItemClick: ((Release) -> Unit)? = null
    var onEpisodeClick : ((Release) -> Unit)? = null

    var onQuickViewClick : ((Release) -> Unit)? = null
    var onPlayClick : ((Release) -> Unit)? = null
    var onListenLaterClick : ((Release) -> Unit)? = null
    var onTranscribeClick : ((Release) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewReleaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_new_release_item, parent, false)
        return NewReleaseViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewReleaseViewHolder, position: Int) {
        val releaseItem: Release = list[position]
        holder.bind(releaseItem)
    }

    override fun getItemCount(): Int = list.size

    fun add(listData: ListData<Release>) {
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

    inner class NewReleaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var mAvatar : ImageView? = null
        private var mAuthorName: TextView? = null
        private var mReleaseDate: TextView? = null
        private var mEpisodeTitle: TextView? = null
        private var mDuration: TextView? = null

        private var mQuickViewButton : MaterialIconView? = null
        private var mPlayButton : MaterialIconView? = null
        private var mListenLaterButton : MaterialIconView? = null
        private var mTranscribeButton : MaterialIconView? = null

        init {
            mAvatar = itemView.findViewById(R.id.releaseAvatar)
            mAuthorName = itemView.findViewById(R.id.authorNameTextView)
            mReleaseDate = itemView.findViewById(R.id.releaseDateTextView)
            mEpisodeTitle = itemView.findViewById(R.id.episodeTitleTextView)
            mDuration = itemView.findViewById(R.id.durationText)

            mQuickViewButton = itemView.findViewById(R.id.quickViewButton)
            mPlayButton = itemView.findViewById(R.id.playButton)
            mListenLaterButton = itemView.findViewById(R.id.listenLaterButton)
            mTranscribeButton = itemView.findViewById(R.id.transcribeButton)

            itemView.setOnClickListener {
                onItemClick?.invoke(list[absoluteAdapterPosition])
            }

            mEpisodeTitle?.setOnClickListener{
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
                onQuickViewClick?.invoke(list[absoluteAdapterPosition])
            }
        }

        fun bind(item: Release) {
            val picasso = Picasso.get()
            picasso.load(item.imageUrl)
                .placeholder(R.drawable.default_album_art)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .error(R.drawable.default_album_art)
                .into(mAvatar)

            mAuthorName?.text = item.authorName
            mReleaseDate?.text = getDateTime(item.releaseDate)
            mEpisodeTitle?.text = item.episodeTitle
            mDuration?.text = item.duration
        }

    }
}
