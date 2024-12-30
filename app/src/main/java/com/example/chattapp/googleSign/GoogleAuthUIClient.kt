package com.example.chattapp.googleSign

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.example.chattapp.dataType.SignInResult
import com.example.chattapp.dataType.UserData
import com.example.chattapp.viewmodel.ViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class GoogleAuthUIClient(
    private val context: Context,
    private val onTapClient : SignInClient,
    val viewModel: ViewModel
) {
    private val auth = Firebase.auth

    suspend fun signIn(): IntentSender?{
        val result = try {
            onTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        }catch (e : Exception){
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    private fun buildSignInRequest(): BeginSignInRequest{
        return BeginSignInRequest.builder().setGoogleIdTokenRequestOptions(
            GoogleIdTokenRequestOptions.builder().setSupported(true)
                .setFilterByAuthorizedAccounts(false).setServerClientId("816083303639-qr0s04ii06l715do7njq2ea755oku1n5.apps.googleusercontent.com").build()
        ).setAutoSelectEnabled(true).build()
    }

    suspend fun signInWithIntent(intent : Intent) : SignInResult {

        viewModel.resetState()
        val cred = onTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = cred.googleIdToken
        val googleCred = GoogleAuthProvider.getCredential(googleIdToken,null)
        return try {
            val users = auth.signInWithCredential(googleCred).await().user
            SignInResult(
                errorMessage = null,
                data = users?.run {
                    UserData(
                        email = email.toString(),
                        userId = uid,
                        userName = displayName,
                        profilePictureUrl = photoUrl?.toString()?.substring(0,photoUrl.toString().length-6),
                    )
                }
            )
        }catch (e : Exception){
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignInResult(
                errorMessage = e.message,
                data = null
            )
        }
    }

    fun getSignedInUser() : UserData? = auth.currentUser?.run {
        UserData(
            email = email.toString(),
            userId = uid,
            userName = displayName,
            profilePictureUrl = photoUrl?.toString()
        )
    }
}