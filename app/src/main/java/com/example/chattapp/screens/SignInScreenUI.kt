package com.example.chattapp.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SignInScreenUI(onSignInClick: () -> Unit, modifier: Modifier = Modifier) {

    Box(modifier = modifier.fillMaxSize(), Alignment.Center){
        Button(
            onClick = {
                onSignInClick.invoke()
            }
        ) {
            Text(text = "Continue with Google")
        }
    }
}