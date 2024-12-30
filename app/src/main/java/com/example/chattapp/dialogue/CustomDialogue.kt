package com.example.chattapp.dialogue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.chattapp.dataType.AppState

@Composable
fun CustomDialogueBox(
    modifier: Modifier = Modifier,
    state : AppState,
    setEmail : (String) -> Unit,
    hideDialog : () -> Unit,
    addChat : () -> Unit
    ) {

    Dialog(onDismissRequest = { hideDialog.invoke() } ,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true, usePlatformDefaultWidth = false)) {

        Card (
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = modifier.fillMaxWidth(.90f),
            colors = CardDefaults.cardColors(
                MaterialTheme.colorScheme.onPrimary
            )
        ){
            Column (
                modifier = modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ){
                Text(
                    text = "Enter Email ID",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = modifier.align(Alignment.CenterHorizontally
                    )
                )
                OutlinedTextField(
                    label = {
                        Text(text = "Enter Email" )
                    },
                    value = state.srEmail ?: "",
                    onValueChange = {setEmail(it) },
                    shape = RoundedCornerShape(20.dp),
                    modifier = modifier.align(Alignment.CenterHorizontally)
                )
                Row (
                    modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ){

                    TextButton(
                        onClick = { hideDialog.invoke() }
                    ) {
                        Text(text = "Cancel" , style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = modifier.width(16.dp))
                    TextButton(
                        onClick = {
                            addChat.invoke()
                        }
                    ) {
                        Text(text = "Add" , style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}