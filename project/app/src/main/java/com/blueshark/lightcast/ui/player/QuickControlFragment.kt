package com.blueshark.lightcast.ui.player

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.Fragment
import com.blueshark.indicatorseekbar.IndicatorSeekBar
import com.blueshark.indicatorseekbar.OnBubbleTextChangeListener
import com.blueshark.indicatorseekbar.OnSeekChangeListener
import com.blueshark.indicatorseekbar.SeekParams
import com.blueshark.lightcast.BaseActivity
import com.blueshark.lightcast.R
import com.blueshark.lightcast.model.Audio
import com.blueshark.lightcast.networking.AddInProgressService
import com.blueshark.lightcast.service.MusicPlayerRemote
import com.blueshark.lightcast.service.MusicServiceEventListener
import com.blueshark.lightcast.ui.player.PlayerConst.BACKWARD_TIME
import com.blueshark.lightcast.ui.player.PlayerConst.FORWARD_TIME
import com.blueshark.lightcast.ui.player.widget.PlayPauseButton
import com.blueshark.lightcast.utils.Constants.ADD_IN_PROGRESS_INTERVAL
import com.blueshark.lightcast.utils.Constants.PLAYING_SPEED
import com.blueshark.lightcast.utils.ImageUtil
import com.blueshark.lightcast.utils.PreferenceUtil
import com.blueshark.lightcast.utils.Util.getTimeFormatFromMillisecond
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.steamcrafted.materialiconlib.MaterialIconView

class QuickControlFragment : Fragment(), MusicServiceEventListener, AdapterView.OnItemSelectedListener {

    private lateinit var mPlayPause : AppCompatImageButton
    private lateinit var mPlayPauseExpanded : PlayPauseButton
    private lateinit var playPauseWrapperExpanded : View
    private lateinit var mProgress : IndicatorSeekBar
    private lateinit var mSeekBar : IndicatorSeekBar
    private lateinit var mTitle : TextView
    private lateinit var mTitleExpanded : TextView
    private lateinit var mArtist : TextView
    private lateinit var mDurationTime: TextView
    private lateinit var mElapsedTime : TextView
    private lateinit var mExpandedElapsedTime : TextView

    private lateinit var mBlurredArt : ImageView
    private lateinit var mAlbumArt : ImageView
    private lateinit var mRewind : AppCompatImageButton
    private lateinit var mForward : AppCompatImageButton

    private lateinit var mExpandedForward : AppCompatImageView
    private lateinit var mExpandedBackward : AppCompatImageView
    private lateinit var mCloseButton : MaterialIconView

    private lateinit var mBookmark : MaterialIconView
    private lateinit var mExpandedBookmark : MaterialIconView

    private lateinit var mSpinner : AppCompatSpinner
    private lateinit var rootView: View

    private var counterUpdateProgressbar = 0
    private var duetoplaypause = false
    private var fragmentPaused = false

    private var isPlaying = false

    private lateinit var updateInProgressHandler : Handler
    private var counterUpdateInProgress = 0

    private var albumArtBitmap : Bitmap? = null

    private lateinit var spinnerArrayAdapter : ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_playback_controls, container, false)
        mPlayPause = rootView.findViewById(R.id.playButton)
        mPlayPauseExpanded = rootView.findViewById(R.id.playpause)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            mPlayPauseExpanded.setColor(requireContext().getColor(R.color.FlatWhite))
        } else {
            mPlayPauseExpanded.setColor(resources.getColor(R.color.FlatWhite))
        }
        playPauseWrapperExpanded = rootView.findViewById(R.id.playpausewrapper)
        mProgress = rootView.findViewById(R.id.song_progress_normal)
        mSeekBar = rootView.findViewById(R.id.song_progress)
        mTitle = rootView.findViewById(R.id.title)
        mArtist = rootView.findViewById(R.id.song_artist)
        mTitleExpanded = rootView.findViewById(R.id.song_title)
        mDurationTime = rootView.findViewById(R.id.song_duration)
        mElapsedTime = rootView.findViewById(R.id.elapsed_time)
        mExpandedElapsedTime = rootView.findViewById(R.id.song_elapsed_time)

        mExpandedForward = rootView.findViewById(R.id.expandedForward)
        mExpandedBackward = rootView.findViewById(R.id.expandedBackward)


        mBlurredArt = rootView.findViewById(R.id.blurredAlbumart)
        mAlbumArt = rootView.findViewById(R.id.album_art_nowplayingcard)
        mBookmark = rootView.findViewById(R.id.bookmark)
        mExpandedBookmark = rootView.findViewById(R.id.expandedBookmark)
        mRewind = rootView.findViewById(R.id.rewindButton)
        mForward = rootView.findViewById(R.id.forwardButton)

        mCloseButton = rootView.findViewById(R.id.closeButton)

        topContainer = rootView.findViewById(R.id.topContainer)

        mPlayPause.setOnClickListener(mPlayPauseListener)
        playPauseWrapperExpanded.setOnClickListener(mPlayPauseListener)

        mSeekBar.onBubbleTextChangeListener =
            OnBubbleTextChangeListener { progress -> getTimeFormatFromMillisecond(progress) }

        mProgress.onBubbleTextChangeListener =
            OnBubbleTextChangeListener { progress -> getTimeFormatFromMillisecond(progress) }

        mSeekBar.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams) {
                if (seekParams.fromUser) {
                    MusicPlayerRemote.seekTo(seekParams.progress)
                } else {

                }
            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar) {
            }
        }

        mProgress.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams) {
                if (seekParams.fromUser) {
                    MusicPlayerRemote.seekTo(seekParams.progress)
                } else {

                }
            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar) {
            }
        }

        mForward.setOnClickListener {
            val pos = MusicPlayerRemote.forward(FORWARD_TIME)
            mProgress.setProgress(pos.toFloat())
            mSeekBar.setProgress(pos.toFloat())
            mElapsedTime.text = getTimeFormatFromMillisecond(pos.toLong())
            mExpandedElapsedTime.text = getTimeFormatFromMillisecond(pos.toLong())
        }

        mRewind.setOnClickListener {
            val pos = MusicPlayerRemote.backward(BACKWARD_TIME)
            mProgress.setProgress(pos.toFloat())
            mSeekBar.setProgress(pos.toFloat())
            mElapsedTime.text = getTimeFormatFromMillisecond(pos.toLong())
            mExpandedElapsedTime.text = getTimeFormatFromMillisecond(pos.toLong())
        }

        mExpandedForward.setOnClickListener {
            val pos = MusicPlayerRemote.forward(FORWARD_TIME)
            mProgress.setProgress(pos.toFloat())
            mSeekBar.setProgress(pos.toFloat())
            mElapsedTime.text = getTimeFormatFromMillisecond(pos.toLong())
            mExpandedElapsedTime.text = getTimeFormatFromMillisecond(pos.toLong())
        }

        mExpandedBackward.setOnClickListener {
            val pos = MusicPlayerRemote.backward(BACKWARD_TIME)
            mProgress.setProgress(pos.toFloat())
            mSeekBar.setProgress(pos.toFloat())
            mElapsedTime.text = getTimeFormatFromMillisecond(pos.toLong())
            mExpandedElapsedTime.text = getTimeFormatFromMillisecond(pos.toLong())
        }

        mBookmark.setOnClickListener(mBookmarkClickListener)
        mExpandedBookmark.setOnClickListener(mBookmarkClickListener)

        mCloseButton.setOnClickListener {
            MusicPlayerRemote.quit()
        }

        (activity as BaseActivity?)?.addMusicServiceEventListener(this, true)

        updateInProgressHandler = Handler()

        mSpinner = rootView.findViewById(R.id.spinner)
        val playSpeeds = resources.getStringArray(R.array.player_speed)
        mSpinner.adapter = ArrayAdapter(
            requireContext(),
            R.layout.cell_play_speed_spinner,
            playSpeeds
        ).apply {
            setDropDownViewResource(R.layout.cell_spinner_dropdown)
        }

        mSpinner.onItemSelectedListener = this
        mSpinner.setSelection(2)
        return rootView
    }

    private val mBookmarkClickListener = View.OnClickListener{
        val currentPosition = MusicPlayerRemote.getSongProgressMillis()
        val timestamp = getTimeFormatFromMillisecond(currentPosition.toLong())
        (requireActivity() as BaseActivity).addBookmark(
            MusicPlayerRemote.getCurrentSong()?.podcastId,
            MusicPlayerRemote.getCurrentSong()?.episodeId,
            timestamp
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (MusicPlayerRemote.isPlaying()) {
            updateNowPlayingCard()
            updateState()
        } else {
            initialize(PreferenceUtil.getInstance().currentAudio)
        }
    }

    override fun onResume() {
        super.onResume()
        topContainer = rootView.findViewById(R.id.topContainer)
        fragmentPaused = false
        mProgress.postDelayed(mUpdateProgress, 100)
        //mSeekBar.postDelayed(mUpdateSeekBar, 10)
    }

    private val mPlayPauseListener = View.OnClickListener {
        if (MusicPlayerRemote.isServiceConnected()) {
            duetoplaypause = true
            if (isPlaying) {
                isPlaying = false
                mPlayPause.setImageDrawable(context?.getDrawable(R.drawable.ic_circle_white_back_play))
            } else {
                isPlaying = true
                mPlayPause.setImageDrawable(context?.getDrawable(R.drawable.ic_circle_white_back_pause))
            }
            GlobalScope.async {
                MusicPlayerRemote.playOrPause()
            }
        }
    }

    private fun updateNowPlayingCard() {
        if (MusicPlayerRemote.isServiceConnected() && MusicPlayerRemote.getCurrentSong() != null) {
            mTitle.text = MusicPlayerRemote.getCurrentSong().title
            mArtist.text = MusicPlayerRemote.getCurrentSong().author
            mTitleExpanded.text = MusicPlayerRemote.getCurrentSong().title
            mDurationTime.text = getTimeFormatFromMillisecond(MusicPlayerRemote.getSongDurationMillis().toLong())
            mElapsedTime.text = getTimeFormatFromMillisecond(MusicPlayerRemote.getSongProgressMillis().toLong())
            mExpandedElapsedTime.text = getTimeFormatFromMillisecond(MusicPlayerRemote.getSongProgressMillis().toLong())

            mSpinner.setSelection(PreferenceUtil.getInstance().playingSpeedIndex)

            if (!duetoplaypause) {

                val defaultBack = BitmapFactory.decodeResource(
                    resources,
                    R.drawable.default_album_art
                )
                if (albumArtBitmap == null) {
                    Picasso.get().load(MusicPlayerRemote.getCurrentSong().thumbnail)
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .into(object : Target {
                            override fun onBitmapFailed(
                                e: java.lang.Exception?,
                                errorDrawable: Drawable?
                            ) {
                                mAlbumArt.setImageBitmap(defaultBack)
                                setBlurredAlbumArt(defaultBack)
                            }

                            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                                //mAlbumArt.setImageBitmap(defaultBack)
                                setBlurredAlbumArt(defaultBack)
                            }

                            override fun onBitmapLoaded(
                                bitmap: Bitmap?,
                                from: Picasso.LoadedFrom?
                            ) {
                                albumArtBitmap = bitmap
                                //mAlbumArt.setImageBitmap(bitmap)
                                setBlurredAlbumArt(bitmap!!)
                            }
                        })
                } else {
                    setBlurredAlbumArt(albumArtBitmap!!)
                }
            }
            duetoplaypause = false
            mProgress.max = MusicPlayerRemote.getSongDurationMillis().toFloat()
            mSeekBar.max = MusicPlayerRemote.getSongDurationMillis().toFloat()
            mProgress.postDelayed(mUpdateProgress, 100)
            //mSeekBar.postDelayed(mUpdateSeekBar, 10)
        } else {
            initialize(PreferenceUtil.getInstance().currentAudio)
        }
    }

    private var mUpdateSeekBar: Runnable = object : Runnable {
        override fun run() {
            if (MusicPlayerRemote.isServiceConnected()) {
                val position = MusicPlayerRemote.getSongProgressMillis().toLong()
                mProgress.setProgress(position.toFloat())
                mElapsedTime.text = getTimeFormatFromMillisecond(position)
                mExpandedElapsedTime.text = getTimeFormatFromMillisecond(position)
                counterUpdateProgressbar--
                if (MusicPlayerRemote.isPlaying()) {
                    val delay = (1500 - position % 1000)
                    if (counterUpdateProgressbar < 0 && !fragmentPaused) {
                        MusicPlayerRemote.updateNotification()
                        counterUpdateProgressbar++
                        mSeekBar.postDelayed(this, delay)
                    }
                } else {
                    mSeekBar.removeCallbacks(this)
                }
            }
        }
    }

    private var mUpdateProgress: Runnable = object : Runnable {
        override fun run() {
            if (MusicPlayerRemote.isServiceConnected()) {
                val position = MusicPlayerRemote.getSongProgressMillis().toLong()
                mSeekBar.setProgress(position.toFloat())
                mProgress.setProgress(position.toFloat())
                mElapsedTime.text = getTimeFormatFromMillisecond(position)
                mExpandedElapsedTime.text = getTimeFormatFromMillisecond(position)
                counterUpdateProgressbar--
                if (MusicPlayerRemote.isPlaying()) {
                    val delay = (1500 - position % 1000)
                    if (counterUpdateProgressbar < 0 && !fragmentPaused) {
                        MusicPlayerRemote.updateNotification()
                        counterUpdateProgressbar++
                        mProgress.postDelayed(this, delay)
                    }
                } else {
                    mProgress.removeCallbacks(this)
                }
            }
        }
    }

    private fun setBlurredAlbumArt(bitmap: Bitmap) {
        val drawable = ImageUtil.createBlurredImageFromBitmap(bitmap, activity, 1)
        if (mBlurredArt.drawable != null) {
            val td = TransitionDrawable(
                arrayOf(
                    mBlurredArt.drawable,
                    drawable
                )
            )
            mBlurredArt.setImageDrawable(td)
            td.startTransition(100)
        } else {
            mBlurredArt.setImageDrawable(drawable)
        }
    }


    private fun updateState() {
        if (MusicPlayerRemote.isServiceConnected()) {
            if (MusicPlayerRemote.isPlaying()) {
                if (!isPlaying) {
                    isPlaying = true
                    mPlayPause.setImageDrawable(context?.getDrawable(R.drawable.ic_circle_white_back_pause))
                }
                if (!mPlayPauseExpanded.isPlayed) {
                    mPlayPauseExpanded.isPlayed = true
                    mPlayPauseExpanded.startAnimation()
                }
            } else {
                if (isPlaying) {
                    isPlaying = false
                    mPlayPause.setImageDrawable(context?.getDrawable(R.drawable.ic_circle_white_back_play))
                }
                if (mPlayPauseExpanded.isPlayed) {
                    mPlayPauseExpanded.isPlayed = false
                    mPlayPauseExpanded.startAnimation()
                }
            }
        }
    }

    private fun initialize(audio: Audio?) {
        if (audio != null) {
            mTitle.text = audio.title
            mTitleExpanded.text = audio.title
            mArtist.text = audio.author

            mDurationTime.text = getTimeFormatFromMillisecond(audio.durationInMilliSeconds?.toLong() ?: 0)
            mElapsedTime.text = getTimeFormatFromMillisecond(audio.lastPlayTime?.toLong() ?: 0)
            mExpandedElapsedTime.text = getTimeFormatFromMillisecond(audio.lastPlayTime?.toLong() ?: 0)

            val playSpeed = PreferenceUtil.getInstance().playingSpeedIndex
            mSpinner.setSelection(playSpeed)

            if (!duetoplaypause) {
                val defaultBack = BitmapFactory.decodeResource(
                    resources,
                    R.drawable.default_album_art
                )
                Picasso.get().load(audio.thumbnail)
                    .resize(256, 256)
                    .placeholder(R.drawable.default_album_art)
                    .into(object: Target{
                        override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {
                            mAlbumArt.setImageBitmap(defaultBack)
                            setBlurredAlbumArt(defaultBack)
                        }

                        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                            mAlbumArt.setImageBitmap(defaultBack)
                            setBlurredAlbumArt(defaultBack)
                        }

                        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                            albumArtBitmap = bitmap
                            mAlbumArt.setImageBitmap(bitmap)
                            setBlurredAlbumArt(bitmap!!)
                        }
                    })

//                Picasso.get().load(audio.thumbnail)
//                    .resize(60, 60)
//                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
//                    .placeholder(R.drawable.ic_empty_music2)
//                    .into(mAlbumArt)
            }
            duetoplaypause = false
            mProgress.max = audio.durationInMilliSeconds?.toFloat() ?: 0f
            mSeekBar.max = audio.durationInMilliSeconds?.toFloat() ?: 0f
            mProgress.setProgress(audio.lastPlayTime?.toFloat() ?: 0f)
            mSeekBar.setProgress(audio.lastPlayTime?.toFloat() ?: 0f)
            mProgress.postDelayed(mUpdateProgress, 100)
        }

        isPlaying = when(PreferenceUtil.getInstance().playingState) {
            "play" -> MusicPlayerRemote.isPlaying()
            "pause" -> false
            else -> false
        }

        if (isPlaying) {
            mPlayPause.setImageDrawable(context?.getDrawable(R.drawable.ic_circle_white_back_pause))
            mPlayPauseExpanded.isPlayed = true
            mPlayPauseExpanded.startAnimation()
        } else {
            mPlayPause.setImageDrawable(context?.getDrawable(R.drawable.ic_circle_white_back_play))
            mPlayPauseExpanded.isPlayed = false
            mPlayPauseExpanded.startAnimation()
        }
    }

    private val addInProgressService = AddInProgressService(null)

    private val updateInProgressTask = object : Runnable {
        override fun run() {
            if (MusicPlayerRemote.isServiceConnected()) {
                val song = MusicPlayerRemote.getCurrentSong()
                if (song != null) {
                    addInProgressService.execute(
                        podcastId = song.podcastId!!,
                        episodeId = song.episodeId!!,
                        durationLeft = MusicPlayerRemote.getSongProgressMillis() / 1000.0,
                        duration = MusicPlayerRemote.getSongDurationMillis()/ 1000.0
                    )
                }

                counterUpdateInProgress--
                if (MusicPlayerRemote.isPlaying()) {
                    if (counterUpdateInProgress < 0) {
                        counterUpdateInProgress++
                        updateInProgressHandler.postDelayed(this, ADD_IN_PROGRESS_INTERVAL)
                    }
                } else {
                    updateInProgressHandler.removeCallbacks(this)
                }
            }
        }
    }
    override fun onPlayStateChanged() {
        Log.i(this.javaClass.simpleName, "onPlayStateChanged called *********************************************")
        updateNowPlayingCard()
        updateState()
//        GlobalScope.launch {
            updateInProgressHandler.postDelayed(updateInProgressTask, 200)
//        }
    }

    override fun onPlayingMetaChanged() {
        Log.i(this.javaClass.simpleName, "onPlayingMetaChanged called *********************************************")
        updateNowPlayingCard()
//        GlobalScope.launch {
//        if (MusicPlayerRemote.isPlaying())
//            updateInProgressHandler.removeCallbacks(updateInProgressTask)
//            updateInProgressHandler.postDelayed(updateInProgressTask, 200)
//        }
//        if (MusicPlayerRemote.isServiceConnected()) {
//            isPlaying = true
//            mPlayPause.setImageDrawable(context?.getDrawable(R.drawable.play_control_pause))
//
//            if (!mPlayPauseExpanded.isPlayed) {
//                mPlayPauseExpanded.isPlayed = true
//                mPlayPauseExpanded.startAnimation()
//            }
//        }
    }

    override fun onQueueChanged() {
    }

    override fun onMediaStoreChanged() {
    }

    override fun onRepeatModeChanged() {
    }

    override fun onPaletteChanged() {
    }

    override fun onShuffleModeChanged() {
    }

    override fun onServiceDisconnected() {
        Log.d(
            QuickControlFragment.javaClass.name,
            "onServiceDisconnected"
        )
    }

    override fun onServiceConnected() {

    }

    override fun onDataSourceInitialized() {
        //val currentAudio = MusicPlayerRemote.getCurrentSong()
        val currentAudio = PreferenceUtil.getInstance().currentAudio
        initialize(currentAudio)
    }

    override fun onPlayEnded() {

    }

    companion object{
        var topContainer : View? = null
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        PreferenceUtil.getInstance().playingSpeedIndex = position
        MusicPlayerRemote.setSpeed(PLAYING_SPEED[position])
    }
}