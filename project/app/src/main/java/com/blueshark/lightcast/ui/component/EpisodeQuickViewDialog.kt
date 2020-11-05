package com.blueshark.lightcast.ui.component

import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.blueshark.lightcast.BaseActivity
import com.blueshark.lightcast.MainActivity
import com.blueshark.lightcast.R
import com.blueshark.lightcast.listener.RequestDataReceiver
import com.blueshark.lightcast.model.Episode
import com.blueshark.lightcast.model.EpisodeRequestParam
import com.blueshark.lightcast.networking.EpisodeOverviewService
import com.blueshark.lightcast.utils.Util.getDateTime
import com.google.gson.Gson
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_quickview.view.*
import org.json.JSONObject
import java.io.Reader
import java.lang.Exception

fun showEpisodeDialog(context: Context?, viewLifecycleOwner : LifecycleOwner, viewGroup: ViewGroup?, episodeRequestParam: EpisodeRequestParam) {
    val mutableLiveData: MutableLiveData<Episode> = MutableLiveData()

    val dialogView: View = LayoutInflater.from(context).inflate(R.layout.dialog_quickview, viewGroup, false)
    val builder: AlertDialog.Builder = AlertDialog.Builder(context!!)

    builder.setView(dialogView)
    val alertDialog: AlertDialog = builder.create()
    val episodeOverviewService = EpisodeOverviewService()

    dialogView.loadingProgressbar.show()
    episodeOverviewService.fetchData(episodeRequestParam, object : RequestDataReceiver {
        override fun onDataReceived(jsonString: String) {
            val gson = Gson()
            try {
                val episode = gson.fromJson(jsonString, Episode::class.java)
                mutableLiveData.postValue(episode)
            } catch (e : Exception) {
                e.printStackTrace()
                mutableLiveData.postValue(null)
            }
        }

        override fun onDataReceivedStream(reader: Reader) {
            val gson = Gson()
            try {
                val episode = gson.fromJson(reader, Episode::class.java)
                mutableLiveData.postValue(episode)
            } catch (e : Exception) {
                e.printStackTrace()
                mutableLiveData.postValue(null)
            }
        }
        override fun onFailed(t: Throwable?) {
            mutableLiveData.postValue(null)
        }
    })

    mutableLiveData.observe(viewLifecycleOwner, Observer<Episode?> {
        if (it != null) {
            val picasso = Picasso.get()
            picasso.load(it.thumbnail)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .placeholder(R.drawable.default_album_art)
                .error(R.drawable.default_album_art)
                .into(dialogView.avatarImageView)
            dialogView.episodeNameTextView?.text = it.title
            dialogView.podcastNameTextView?.text = it.podcastName
            dialogView.releaseDateTextView?.text = getDateTime(it.releaseDate)
            dialogView.durationTextView?.text = it.duration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dialogView.summaryTextView?.text =
                    Html.fromHtml(it.summary, Html.FROM_HTML_MODE_COMPACT);
            } else {
                dialogView.summaryTextView?.text = Html.fromHtml(it.summary);
            }
        }
        dialogView.loadingProgressbar.hide()
    })
    dialogView.closeButton.setOnClickListener {
        alertDialog.dismiss()
    }

    dialogView.listenLaterIconButton.setOnClickListener {
        (context as BaseActivity).addListenLater(episodeRequestParam.podcastId, episodeRequestParam.episodeId)
    }
    alertDialog.show()
}

private fun updateDialogUI(dialogView : View, episode: Episode) {
    val picasso = Picasso.get()
    picasso.load(episode.thumbnail)
        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
        .placeholder(R.drawable.default_album_art)
        .error(R.drawable.default_album_art)
        .into(dialogView.avatarImageView)
    dialogView.episodeNameTextView.text = episode.title
    dialogView.podcastNameTextView.text = episode.podcastName
    dialogView.releaseDateTextView.text = getDateTime(episode.releaseDate)
    dialogView.durationTextView.text = episode.duration
    dialogView.summaryTextView.text = episode.description
}

