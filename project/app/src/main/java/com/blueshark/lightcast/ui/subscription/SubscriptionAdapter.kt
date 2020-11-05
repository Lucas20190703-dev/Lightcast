package com.blueshark.lightcast.ui.subscription

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.blueshark.lightcast.R
import com.blueshark.lightcast.model.ListData
import com.blueshark.lightcast.model.Subscription
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso


class SubscriptionAdapter(var list: ArrayList<Subscription>)
    : RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder>() {

    var onItemClick: ((Subscription) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_subscription_item, parent, false)
        val params = view.layoutParams
        params.height = parent.measuredWidth / 3
        params.width = parent.measuredWidth / 3
        view.layoutParams = params
        return SubscriptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int) {
        val inProgressItem: Subscription = list[position]
        holder.bind(inProgressItem)
    }


    override fun getItemCount(): Int = list.size

    fun add(listData: ListData<Subscription>) {
        list.clear()
        list.addAll(listData.data)
        notifyDataSetChanged()
    }

    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    inner class SubscriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var mAvatar : ImageView? = null

        init {
            mAvatar = itemView.findViewById(R.id.subscriptionAvatar)
            itemView.setOnClickListener {
                onItemClick?.invoke(list[adapterPosition])
            }
        }

        fun bind(item: Subscription) {
            val picasso = Picasso.get()
            picasso.load(item.img)
                .placeholder(R.drawable.default_album_art)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .error(R.drawable.default_album_art)
                .into(mAvatar)
        }
    }
}