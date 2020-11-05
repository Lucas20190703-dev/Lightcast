package com.blueshark.lightcast.model

data class Subscription(
    var podcastName : String,
    var img : String,
    var podcastId : Long,
    var releaseDate : Long,
    var date : Long
)

data class SubscriptionResponse(
    var subscribeDto : ArrayList<Subscription>,
    var responseMessage : String
)
