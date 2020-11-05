package com.blueshark.lightcast.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blueshark.lightcast.R
import com.blueshark.lightcast.model.ListData
import com.blueshark.lightcast.model.PodcastOverview
import com.blueshark.lightcast.model.TopPodcast
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso


class SearchSuggestAdapter(var list : ArrayList<PodcastOverview>)
    : RecyclerView.Adapter<SearchSuggestAdapter.SearchSuggestViewHolder>() {

    var onItemClick: ((TopPodcast) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchSuggestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_search_suggester_item, parent, false)
        return SearchSuggestViewHolder(view)
    }

    override fun getItemCount() : Int = list.size

    override fun onBindViewHolder(holder: SearchSuggestViewHolder, position: Int) {
        val podcastItem: PodcastOverview = list[position]
        holder.bind(podcastItem)
    }

    fun add(listData : ListData<PodcastOverview>) {
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

    inner class SearchSuggestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var mAvatar : ImageView? = null
        private var mTitle : TextView? = null

        init {
            mAvatar = itemView.findViewById(R.id.avatarImageView)
            mTitle = itemView.findViewById(R.id.podcastTitleTextView)

            itemView.setOnClickListener {
                onItemClick?.invoke(list[adapterPosition].topPodcast)
            }
        }

        fun bind(item : PodcastOverview) {
            Picasso.get().load(item.topPodcast.thumbnail)
                .resize(60, 60)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .placeholder(R.drawable.default_album_art)
                .into(mAvatar)
            mTitle?.text = item.topPodcast.name
        }
    }
}