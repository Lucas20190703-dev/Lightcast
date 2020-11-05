package com.blueshark.lightcast.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import com.blueshark.lightcast.App;
import com.blueshark.lightcast.model.Audio;
import com.blueshark.lightcast.model.User;
import com.google.gson.Gson;

public final class PreferenceUtil {
    private static final String SONG_CHILD_SORT_ORDER = "song_child_sort_order";
    public static final String GENERAL_THEME = "general_theme";
    public static final String REMEMBER_LAST_TAB = "remember_last_tab";
    public static final String LAST_PAGE = "last_start_page";
    public static final String LAST_MUSIC_CHOOSER = "last_music_chooser";
    public static final String NOW_PLAYING_SCREEN_ID = "now_playing_screen_id";

    public static final String ARTIST_SORT_ORDER = "artist_sort_order";
    public static final String ARTIST_SONG_SORT_ORDER = "artist_song_sort_order";
    public static final String ARTIST_ALBUM_SORT_ORDER = "artist_album_sort_order";
    public static final String ALBUM_SORT_ORDER = "album_sort_order";
    public static final String ALBUM_SONG_SORT_ORDER = "album_song_sort_order";
    public static final String SONG_SORT_ORDER = "song_sort_order";
    public static final String GENRE_SORT_ORDER = "genre_sort_order";

    public static final String ALBUM_GRID_SIZE = "album_grid_size";
    public static final String ALBUM_GRID_SIZE_LAND = "album_grid_size_land";

    public static final String SONG_GRID_SIZE = "song_grid_size";
    public static final String SONG_GRID_SIZE_LAND = "song_grid_size_land";

    public static final String ARTIST_GRID_SIZE = "artist_grid_size";
    public static final String ARTIST_GRID_SIZE_LAND = "artist_grid_size_land";

    public static final String ALBUM_COLORED_FOOTERS = "album_colored_footers";
    public static final String SONG_COLORED_FOOTERS = "song_colored_footers";
    public static final String ARTIST_COLORED_FOOTERS = "artist_colored_footers";
    public static final String ALBUM_ARTIST_COLORED_FOOTERS = "album_artist_colored_footers";

    public static final String FORCE_SQUARE_ALBUM_COVER = "force_square_album_art";

    public static final String COLORED_NOTIFICATION = "colored_notification";
    public static final String CLASSIC_NOTIFICATION = "classic_notification";

    public static final String COLORED_APP_SHORTCUTS = "colored_app_shortcuts";

    public static final String AUDIO_DUCKING = "audio_ducking";
    public static final String GAPLESS_PLAYBACK = "gapless_playback";

    public static final String LAST_ADDED_CUTOFF = "last_added_interval";
    public static final String RECENTLY_PLAYED_CUTOFF = "recently_played_interval";

    public static final String ALBUM_ART_ON_LOCKSCREEN = "album_art_on_lockscreen";
    public static final String BLURRED_ALBUM_ART = "blurred_album_art";

    public static final String LAST_SLEEP_TIMER_VALUE = "last_sleep_timer_value";
    public static final String NEXT_SLEEP_TIMER_ELAPSED_REALTIME = "next_sleep_timer_elapsed_real_time";
    public static final String SLEEP_TIMER_FINISH_SONG = "sleep_timer_finish_music";

    public static final String IGNORE_MEDIA_STORE_ARTWORK = "ignore_media_store_artwork";

    public static final String LAST_CHANGELOG_VERSION = "last_changelog_version";
    public static final String INTRO_SHOWN = "intro_shown";

    public static final String AUTO_DOWNLOAD_IMAGES_POLICY = "auto_download_images_policy";

    public static final String START_DIRECTORY = "start_directory";

    public static final String SYNCHRONIZED_LYRICS_SHOW = "synchronized_lyrics_show";

    public static final String INITIALIZED_BLACKLIST = "initialized_blacklist";

    public static final String LIBRARY_CATEGORIES = "library_categories";

    private static final String REMEMBER_SHUFFLE = "remember_shuffle";

    private static final String USE_ARTIST_IMAGE_AS_BACKGROUND = "use_artist_image_as_bg";
    public static final String IN_APP_VOLUME = "in_app_volume";
    private static final String AUDIO_MIN_DURATION = "audio_min_duration";
    public static final String BALANCE_VALUE = "balance_value";
    public static final String THREAD_NUMBER = "thread_number";

    public static final String CURRENT_AUDIO = "current_episode";

    //user
    public static final String USER_AUTHENTICATION_STATE = "authentication_state";
    public static final String USER_INFO_KEY = "user";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String USER_ID = "userId";
    public static final String CURRENT_PLAY_EPISODE = "current_play_episode";
    public static final String SHOW_MINI_CONTROLLER = "is_show_mini_controller";
    public static final String PLAYING_STATE = "current_play_state";

    public static final String PLAYING_SPEED_INDEX = "current_play_speed";

    private static PreferenceUtil sInstance;

    public boolean isFirstTime() {
        return isFirstTime;
    }

    public void setFirstTime(boolean firstTime) {
        isFirstTime = firstTime;
    }

    private boolean isFirstTime = true;

    private final SharedPreferences mPreferences;

    private PreferenceUtil(@NonNull final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PreferenceUtil getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new PreferenceUtil(App.Companion.getInstance().getApplicationContext());
        }
        return sInstance;
    }
    public static PreferenceUtil getInstance() {
        if (sInstance == null) {
            sInstance = new PreferenceUtil(App.Companion.getInstance().getApplicationContext());
        }
        return sInstance;
    }

    public static boolean isAllowedToDownloadMetadata(final Context context) {
        switch (getInstance(context).autoDownloadImagesPolicy()) {
            case "always":
                return true;
            case "only_wifi":
                final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
                return netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI && netInfo.isConnectedOrConnecting();
            case "never":
            default:
                return false;
        }
    }

    public void registerOnSharedPreferenceChangedListener(SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener) {
        mPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    public void unregisterOnSharedPreferenceChangedListener(SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener) {
        mPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }


    public void setGeneralTheme(String theme) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(GENERAL_THEME, theme);
        editor.commit();
    }


    public final boolean rememberLastTab() {
        return mPreferences.getBoolean(REMEMBER_LAST_TAB, true);
    }

    public void setLastPage(final int value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(LAST_PAGE, value);
        editor.apply();
    }

    public final int getLastPage() {
        return mPreferences.getInt(LAST_PAGE, 0);
    }

    public void setLastMusicChooser(final int value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(LAST_MUSIC_CHOOSER, value);
        editor.apply();
    }

    public final int getLastMusicChooser() {
        return mPreferences.getInt(LAST_MUSIC_CHOOSER, 0);
    }



    public final boolean coloredNotification() {
        return mPreferences.getBoolean(COLORED_NOTIFICATION, true);
    }

    public final boolean classicNotification() {
        return mPreferences.getBoolean(CLASSIC_NOTIFICATION, false);
    }

    public void setColoredNotification(final boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(COLORED_NOTIFICATION, value);
        editor.apply();
    }

    public void setClassicNotification(final boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(CLASSIC_NOTIFICATION, value);
        editor.apply();
    }

    public void setColoredAppShortcuts(final boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(COLORED_APP_SHORTCUTS, value);
        editor.apply();
    }

    public final boolean coloredAppShortcuts() {
        return mPreferences.getBoolean(COLORED_APP_SHORTCUTS, true);
    }

    public final boolean gaplessPlayback() {
        return mPreferences.getBoolean(GAPLESS_PLAYBACK, false);
    }

    public final boolean audioDucking() {
        return mPreferences.getBoolean(AUDIO_DUCKING, true);
    }

    public final boolean albumArtOnLockscreen() {
        return mPreferences.getBoolean(ALBUM_ART_ON_LOCKSCREEN, true);
    }

    public final boolean blurredAlbumArt() {
        return mPreferences.getBoolean(BLURRED_ALBUM_ART, false);
    }

    public final boolean ignoreMediaStoreArtwork() {
        return mPreferences.getBoolean(IGNORE_MEDIA_STORE_ARTWORK, false);
    }

    public final String getArtistSortOrder() {
        return mPreferences.getString(ARTIST_SORT_ORDER, SortOrder.ArtistSortOrder.ARTIST_A_Z);
    }

    public void setArtistSortOrder(final String sortOrder) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(ARTIST_SORT_ORDER, sortOrder);
        editor.commit();
    }

    public final String getArtistSongSortOrder() {
        return mPreferences.getString(ARTIST_SONG_SORT_ORDER, SortOrder.ArtistSongSortOrder.SONG_A_Z);
    }

    public final String getArtistAlbumSortOrder() {
        return mPreferences.getString(ARTIST_ALBUM_SORT_ORDER, SortOrder.ArtistAlbumSortOrder.ALBUM_YEAR);
    }

    public final String getAlbumSortOrder() {
        return mPreferences.getString(ALBUM_SORT_ORDER, SortOrder.AlbumSortOrder.ALBUM_A_Z);
    }

    public void setAlbumSortOrder(final String sortOrder) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(ALBUM_SORT_ORDER, sortOrder);
        editor.commit();
    }

    public final String getAlbumSongSortOrder() {
        return mPreferences.getString(ALBUM_SONG_SORT_ORDER, SortOrder.AlbumSongSortOrder.SONG_TRACK_LIST);
    }

    public final String getSongSortOrder() {
        return mPreferences.getString(SONG_SORT_ORDER, SortOrder.SongSortOrder.SONG_A_Z);
    }

    public void setSongSortOrder(final String sortOrder) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(SONG_SORT_ORDER, sortOrder);
        editor.commit();
    }


    public int getLastSleepTimerValue() {
        return mPreferences.getInt(LAST_SLEEP_TIMER_VALUE, 30);
    }

    public void setLastSleepTimerValue(final int value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(LAST_SLEEP_TIMER_VALUE, value);
        editor.apply();
    }

    public long getNextSleepTimerElapsedRealTime() {
        return mPreferences.getLong(NEXT_SLEEP_TIMER_ELAPSED_REALTIME, -1);
    }

    public void setNextSleepTimerElapsedRealtime(final long value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(NEXT_SLEEP_TIMER_ELAPSED_REALTIME, value);
        editor.apply();
    }

    public boolean getSleepTimerFinishMusic() {
        return mPreferences.getBoolean(SLEEP_TIMER_FINISH_SONG, false);
    }

    public void setSleepTimerFinishMusic(final boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(SLEEP_TIMER_FINISH_SONG, value);
        editor.apply();
    }

    public void setAlbumGridSize(final int gridSize) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(ALBUM_GRID_SIZE, gridSize);
        editor.apply();
    }



    public void setLastChangeLogVersion(int version) {
        mPreferences.edit().putInt(LAST_CHANGELOG_VERSION, version).apply();
    }

    public final int getLastChangelogVersion() {
        return mPreferences.getInt(LAST_CHANGELOG_VERSION, -1);
    }



    @SuppressLint("CommitPrefEdits")
    public void setIntroShown() {
        // don't use apply here
        mPreferences.edit().putBoolean(INTRO_SHOWN, true).commit();
    }

    public final boolean introShown() {
        return mPreferences.getBoolean(INTRO_SHOWN, false);
    }

    public final boolean rememberShuffle() {
        return mPreferences.getBoolean(REMEMBER_SHUFFLE, true);
    }

    public final String autoDownloadImagesPolicy() {
        return mPreferences.getString(AUTO_DOWNLOAD_IMAGES_POLICY, "only_wifi");
    }


    public final boolean synchronizedLyricsShow() {
        return mPreferences.getBoolean(SYNCHRONIZED_LYRICS_SHOW, true);
    }

    public void setInitializedBlacklist() {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(INITIALIZED_BLACKLIST, true);
        editor.apply();
    }

    public final boolean initializedBlacklist() {
        return mPreferences.getBoolean(INITIALIZED_BLACKLIST, false);
    }


    public final int getSongChildSortOrder() {
        return mPreferences.getInt(SONG_CHILD_SORT_ORDER,1);
    }

    public final void setSongChildSortOrder(int value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(SONG_CHILD_SORT_ORDER, value);
        editor.apply();
    }

    public final void setIsUsingArtistImageAsBackground(boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(USE_ARTIST_IMAGE_AS_BACKGROUND,value);
        editor.apply();;
    }

    public final boolean isUsingArtistImageAsBackground() {
        return mPreferences.getBoolean(USE_ARTIST_IMAGE_AS_BACKGROUND,true);
    }

    public final void setInAppVolume(float value) {
        if(value<0) value = 0;
        else if(value>1) value = 1;
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putFloat(IN_APP_VOLUME, value);
        editor.apply();
    }

    public final float getInAppVolume() {
        return mPreferences.getFloat(IN_APP_VOLUME,1);
    }

    public final void setMinDuration(int value) {
        if(value<0) value = 0;
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putFloat(AUDIO_MIN_DURATION, value);
        editor.apply();
    }

    public final int getMinDuration() {
        return mPreferences.getInt(AUDIO_MIN_DURATION,10000);
    }

    public SharedPreferences getSharePreferences() {
        return mPreferences;
    }

    public void notFirstTime() {
        setFirstTime(false);
    }

    public float getBalanceValue() {
        return mPreferences.getFloat(BALANCE_VALUE,0.5f);
    }

    public final void setBalanceValue(float value) {
        if(value<0) value = 0;
        else if(value>1) value = 1;
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putFloat(BALANCE_VALUE, value);
        editor.apply();
    }

    public final void setThreadNumber(int value) {
        if(value<=0) return;
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putFloat(THREAD_NUMBER,value);
        editor.apply();
    }

    public final int getThreadNumber() {
        return mPreferences.getInt(THREAD_NUMBER,6);
    }


    public final void setCurrentAudio(Audio audio) {
        if (audio == null) return;
        if (audio.getUrl() == null) return;
        Gson gson = new Gson();
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(CURRENT_AUDIO, gson.toJson(audio).toString());
        editor.apply();
    }
    
    public final Audio getCurrentAudio() {
        String jsonString = mPreferences.getString(CURRENT_AUDIO, null);
        return new Gson().fromJson(jsonString, Audio.class);
    }

    public final boolean getUserAuthenticationState() {
        return mPreferences.getBoolean(USER_AUTHENTICATION_STATE, false);
    }

    public final void setUserAuthenticationState(boolean state) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(USER_AUTHENTICATION_STATE, state);
        editor.apply();
    }

    public final User getUserInformation() {
        return new Gson().fromJson(mPreferences.getString(USER_INFO_KEY, null), User.class);
    }

    public final void setUserInformation(User userInformation) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(USER_INFO_KEY, (new Gson()).toJson(userInformation));
        editor.apply();
    }

    public final String getAccessToken() {
        return mPreferences.getString(ACCESS_TOKEN, null);
    }

    public final void setAccessToken(String token) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(ACCESS_TOKEN, token);
        editor.apply();
    }

    public final boolean getShowMiniController() {
        return mPreferences.getBoolean(SHOW_MINI_CONTROLLER, false);
    }

    public final void setShowMiniController(boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(SHOW_MINI_CONTROLLER, value);
        editor.apply();
    }

    public final void setPlayingSpeedIndex(int idx) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(PLAYING_SPEED_INDEX, idx);
        editor.apply();
    }

    public final int getPlayingSpeedIndex() {
        return mPreferences.getInt(PLAYING_SPEED_INDEX, 2);
    }

    public final void clearAll() {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public final void setPlayingState(String state) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PLAYING_STATE, state);
        editor.apply();
    }

    public final String getPlayingState() {
        return mPreferences.getString(PLAYING_STATE, "idle");   //idle, play, pause
    }
}
