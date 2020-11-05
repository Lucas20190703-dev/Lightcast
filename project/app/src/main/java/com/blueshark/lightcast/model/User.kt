package com.blueshark.lightcast.model

data class Address(
    var email : String?,
    var houseNo : String?,
    var phoneNo : String?,
    var city : String?,
    var country : String?,
    var postalCode : String?,
    var state : String?
)
data class User(
    var userid : String,
    var email : String,
    var username : String,
    var firstname : String,
    var lastname : String,
    var balance : Double,
    var address : Address,
    var referralCode : String,
    var noOfRefferral : Int
)

data class UserResponse(
    var user : User?,
    var accessToken : String?,
    var email : String?,
    var password: String?,

    var errorCode: String?,
    var message : String?
)

enum class AuthenticationState {
    UNAUTHENTICATED,        // Initial state, the user needs to authenticate
    AUTHENTICATED  ,        // The user has authenticated successfully
    INVALID_AUTHENTICATION  // Authentication failed
}

data class AuthenticationResult(
    var authenticationState: AuthenticationState,
    var errorCode : LoginErrorCode?,
    var message : String?
)

enum class LoginErrorCode {
    INVALID_USER_NAME,
    INVALID_PASSWORD,
    NULL_PASSWORD,
    NULL_USER_NAME,
    NOT_REGISTERED_USER,
    NETWORK_FAILED
}

