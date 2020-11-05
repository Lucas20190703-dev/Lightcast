package com.blueshark.lightcast.model


data class ListenLater (
    var podcastName: String,
    var episodeName: String,
    var fileUrl : String,
    var img : String,
    var playtime : String,
    var episodeId : String,
    var podcastId: Long,
    var createdDate : Long,
    var releaseDate: Long,
    var episodeExtendedProperties : EpisodeExtendedProperties
)

data class ListenLaterResponse (
    var bookMarks : ArrayList<ListenLater>,
    var responseMessage : String
)
