package com.blueshark.lightcast.model

data class ListData<T> constructor(
    var offset: Int,
    var data: List<T>
)
