package com.blueshark.lightcast

import android.annotation.TargetApi
import android.content.*
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.blueshark.lightcast.helper.LocaleHelper
import com.blueshark.lightcast.listener.*
import com.blueshark.lightcast.model.Audio
import com.blueshark.lightcast.networking.*
import com.blueshark.lightcast.service.MusicPlayerRemote
import com.blueshark.lightcast.service.MusicService
import com.blueshark.lightcast.service.MusicServiceEventListener
import com.blueshark.lightcast.utils.PreferenceUtil
import com.blueshark.lightcast.utils.Util.getTimeFormatFromMillisecond
import org.json.JSONObject
import java.io.Reader
import java.lang.ref.WeakReference
import java.util.*

abstract class BaseActivity : AppCompatActivity(),
    MusicServiceEventListener {
    private val mMusicServiceEventListeners: ArrayList<MusicServiceEventListener> =
        ArrayList<MusicServiceEventListener>()
    private var serviceToken: MusicPlayerRemote.ServiceToken? = null
    private var musicStateReceiver: MusicStateReceiver? = null
    private var receiverRegistered = false

    private val mSubscribeEventListeners : ArrayList<AddSubscribeListener> = ArrayList<AddSubscribeListener>()
    private val mBookmarkEventListeners : ArrayList<AddBookmarkListener> = ArrayList<AddBookmarkListener>()
    private val mListenLaterEventListeners : ArrayList<AddListenLaterListener> = ArrayList<AddListenLaterListener>()
    private val mInProgressEventListeners : ArrayList<AddInprogressListener> = ArrayList<AddInprogressListener>()

    private val subscribeServiceCallback = object: RequestDataReceiver {
        override fun onDataReceived(jsonString: String) {
            val jsonObject = JSONObject(jsonString)
            val responseMessage = jsonObject.getString("message")
            val responseCode = jsonObject.getString("responseCode")
            showSnakbar(responseMessage)
            if (responseCode == "SUCCESS") {
                for (listener in mSubscribeEventListeners) {
                    listener.onSubscribeSuccess()
                }
            } else {
                for (listener in mSubscribeEventListeners) {
                    listener.onSubscribeFailed(responseMessage)
                }
            }

        }

        override fun onDataReceivedStream(reader: Reader) {

        }

        override fun onFailed(t: Throwable?) {
            showSnakbar("Failed to subscribe")
            for (listener in mSubscribeEventListeners) {
                listener.onSubscribeFailed(t?.message)
            }
        }
    }

    private val bookmarkServiceCallback = object: RequestDataReceiver {
        override fun onDataReceived(jsonString: String) {
            val jsonObject = JSONObject(jsonString)
            val responseMessage = jsonObject.getString("message")
            showSnakbar(responseMessage)
            for (listener in mBookmarkEventListeners) {
                listener.onBookmarkSuccess()
            }
        }

        override fun onDataReceivedStream(reader: Reader) {

        }

        override fun onFailed(t: Throwable?) {
            showSnakbar("Failed to add Bookmark")
            for (listener in mBookmarkEventListeners) {
                listener.onBookmarkFailed(t?.message)
            }
        }
    }

    private val listenLaterServiceCallback = object: RequestDataReceiver {
        override fun onDataReceived(jsonString: String) {
            val jsonObject = JSONObject(jsonString)
            val responseMessage = jsonObject.getString("message")
            val responseCode = jsonObject.getString("responseCode")
            showSnakbar(responseMessage)
            if (responseCode == "SUCCESS") {
                for (listener in mListenLaterEventListeners) {
                    listener.onListenLaterSuccess()
                }
            } else {
                for (listener in mListenLaterEventListeners) {
                    listener.onListenLaterFailed(responseMessage)
                }
            }
        }

        override fun onDataReceivedStream(reader: Reader) {

        }

        override fun onFailed(t: Throwable?) {
            showSnakbar("Failed to add Listen Later")
            for (listener in mListenLaterEventListeners) {
                listener.onListenLaterFailed(t?.message)
            }
        }
    }

    private val inProgressServiceCallback = object: RequestDataReceiver {
        override fun onDataReceived(jsonString: String) {
            val jsonObject = JSONObject(jsonString)
            val responseMessage = jsonObject.getString("message")
            val responseCode = jsonObject.getString("responseCode")
            showSnakbar(responseMessage)
            if (responseCode == "SUCCESS") {
                for (listener in mInProgressEventListeners) {
                    listener.onInprogressSuccess()
                }
            } else {
                for (listener in mInProgressEventListeners) {
                    listener.onInprogressFailed(responseMessage)
                }
            }
        }

        override fun onDataReceivedStream(reader: Reader) {

        }

        override fun onFailed(t: Throwable?) {
            for (listener in mInProgressEventListeners) {
                listener.onInprogressFailed(t?.message)
            }
        }
    }

    private val addBookmarkService = AddBookmarkService(bookmarkServiceCallback)
    private val addSubscriptionService = AddSubscribeService(subscribeServiceCallback)
    private val addListenLaterService = AddToListenLaterService(listenLaterServiceCallback)
    private val addInProgressService = AddInProgressService(inProgressServiceCallback)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(createBundleNoFragmentRestore(savedInstanceState))
        volumeControlStream = AudioManager.STREAM_MUSIC
        serviceToken = MusicPlayerRemote.bindToService(this, object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName,
                service: IBinder
            ) {
                this@BaseActivity.onServiceConnected()
            }

            override fun onServiceDisconnected(name: ComponentName) {
                this@BaseActivity.onServiceDisconnected()
            }
        })
    }

    override fun onDestroy() {
        if (!MusicPlayerRemote.isPlaying()) {
            PreferenceUtil.getInstance().playingState = "idle"
            PreferenceUtil.getInstance().playingSpeedIndex = 2
            PreferenceUtil.getInstance().showMiniController = false
        }
        MusicPlayerRemote.unbindFromService(serviceToken)
        if (receiverRegistered) {
            unregisterReceiver(musicStateReceiver)
            receiverRegistered = false
        }
        removeAllMusicServiceEventListener()

        removeAllBookmarkEventListener()
        removeAllInprogressEventListener()
        removeAllListenLaterEventListener()
        removeAllSubscribeEventListener()
        super.onDestroy()
    }

    fun addMusicServiceEventListener(listener: MusicServiceEventListener?) {
        if (listener != null) {
            mMusicServiceEventListeners.add(listener)
        }
    }

    fun removeMusicServiceEventListener(listener: MusicServiceEventListener?) {
        if (listener != null) {
            mMusicServiceEventListeners.remove(listener)
        }
    }

    fun removeAllMusicServiceEventListener() {
        mMusicServiceEventListeners.clear()
    }

    override fun onServiceConnected() {
        if (!receiverRegistered) {
            musicStateReceiver = MusicStateReceiver(this)
            val filter = IntentFilter()
            filter.addAction(MusicService.PLAY_STATE_CHANGED)
            filter.addAction(MusicService.SHUFFLE_MODE_CHANGED)
            filter.addAction(MusicService.REPEAT_MODE_CHANGED)
            filter.addAction(MusicService.META_CHANGED)
            filter.addAction(MusicService.QUEUE_CHANGED)
            filter.addAction(MusicService.MEDIA_STORE_CHANGED)
            filter.addAction(MusicService.DATA_SOURCE_INITIALIZED)
            filter.addAction(MusicService.PLAYER_QUIT)
            filter.addAction(MusicService.ADD_BOOKMARK)
            //filter.addAction(PaletteGeneratorTask.PALETTE_ACTION)
            registerReceiver(musicStateReceiver, filter)
            receiverRegistered = true
        }
        for (listener in mMusicServiceEventListeners) {
            listener.onServiceConnected()
        }
    }

    override fun onServiceDisconnected() {
        if (receiverRegistered) {
            unregisterReceiver(musicStateReceiver)
            receiverRegistered = false
        }
        for (listener in mMusicServiceEventListeners) {
            listener.onServiceDisconnected()
        }
        hideCastMiniController()
    }

    override fun onPlayingMetaChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onPlayingMetaChanged()
        }
        showCastMiniController()
    }

    override fun onQueueChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onQueueChanged()
        }
    }

    override fun onPlayStateChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onPlayStateChanged()
        }

        showCastMiniController()
    }

    override fun onMediaStoreChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onMediaStoreChanged()
        }
    }

    override fun onRepeatModeChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onRepeatModeChanged()
        }
    }

    override fun onShuffleModeChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onShuffleModeChanged()
        }
    }

    override fun onPaletteChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onPaletteChanged()
        }
    }

    override fun onDataSourceInitialized() {
        for (listener in mMusicServiceEventListeners) {
            listener.onDataSourceInitialized()
        }
        showCastMiniController()
    }

    override fun onPlayEnded() {
        hideCastMiniController()
    }

    abstract fun showCastMiniController()
    abstract fun hideCastMiniController()
    abstract fun showSnakbar(message : String)

    private class MusicStateReceiver(activity: BaseActivity) :
        BroadcastReceiver() {
        private val reference: WeakReference<BaseActivity> = WeakReference(activity)
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            val action = intent.action
            val activity = reference.get()
            if (activity != null && action != null) {
                when (action) {
                    MusicService.META_CHANGED -> activity.onPlayingMetaChanged()
                    MusicService.QUEUE_CHANGED -> activity.onQueueChanged()
                    MusicService.PLAY_STATE_CHANGED -> activity.onPlayStateChanged()
                    MusicService.REPEAT_MODE_CHANGED -> activity.onRepeatModeChanged()
                    MusicService.SHUFFLE_MODE_CHANGED -> activity.onShuffleModeChanged()
                    MusicService.MEDIA_STORE_CHANGED -> activity.onMediaStoreChanged()
                    MusicService.DATA_SOURCE_INITIALIZED -> activity.onDataSourceInitialized()
                    MusicService.PLAYER_QUIT -> activity.onPlayEnded()
                    MusicService.ADD_BOOKMARK -> activity.addBookmark(
                        MusicPlayerRemote.getCurrentSong().podcastId,
                        MusicPlayerRemote.getCurrentSong().episodeId,
                        getTimeFormatFromMillisecond(MusicPlayerRemote.getSongProgressMillis().toLong())
                    )
                }
            }
        }

    }

    fun addMusicServiceEventListener(
        listener: MusicServiceEventListener?,
        firstIndex: Boolean
    ) {
        if (listener === this) {
            throw UnsupportedOperationException("Override the method, don't add a listener")
        }
        if (listener != null) {
            mMusicServiceEventListeners.add(0, listener)
        }
    }

    fun addSubscribeEventListener(listener: AddSubscribeListener?) {
        if (listener == this) {
            throw UnsupportedOperationException("Override the method, don't add a listener")
        }
        if (listener != null) {
            mSubscribeEventListeners.add(listener)
        }
    }

    fun removeSubscribeEventListener(listener : AddSubscribeListener?) {
        mSubscribeEventListeners.remove(listener)
    }

    fun removeAllSubscribeEventListener() {
        mSubscribeEventListeners.clear()
    }

    fun addListenLaterEventListener(listener : AddListenLaterListener?) {
        if (listener == this) {
            throw UnsupportedOperationException("Override the method, don't add a listener")
        }
        if (listener != null) {
            mListenLaterEventListeners.add(listener)
        }
    }

    fun removeListenLaterEventListener(listener : AddListenLaterListener?) {
        mListenLaterEventListeners.remove(listener)
    }

    fun removeAllListenLaterEventListener() {
        mListenLaterEventListeners.clear()
    }

    fun addBookmarkEventListener(listener : AddBookmarkListener?) {
        if (listener == this) {
            throw UnsupportedOperationException("Override the method, don't add a listener")
        }
        if (listener != null) {
            mBookmarkEventListeners.add(listener)
        }
    }

    fun removeBookmarkEventListener(listener : AddBookmarkListener?) {
        mBookmarkEventListeners.remove(listener)
    }

    fun removeAllBookmarkEventListener() {
        mBookmarkEventListeners.clear()
    }

    fun addInprogressEventListener(listener: AddInprogressListener?) {
        if (listener == this) {
            throw UnsupportedOperationException("Override the method, don't add a listener")
        }
        if (listener != null) {
            mInProgressEventListeners.add(listener)
        }
    }

    fun removeInprogressEventListener(listener: AddInprogressListener?) {
        mInProgressEventListeners.remove(listener)
    }

    fun removeAllInprogressEventListener() {
        mInProgressEventListeners.clear()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(updateBaseContextLocale(base))
    }

    private fun updateBaseContextLocale(context: Context): Context {
        val language: String =
            LocaleHelper.getLanguage(context) // Helper method to get saved language from SharedPreferences
        val locale = Locale(language)
        Locale.setDefault(locale)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResourcesLocale(context, locale)
        } else updateResourcesLocaleLegacy(context, locale)
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResourcesLocale(
        context: Context,
        locale: Locale
    ): Context {
        val configuration =
            context.resources.configuration
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    private fun updateResourcesLocaleLegacy(
        context: Context,
        locale: Locale
    ): Context {
        val resources = context.resources
        val configuration = resources.configuration
        configuration.locale = locale
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }

    fun playEpisode(audio : Audio) {
        if (MusicPlayerRemote.isServiceConnected()) {
            MusicPlayerRemote.playSongWith(audio)
        }
    }

    fun addBookmark(podcastId : Long?, episodeId : String?, timestamp: String?){
        addBookmarkService.execute(podcastId, episodeId, timestamp)
    }

    fun addSubscription(podcastId : Long?) {
        addSubscriptionService.execute(podcastId)
    }

    fun addListenLater(podcastId: Long, episodeId: String) {
        addListenLaterService.execute(podcastId, episodeId)
    }

    fun addInProgress(podcastId: Long, episodeId: String, durationLeft : Double, duration : Double) {
        addInProgressService.execute(podcastId, episodeId, durationLeft, duration)
    }

    companion object {
        private const val TAG = "BaseActivity"

        /**
         * Improve bundle to prevent restoring of fragments.
         * @param bundle bundle container
         * @return improved bundle with removed "fragments parcelable"
         */
        private fun createBundleNoFragmentRestore(bundle: Bundle?): Bundle? {
            bundle?.remove("android:support:fragments")
            return bundle
        }
    }
}