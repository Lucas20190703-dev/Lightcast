package com.blueshark.lightcast.model

data class Release(
    var podcastId: Long,
    var episodeId : String,
    var podcastTitle : String,
    var episodeTitle: String,
    var authorName : String,
    var imageUrl : String,
    var duration: String,
    var audioFilePath : String,
    var releaseDate: Long,
    var episodeExtendedProperties : EpisodeExtendedProperties
)
