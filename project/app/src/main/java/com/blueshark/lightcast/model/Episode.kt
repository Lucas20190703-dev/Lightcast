package com.blueshark.lightcast.model

import java.io.Serializable

data class Episode(
    var episodeId : String?,
    var title : String?,
    var summary : String?,
    var capacity : Long,
    var authorName : String?,
    var releaseDate : Long?,
    var fileUrl : String?,
    var thumbnail : String?,
    var categories : ArrayList<String>?,
    var duration : String?,
    var isPlayed : Long?,
    var playedSecond : Double?,
    var delFlag : Int?,
    var podcastId : Long?,
    var description : String?,
    var podcastName : String?,
    var segmentsByTime : ArrayList<Segment>?,
    var businessProfile : BusinessProfile?,
    var startTime : Long?,
    var migrated : Boolean?,
    var episodeExtendedProperties: EpisodeExtendedProperties?,
    var transcribed : Boolean?
) : Serializable

data class Segment(
    var caption : String?,
    var textBySegments : ArrayList<TextSegment>?,
    var otherUsersHighlightsCount : Int?
) : Serializable

data class TextSegment(
    var startTime : String?,
    var endTime : String?,
    var text : String?
): Serializable

data class BusinessProfile(
    var id : String?,
    var userId : String?,
    var podcasturl : String?,
    var name : String?,
    var banner : String?,
    var tags : String?,
    var bannerAds : String?
) : Serializable

