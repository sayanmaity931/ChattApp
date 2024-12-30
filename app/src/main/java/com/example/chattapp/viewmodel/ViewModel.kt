package com.example.chattapp.viewmodel

import android.content.ContentValues
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chattapp.CHATS_COLLECTION
import com.example.chattapp.MESSAGES_COLLECTION
import com.example.chattapp.USERS_COLLECTION
import com.example.chattapp.dataType.AppState
import com.example.chattapp.dataType.ChatData
import com.example.chattapp.dataType.ChatUserData
import com.example.chattapp.dataType.Message
import com.example.chattapp.dataType.SignInResult
import com.example.chattapp.dataType.UserData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class ViewModel : ViewModel() {

    private val _state = MutableStateFlow(AppState())
    val state = _state.asStateFlow()
    private val userCollection = Firebase.firestore.collection(USERS_COLLECTION)
    var userDataListener: ListenerRegistration? = null
    var chatDataListener: ListenerRegistration? = null
    var chats = mutableStateOf<List<ChatData>>(emptyList())
    var tp = mutableStateOf(ChatData())
    var tpListener : ListenerRegistration?= null
    var reply by mutableStateOf("")
    private val firestore = FirebaseFirestore.getInstance()
    var msgListener : ListenerRegistration? = null
    var messages by mutableStateOf<List<Message>>(listOf())

    fun resetState() {

    }

    fun onSignInResult(signInResult: SignInResult) {

        _state.update {
            it.copy(
                isSigned = signInResult.data != null,
                signInError = signInResult.errorMessage
            )
        }
    }

    fun addUserToFireStore(userData: UserData?) {

        val userDataMap = mapOf(
            "userId" to userData?.userId,
            "userName" to userData?.userName,
            "profilePictureUrl" to userData?.profilePictureUrl,
            "email" to userData?.email
        )

        val userDocument = userCollection.document(userData?.userId ?: "")
        userDocument.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                userDocument.update(userDataMap).addOnSuccessListener {
                    Log.d(ContentValues.TAG, "UserData updated successfully")
                }.addOnFailureListener {
                    Log.d(ContentValues.TAG, "Failed to update UserData")
                }
            } else {
                userDocument.set(userDataMap).addOnSuccessListener {
                    Log.d(ContentValues.TAG, "UserData added successfully")
                }.addOnFailureListener {
                    Log.d(ContentValues.TAG, "Failed to add UserData")
                }
            }
        }
    }

    fun getUserData(userId: String) {
        userDataListener = userCollection.document(userId).addSnapshotListener { value, error ->
            if (value != null) {
                val userData = value.toObject(UserData::class.java)
                _state.update {
                    it.copy(
                        userData = userData
                    )
                }
            }
        }
    }

    fun hideDialog() {
        _state.update {
            it.copy(
                showDialog = false
            )
        }
    }

    fun showDialog() {
        _state.update {
            it.copy(
                showDialog = true
            )
        }
    }

    fun setSrEmail(email: String) {
        _state.update {
            it.copy(
                srEmail = email
            )
        }
    }

    fun addChat(email: String?) {

        Firebase.firestore.collection(CHATS_COLLECTION).where(
            Filter.or(
                Filter.and(
                    Filter.equalTo("user1.email", state.value.userData?.email),
                    Filter.equalTo("user2.email", email)
                ),
                Filter.and(
                    Filter.equalTo("user1.email", email),
                    Filter.equalTo("user2.email", state.value.userData?.email)
                )
            )
        ).get().addOnSuccessListener {
            if (it.isEmpty) {

        userCollection.whereEqualTo("email", email).get().addOnSuccessListener {

            if (it.isEmpty) {
                Log.d("addChat", "It is empty")
            } else {
                val chatPartner = it.toObjects(UserData::class.java).firstOrNull()
                val id = Firebase.firestore.collection(CHATS_COLLECTION).document().id
                val chat = ChatData(
                    chatId = id,
                    last = Message(
                        senderId = "",
                        content = "",
                        time = null
                    ),
                    user1 = ChatUserData(
                        userId = state.value.userData?.userId.toString(),
                        typing = false,
                        ppurl = state.value.userData?.profilePictureUrl.toString(),
                        userName = state.value.userData?.userName.toString(),
                        bio = state.value.userData?.bio.toString(),
                        email = state.value.userData?.email.toString(),
                    ),
                    user2 = ChatUserData(
                        userId = chatPartner?.userId.toString(),
                        typing = false,
                        ppurl = chatPartner?.profilePictureUrl.toString(),
                        userName = chatPartner?.userName.toString(),
                        email = chatPartner?.email.toString(),
                        bio = chatPartner?.bio.toString()
                    )
                )
                Firebase.firestore.collection(CHATS_COLLECTION).document().set(chat)
                }
            }
        }else{
                Log.d("addChat", "Already Exists")
            }
        }
    }

    fun showChats(userId : String){
        chatDataListener = Firebase.firestore.collection(CHATS_COLLECTION).where(
            Filter.or(
                Filter.equalTo("user1.userId",userId),
                Filter.equalTo("user2.userId",userId)
            )
        ).addSnapshotListener { value, error ->
            if(value != null){
                chats.value = value.documents.mapNotNull {
                    it.toObject<ChatData>()
                }.sortedBy {
                    it.last?.time
                }.reversed()
            }
        }
    }

    fun getTp(chatId : String = ""){
        tpListener?.remove()
        tpListener = Firebase.firestore.collection(CHATS_COLLECTION).document(chatId).addSnapshotListener{snp , err ->
            if (err != null) {
                // Log the error
                Log.d("TAG", "Error: $err")
                return@addSnapshotListener
            }

            if (snp != null && snp.exists()) {
                // Convert snapshot to ChatData only if it's valid
                val chatData = snp.toObject(ChatData::class.java)
                if (chatData != null) {
                    tp.value = chatData
                } else {
                    Log.d("TAG", "Failed to parse snapshot to ChatData")
                }
            } else {
                Log.d("TAG", "Snapshot is null or does not exist")
            }
        }
    }

    fun setChatUser(usr: ChatUserData, id: String) {
        _state.update {
            it.copy(
                User2 = usr , chatId = id
            )
        }
    }

    fun sendReply(
        chatId : String,
        replyMessage : Message = Message(),
        msg : String,
        senderId : String = state.value.userData?.userId.toString()
    ){

        val id = Firebase.firestore.collection(CHATS_COLLECTION).document()
            .collection(MESSAGES_COLLECTION).document().id

        val time = Calendar.getInstance().time

        val messages = Message(
            msgId = id,
            repliedMessage = replyMessage,
            senderId = senderId,
            content = msg,
            time = Timestamp(date = time)
        )

        Firebase.firestore.collection(CHATS_COLLECTION).document(chatId).collection(
            MESSAGES_COLLECTION).document(id).set(messages)

        firestore.collection(CHATS_COLLECTION).document(chatId).update("last",messages)
    }

    fun popMessages(chatId : String){
        msgListener?.remove()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (chatId != "") {

                    msgListener =
                        Firebase.firestore.collection(CHATS_COLLECTION).document(chatId).collection(
                            MESSAGES_COLLECTION
                        ).addSnapshotListener { value, error ->
                            if (value != null) {
                                messages = value.documents.mapNotNull {
                                    it.toObject(Message::class.java)
                                }.sortedBy {
                                    it.time
                                }.reversed()
                            }else{
                                Log.d("TAG", "Snapshot is null or does not exist")
                            }
                        }
                }else{
                    Log.d("TAG", "chatId is empty")
                }
            }
        }
    }
}