package com.example.chattapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chattapp.googleSign.GoogleAuthUIClient
import com.example.chattapp.routes.Chats
import com.example.chattapp.routes.ChatsScreen
import com.example.chattapp.routes.SignInScreen
import com.example.chattapp.routes.StartScreen
import com.example.chattapp.screens.ChatsScreenUI
import com.example.chattapp.screens.ChatsUI
import com.example.chattapp.screens.SignInScreenUI
import com.example.chattapp.ui.theme.ChattAppTheme
import com.example.chattapp.viewmodel.ViewModel
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel : ViewModel by viewModels()

    private val googleAuthUIClient by lazy {
        GoogleAuthUIClient(
            context = applicationContext,
            onTapClient = Identity.getSignInClient(applicationContext),
            viewModel = viewModel
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChattAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())){

                        val state =  viewModel.state.collectAsStateWithLifecycle()

                        val navController = rememberNavController()

                        NavHost(navController = navController , startDestination = StartScreen){


                            composable<SignInScreen>{

                                val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartIntentSenderForResult(),
                                    onResult = {result ->
                                        if(result.resultCode == RESULT_OK){
                                            lifecycleScope.launch{
                                                val signInResult = googleAuthUIClient.signInWithIntent(
                                                    intent = result.data ?: return@launch
                                                )

                                                viewModel.onSignInResult(signInResult)
                                            }
                                        }
                                    }
                                )

                                LaunchedEffect(state.value.isSigned) {

                                    val userData = googleAuthUIClient.getSignedInUser()

                                    if (userData?.userId != null && userData.userId.isNotBlank()){
                                        userData.run {
                                            viewModel.addUserToFireStore(userData)
                                            viewModel.getUserData(userData.userId)
                                            viewModel.showChats(userData.userId)
                                            navController.navigate(ChatsScreen)
                                        }
                                    }else{
                                        Log.d("Faltu", "onCreate: Error ")
                                    }

                                }

                                SignInScreenUI(
                                    onSignInClick = {
                                        lifecycleScope.launch{
                                            val signInIntentSender = googleAuthUIClient.signIn()
                                            launcher.launch(
                                                IntentSenderRequest.Builder(
                                                    signInIntentSender ?: return@launch
                                                ).build()
                                            )
                                        }
                                    }
                                )
                            }

                            composable<ChatsScreen>{
                                ChatsScreenUI(viewModel = viewModel , state = state.value,
                                    showSingleChat = {usr , id ->
                                        viewModel.getTp(chatId = id)
                                        viewModel.setChatUser(usr , id)
                                        navController.navigate(Chats)
                                    }
                                )
                            }

                            composable<StartScreen>{
                                LaunchedEffect(Unit) {

                                    val userData = googleAuthUIClient.getSignedInUser()

                                    if (userData != null) {
                                        viewModel.getUserData(userData.userId.toString())
                                        viewModel.showChats(userData.userId.toString())
                                        navController.navigate(ChatsScreen)
                                    }
                                    else{
                                        navController.navigate(SignInScreen)
                                    }
                                }
                            }

                            composable<Chats>(enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = {fullWidth ->
                                        fullWidth
                                    },
                                    animationSpec = tween(200)
                                )
                            },
                                exitTransition = {
                                    slideOutHorizontally(
                                        targetOffsetX = {fullWidth ->
                                            -fullWidth
                                        },
                                        animationSpec = tween(200)
                                    )
                                }
                            ){
                                ChatsUI(
                                    viewModel = viewModel,
                                    navController = navController,
                                    userData = state.value.User2!!,
                                    chatId = state.value.chatId,
                                    state = state.value,
                                    messages = viewModel.messages
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

