package com.blueshark.lightcast.model

data class PodcastOverview(
    var highlightText : ArrayList<HighLight>,
    var relevanceScore : Double,
    var topPodcast : TopPodcast
)

data class SearchResult(
    var podcastOverview : PodcastOverview?,
    var episodeCount: Int,
    var offset : Int
)

data class PodcastOverviewResponse(
    var docSearchResults : ArrayList<PodcastOverview>,
    var maxPage : Int,
    var pages : ArrayList<Int>?,
    var totalResultCount : Int,
    var episodeCount : Int
)

data class HighLight(
    var text : String?,
    var startTime : String?,
    var page : Int?
)

data class TopPodcast(
    var podcastId : Long,
    var name : String,
    var thumbnail : String,
    var description : String,
    var publishDate : Long,
    var releaseDate : Long,
    var authorName : String,
    var isSubscribed : Boolean,
    var categories : ArrayList<String>?,
    var episodes : ArrayList<Episode>?,
    var artworkUrl600 : String
)



//Search
data class SearchResultItem(
    var podcastId : Long?,
    var name : String?,
    var thumbnail : String?,
    var description : String?,
    var publishDate : Long?,
    var releaseDate : Long?,
    var authorName : String?,
    var isSubscribed : Boolean?,
    var categories : ArrayList<String>?,
    var episode : Episode?,
    var artworkUrl600 : String?
)