package com.blueshark.lightcast.model

data class PodcastRequestParams(
    var offset: Int,
    var size : Int,
    var podcastId : Long
)

data class EpisodeRequestParam(
    var podcastId : Long,
    var episodeId : String
)

data class RequestParams(
    var offset : Int,
    var size : Int
)

data class SearchQueryParam(
    var p : Int,
    var query : String?
)
