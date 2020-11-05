package com.blueshark.lightcast.model

data class Bookmark(
    var podcastId : Long,
    var episodeId : String,
    var userId : String,
    var timestamp : String,
    var episodeName : String,
    var filePath : String,
    var createdDate : Long,
    var podcastName : String,
    var image : String,
    var segment : String,
    var releaseDate : Long,
    var episodeExtendedProperties: EpisodeExtendedProperties
)

