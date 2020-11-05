package com.blueshark.lightcast.model

data class Audio(
    var title : String?,
    var thumbnail : String?,
    var url : String?,
    var author : String?,
    var podcastName : String?,
    var episodeId : String?,
    var podcastId : Long?,
    var duration : String?,
    var releaseDate : Long?,
    var lastPlayTime : Int?,
    var durationInMilliSeconds : Int? = null  //for play card ui.
)