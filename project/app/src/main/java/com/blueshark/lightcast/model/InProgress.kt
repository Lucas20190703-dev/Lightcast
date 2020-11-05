package com.blueshark.lightcast.model

import java.io.Serializable

data class EpisodeExtendedProperties(
    var subscribe : Boolean,
    var transcribe: Boolean,
    var listenLater : Boolean?
) : Serializable

data class InProgress(
    var podcastName: String,
    var episodeName: String,
    var fileUrl : String,
    var img : String,
    var playtime : String,
    var episodeId : String,
    var podcastId: Long,
    var timeLeft : String,
    var createdDate : Long,
    var releaseDate: Long,
    var episodeExtendedProperties : EpisodeExtendedProperties
) : Serializable

data class InProgressResponse(
    var inprogressDto : ArrayList<InProgress>,
    var responseMessage : String
)

data class InprogressEpisodeDtoResponse(
    var responseMessage: String,
    var inprogressEpisodeDto: InprogressEpisodeDto?
)

data class InprogressEpisodeDto(
    var playtime : String,
    var episodeId : String,
    var podcastId : String,
    var timeLeft: String
)