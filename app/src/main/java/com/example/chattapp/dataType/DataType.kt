package com.example.chattapp.dataType

import androidx.compose.runtime.MutableState
import com.google.firebase.Timestamp

data class SignInResult(
    val data : UserData?,
    val errorMessage : String?
)

data class UserData(
    val userId : String? = "",
    val userName : String? = "",
    val profilePictureUrl : String? = "",
    val email : String? = "",
    val bio : String? = null
)

data class AppState(
    val isSigned : Boolean = false,
    val userData : UserData? = null,
    val signInError : String? = null,
    val srEmail : String? = null,
    val showDialog : Boolean = false,
    val User2 : ChatUserData? = null,
    val chatId : String = ""
)

data class ChatData(
    val chatId : String? = null,
    val last : Message? = null,
    val user1 : ChatUserData? = null,
    val user2 : ChatUserData? = null
)


data class Message(
    val msgId : String = "",
    val senderId : String = "",
    val repliedMessage : Message? = null,
    val reaction : List<Reaction> = emptyList<Reaction>(),
    val imageUrl : String = "",
    val fileUrl : String = "",
    val fileName : String = "",
    val fileSize : String = "",
    val vidUrl : String = "",
    val content : String = "",
    val progress : String = "",
    val time : Timestamp ? = null,
    val forwarded : Boolean = false,
    val read : Boolean = false
)

data class Reaction(
    val userId : String = "",
    val ppurl : String = "",
    val userName : String = "",
    val reaction : String = ""
)

data class ChatUserData(
    val userId : String = "",
    val typing : Boolean = false,
    val ppurl : String = "",
    val userName : String = "",
    val bio : String = "",
    val email : String = "",
    val status : String = "",
    val unRead : Int = 0
)

