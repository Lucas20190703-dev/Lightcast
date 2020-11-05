package com.blueshark.lightcast.ui.bookmark

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blueshark.lightcast.R
import com.blueshark.lightcast.model.Bookmark
import com.blueshark.lightcast.model.ListData
import com.blueshark.lightcast.utils.Util.getDateTime
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import net.steamcrafted.materialiconlib.MaterialIconView
import kotlin.Comparator
import kotlin.collections.ArrayList

class BookmarkAdapter(var list: ArrayList<Bookmark>)
    : RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder>() {

    var onItemClick: ((Bookmark) -> Unit)? = null
    var onEpisodeClick: ((Bookmark) -> Unit)? = null

    var onQuickViewClick : ((Bookmark) -> Unit)? = null
    var onPlayClick : ((Bookmark) -> Unit)? = null
    var onListenLaterClick : ((Bookmark) -> Unit)? = null
    var onTranscribeClick : ((Bookmark) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_bookmark_item, parent, false)
        return BookmarkViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        val bookmarkItem: Bookmark = list[position]
        holder.bind(bookmarkItem)
    }

    override fun getItemCount(): Int = list.size

    fun add(listData: ListData<Bookmark>) {
        val prevCount = itemCount
        if (prevCount == 0 || prevCount > listData.offset) {
            list.clear()
            list.addAll(listData.data)
            list.sortWith(Comparator { lhs, rhs -> rhs?.createdDate!!.compareTo(lhs?.createdDate!!) })
            notifyDataSetChanged()
        } else {
            list.addAll(listData.data)
            list.sortWith(Comparator { lhs, rhs -> rhs?.createdDate!!.compareTo(lhs?.createdDate!!) })
            notifyItemRangeInserted(prevCount, listData.data.size)
        }
    }

    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    inner class BookmarkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var mAvatar : ImageView? = null
        private var mEpisodeName: TextView? = null
        private var mReleaseDate: TextView? = null
        private var mSegment : TextView? = null
        private var mBookedTime : TextView? = null
        private var mBookedDate : TextView? = null

        private var mQuickViewButton : MaterialIconView? = null
        private var mPlayButton : MaterialIconView? = null
        private var mListenLaterButton : MaterialIconView? = null
        private var mTranscribeButton : MaterialIconView? = null

        init {
            mAvatar = itemView.findViewById(R.id.bookmarkAvatar)
            mEpisodeName = itemView.findViewById(R.id.episodeNameTextView)
            mSegment = itemView.findViewById(R.id.segmentTextView)
            mReleaseDate = itemView.findViewById(R.id.releaseDateTextView)
            mBookedTime = itemView.findViewById(R.id.bookedTimeTextView)
            mBookedDate = itemView.findViewById(R.id.bookedDateTextView)
            mQuickViewButton = itemView.findViewById(R.id.quickViewButton)
            mPlayButton = itemView.findViewById(R.id.playButton)
            mListenLaterButton = itemView.findViewById(R.id.listenLaterButton)
            mTranscribeButton = itemView.findViewById(R.id.transcribeButton)

            mAvatar?.setOnClickListener {
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
                onQuickViewClick?.invoke(list[absoluteAdapterPosition])
            }
        }

        fun bind(item: Bookmark) {
            val picasso = Picasso.get()
            picasso.load(item.image)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .placeholder(R.drawable.default_album_art)
                .error(R.drawable.default_album_art)
                .into(mAvatar)
            mEpisodeName?.text = item.episodeName
            mSegment?.text = item.segment
            mReleaseDate?.text = getDateTime(item.releaseDate)
            mBookedTime?.text = item.timestamp
            mBookedDate?.text = getDateTime(item.createdDate)
        }
    }
}
