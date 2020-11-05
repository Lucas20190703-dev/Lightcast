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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constants {

    public static final String NAVIGATE_LIBRARY = "navigate_library";
    public static final String NAVIGATE_PLAYLIST = "navigate_playlist";
    public static final String NAVIGATE_QUEUE = "navigate_queue";
    public static final String NAVIGATE_ALBUM = "navigate_album";
    public static final String NAVIGATE_ARTIST = "navigate_artist";
    public static final String NAVIGATE_NOWPLAYING = "navigate_nowplaying";
    public static final String NAVIGATE_LYRICS = "navigate_lyrics";

    public static final String NAVIGATE_PLAYLIST_RECENT = "navigate_playlist_recent";
    public static final String NAVIGATE_PLAYLIST_LASTADDED = "navigate_playlist_lastadded";
    public static final String NAVIGATE_PLAYLIST_TOPTRACKS = "navigate_playlist_toptracks";
    public static final String NAVIGATE_PLAYLIST_USERCREATED = "navigate_playlist";
    public static final String PLAYLIST_FOREGROUND_COLOR = "foreground_color";
    public static final String PLAYLIST_NAME = "playlist_name";

    public static final String ALBUM_ID = "album_id";
    public static final String ARTIST_ID = "artist_id";
    public static final String PLAYLIST_ID = "playlist_id";

    public static final String FRAGMENT_ID = "fragment_id";
    public static final String NOWPLAYING_FRAGMENT_ID = "nowplaying_fragment_id";

    public static final String WITH_ANIMATIONS = "with_animations";

    public static final String TIMBER1 = "timber1";
    public static final String TIMBER2 = "timber2";
    public static final String TIMBER3 = "timber3";
    public static final String TIMBER4 = "timber4";
    public static final String TIMBER5 = "timber5";
    public static final String TIMBER6 = "timber6";

    public static final String NAVIGATE_SETTINGS = "navigate_settings";
    public static final String NAVIGATE_SEARCH = "navigate_search";

    public static final String SETTINGS_STYLE_SELECTOR_NOWPLAYING = "style_selector_nowplaying";
    public static final String SETTINGS_STYLE_SELECTOR_ARTIST = "style_selector_artist";
    public static final String SETTINGS_STYLE_SELECTOR_ALBUM = "style_selector_album";
    public static final String SETTINGS_STYLE_SELECTOR_WHAT = "style_selector_what";

    public static final String SETTINGS_STYLE_SELECTOR = "settings_style_selector";

    public static final int PLAYLIST_VIEW_DEFAULT = 0;
    public static final int PLAYLIST_VIEW_LIST = 1;
    public static final int PLAYLIST_VIEW_GRID = 2;

    public static final int PLAYLIST_ALBUM_ART_TAG = 888;
    public static final int ACTION_DELETE_PLAYLIST = 111;


    public static final String ACTIVITY_TRANSITION = "activity_transition";

    public static final int CAST_SERVER_PORT = 8080;

    public static final List<Float> PLAYING_SPEED = Arrays.asList(0.5f, 0.8f, 1.0f, 1.2f, 1.3f, 1.4f, 1.5f, 1.6f, 1.7f, 1.8f, 1.9f, 2.0f, 2.3f, 2.5f, 2.7f, 3.0f);

    public static final long ADD_IN_PROGRESS_INTERVAL = 5000;
}
