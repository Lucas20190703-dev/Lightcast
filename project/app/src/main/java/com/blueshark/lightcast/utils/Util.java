/*
 * Copyright (C) 2015 Naman Dwivedi
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.blueshark.lightcast.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;


import com.blueshark.lightcast.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Util {

    public static final String MUSIC_ONLY_SELECTION = MediaStore.Audio.AudioColumns.IS_MUSIC + "=1"
            + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''";

    public static boolean isOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }


    public static boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }


    public static boolean isJellyBeanMR2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    public static boolean isJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean isJellyBeanMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    public static Uri getAlbumArtUri(long albumId) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
    }
    public static String getAlbumArtForFile(String filePath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(filePath);

        return mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
    }

    public static final String makeCombinedString(final Context context, final String first,
                                                  final String second) {
        final String formatter = context.getResources().getString(R.string.combine_two_strings);
        return String.format(formatter, first, second);
    }

    public static final String makeLabel(final Context context, final int pluralInt,
                                         final int number) {
        return context.getResources().getQuantityString(pluralInt, number, number);
    }

    public static final String makeShortTimeString(final Context context, long secs) {
        long hours, mins;

        hours = secs / 3600;
        secs %= 3600;
        mins = secs / 60;
        secs %= 60;

        final String durationFormat = context.getResources().getString(
                hours == 0 ? R.string.durationformatshort : R.string.durationformatlong);
        return String.format(durationFormat, hours, mins, secs);
    }

    public static int getActionBarHeight(Context context) {
        int mActionBarHeight;
        TypedValue mTypedValue = new TypedValue();

        context.getTheme().resolveAttribute(R.attr.actionBarSize, mTypedValue, true);

        mActionBarHeight = TypedValue.complexToDimensionPixelSize(mTypedValue.data, context.getResources().getDisplayMetrics());

        return mActionBarHeight;
    }

    public static final int getSongCountForPlaylist(final Context context, final long playlistId) {
        Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                new String[]{BaseColumns._ID}, MUSIC_ONLY_SELECTION, null, null);

        if (c != null) {
            int count = 0;
            if (c.moveToFirst()) {
                count = c.getCount();
            }
            c.close();
            c = null;
            return count;
        }

        return 0;
    }

/*    public static boolean hasEffectsPanel(final Activity activity) {
        final PackageManager packageManager = activity.getPackageManager();
        return packageManager.resolveActivity(createEffectsIntent(),
                PackageManager.MATCH_DEFAULT_ONLY) != null;
    }*/

 /*   public static Intent createEffectsIntent() {
        final Intent effects = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
        effects.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, MusicPlayer.getAudioSessionId());
        return effects;
    }*/

    public static int getBlackWhiteColor(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        if (darkness >= 0.5) {
            return Color.WHITE;
        } else return Color.BLACK;
    }

    public enum IdType {
        NA(0),
        Artist(1),
        Album(2),
        Playlist(3);

        public final int mId;

        IdType(final int id) {
            mId = id;
        }

        public static IdType getTypeById(int id) {
            for (IdType type : values()) {
                if (type.mId == id) {
                    return type;
                }
            }

            throw new IllegalArgumentException("Unrecognized id: " + id);
        }
    }

    public static void removeFromPlaylist(final Context context, final long id,
                                          final long playlistId) {
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        final ContentResolver resolver = context.getContentResolver();
        resolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID + " = ? ", new String[]{
                Long.toString(id)
        });
    }
/*
    public static void clearTopTracks(Context context) {
        SongPlayCount.getInstance(context).deleteAll();
    }

    public static void clearRecent(Context context) {
        RecentStore.getInstance(context).deleteAll();
    }

    public static void clearLastAdded(Context context) {
        PreferencesUtility.getInstance(context)
                .setLastAddedCutoff(System.currentTimeMillis());
    }*/

    public static void showDeleteDialog(final Context context, final String name, final long[] list, final RecyclerView.Adapter adapter, final int pos) {
/*
        new MaterialDialog.Builder(context)
                .title("Delete song?")
                .content("Are you sure you want to delete " + name + " ?")
                .positiveText("Delete")
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Util.deleteTracks(context, list);
                        adapter.notifyItemRemoved(pos);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();


*/
    }
    /*public static void deleteTracks(final Context context, final long[] list) {
        final String[] projection = new String[]{
                BaseColumns._ID, MediaStore.MediaColumns.DATA, MediaStore.Audio.AudioColumns.ALBUM_ID
        };
        final StringBuilder selection = new StringBuilder();
        selection.append(BaseColumns._ID + " IN (");
        for (int i = 0; i < list.length; i++) {
            selection.append(list[i]);
            if (i < list.length - 1) {
                selection.append(",");
            }
        }
        selection.append(")");
        final Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection.toString(),
                null, null);
        if (c != null) {
            // Step 1: Remove selected tracks from the current playlist, as well
            // as from the album art cache
            c.moveToFirst();
            while (!c.isAfterLast()) {
                // Remove from current playlist
                final long id = c.getLong(0);
                MusicPlayer.removeTrack(id);
                // Remove the track from the shuffle count
                SongPlayCount.getInstance(context).removeItem(id);
                // Remove any items in the recents database
                RecentStore.getInstance(context).removeItem(id);
                c.moveToNext();
            }

            // Step 2: Remove selected tracks from the database
            context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    selection.toString(), null);

            // Step 3: Remove files from card
            c.moveToFirst();
            while (!c.isAfterLast()) {
                final String name = c.getString(1);
                final File f = new File(name);
                try { // File.delete can throw a security exception
                    if (!f.delete()) {
                        // I'm not sure if we'd ever get here (deletion would
                        // have to fail, but no exception thrown)
                        Log.e("MusicUtils", "Failed to delete file " + name);
                    }
                    c.moveToNext();
                } catch (final SecurityException ex) {
                    c.moveToNext();
                }
            }
            c.close();
        }

        final String message = makeLabel(context, R.plurals.NNNtracksdeleted, list.length);

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
        MusicPlayer.refresh();
    }*/

    public static void shareTrack(final Context context, long id) {

        final String[] projection = new String[]{
                BaseColumns._ID, MediaStore.MediaColumns.DATA, MediaStore.Audio.AudioColumns.ALBUM_ID
        };
        final StringBuilder selection = new StringBuilder();
        selection.append(BaseColumns._ID + " IN (");
        selection.append(id);
        selection.append(")");
        final Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection.toString(),
                null, null);

        if (c == null) {
            return;
        }
        c.moveToFirst();
        try {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("audio/*");
            share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(c.getString(1))));
            context.startActivity(Intent.createChooser(share, "Share"));
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Point getScreenSize(@NonNull Context c) {
        Display display = ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }
    public static void hideSoftKeyboard(@Nullable Activity activity) {
        if (activity != null) {
            View currentFocus = activity.getCurrentFocus();
            if (currentFocus != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
    }

    public static String getDateTime(Long s) {
        if (s == null) return "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            Date netDate = new Date(s);
            return sdf.format(netDate);
        } catch (Exception e) {
            Log.d("Util", e.toString());
            return "";
        }
    }

    public static String getTimeFormatFromSecond(Long s) {
        int hours = (int)(s / 3600);
        int minutes = (int)(s - hours * 3600) / 60;
        int seconds = (int)(s - hours * 3600 - minutes * 60);
        String formattedTime = "";
        if (hours < 10) formattedTime += "0";
        formattedTime += hours + ":";

        if (minutes < 10) formattedTime += "0";
        formattedTime += minutes + ":";

        if (seconds < 10) formattedTime += "0";
        formattedTime += seconds;
        return formattedTime;
    }

    public static String getTimeFormatFromSecondString(String str){
        double timeInDouble = 0;
        try {
            timeInDouble = Double.parseDouble(str);
        } catch (Exception e) {
            Log.d("Util", e.getMessage());
            return str;
        }
        return getTimeFormatFromSecond((long)timeInDouble);
    }

    public static int getMillisecondsFromSecondString(String str) {
        double timeInDouble;
        try {
            timeInDouble = Double.parseDouble(str);
        } catch (Exception e) {
            Log.d("Util", e.getMessage());
            return 0;
        }
        return (int)(timeInDouble * 1000);
    }

    public static int getMillisecondsFromHMSString(String str) {
        if (str == null) return 0;

        String[] ss = str.split(":");
        int seconds = 0;
        if (ss.length == 1) {
            seconds = Integer.parseInt(ss[0]);
        } else if (ss.length == 2) {
            int mins = Integer.parseInt(ss[0]);
            seconds = Integer.parseInt(ss[1]);
            seconds += mins * 60;
        } else if (ss.length > 2) {
            int hours = Integer.parseInt(ss[0]);
            int mins = Integer.parseInt(ss[1]);
            seconds = Integer.parseInt(ss[2]);
            seconds += 60 * mins + 3600 * hours;
        }
        return seconds * 1000;
    }

    //for transcript time format - 00:04:30, 458
    public static int getMillisecondsFromHMSMSString(String str) {
        if (str == null) return 0;
        String[] ss = str.split(",");
        int ms = Integer.parseInt(ss[1]);
        return getMillisecondsFromHMSString(ss[0]) + ms;
    }

    public String getTimeFormatFromMillisecondString(String str){
        long timeInLong;
        try {
            timeInLong = Long.parseLong(str);
        } catch (Exception e) {
            Log.d("Util", e.getMessage());
            return str;
        }
        return getTimeFormatFromMillisecond(timeInLong);
    }

    public static String getTimeFormatFromMillisecond(long ms){
        long s = (ms + 500) / 1000;
        long hours = s / 3600;
        long minutes = (s- hours * 3600) / 60;
        long seconds = s - hours * 3600 - minutes * 60;
        String formattedTime = "";
        if (hours < 10) formattedTime += "0";
        formattedTime += String.valueOf(hours) + ":";

        if (minutes < 10) formattedTime += "0";
        formattedTime += minutes + ":";

        if (seconds < 10) formattedTime += "0";
        formattedTime += seconds;
        return formattedTime;
    }
}
