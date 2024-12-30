package com.example.chattapp.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import com.example.chattapp.R
import com.example.chattapp.dataType.AppState
import com.example.chattapp.dataType.ChatData
import com.example.chattapp.dataType.ChatUserData
import com.example.chattapp.dialogue.CustomDialogueBox
import com.example.chattapp.viewmodel.ViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ChatsScreenUI(viewModel: ViewModel , state : AppState , showSingleChat : (ChatUserData,String) -> Unit = {_,_ ->}) {

    val padding = animateDpAsState(targetValue = 10.dp , label = "")
    val chats = viewModel.chats
    val filterChats = chats
    val selectedItem = remember {
        mutableStateListOf<String>()
    }

    val brush = Brush.linearGradient(
        listOf(
            Color(0xFF1D6EAD),
            Color(0xFF032C80)
        )
    )

    Scaffold (
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.showDialog()
                }
            ) {
                Icon(imageVector = Icons.Filled.AddComment,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    ){
        Box(modifier = Modifier
            .background(brush = brush)
            .fillMaxSize())
        AnimatedVisibility(visible = state.showDialog) {
            CustomDialogueBox(
                state = state,
                setEmail = {viewModel.setSrEmail(it)},
                hideDialog = {viewModel.hideDialog()},
                addChat = {
                    viewModel.addChat(state.srEmail)
                    viewModel.hideDialog()
                    viewModel.setSrEmail("")
                }
            )
        }

        Column (
            modifier = Modifier.padding(top = 36.dp)
        ){
            Box(){
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(.98f)
                ){
                    Column {
                        Text(
                            text = "Hello ,",
                            modifier = Modifier.padding(start = 16.dp).offset(y = 5.dp),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = state.userData?.userName.toString(),
                            modifier = Modifier.padding(start = 16.dp),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 20.sp
                            ),
                            fontWeight = FontWeight.Bold
                        )
                    }
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = {},
                            modifier = Modifier.background(
                                lightColorScheme().background.copy(alpha = 0.2f),
                                CircleShape
                            ).border(
                                0.05.dp,
                                color = Color(0xFF35567A),
                                shape = CircleShape
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search, contentDescription = null,
                                modifier = Modifier.scale(0.7f)
                            )

                        }
                            Column() {
                                IconButton(
                                    onClick = {},
                                    modifier = Modifier.background(lightColorScheme().background.copy(alpha = 0.2f),
                                        CircleShape).border(0.05.dp, Color(0xFF35567A), CircleShape)
                                ) {
                                    Icon(imageVector = Icons.Filled.MoreVert ,contentDescription = null ,
                                        modifier = Modifier.scale(1.3f))
                                }
                            }

                }
            }
            LazyColumn (
                modifier = Modifier
                    .padding(top = padding.value)
                    .fillMaxSize()
                    .background(
                        color = darkColorScheme().background.copy(
                            alpha = .2f
                        ),
                        shape = RoundedCornerShape(30.dp, 30.dp)
                    )
                    .border(
                        0.05.dp,
                        color = Color(0xFF35567A),
                        shape = RoundedCornerShape(30.dp, 30.dp)
                    )
            ){
                item {
                    Text(
                        text = "Chats",
                        modifier = Modifier.padding(16.dp,16.dp,16.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Normal
                    )
                }

                items(filterChats.value){
                    val chatUser = if(
                        it.user1?.userId != state.userData?.userId
                    ){
                        it.user1
                    }else{
                        it.user2
                    }
                    ChatItem(
                        state = state,
                        chatUser!!,
                        chat = it,
                        isSelected = selectedItem.contains(it.chatId),
                        showSingleChat = { user, id -> showSingleChat(user,id).toString() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatItem(
    state : AppState,
    userData : ChatUserData,
    chat : ChatData,
    isSelected : Boolean,
    showSingleChat: (ChatUserData, String) -> Unit
){
    val formatter = remember {
        SimpleDateFormat(("hh:mm a"),Locale.getDefault())
    }

    val color = if(!isSelected) Color.Transparent else darkColorScheme().onPrimary

    Row (
        modifier = Modifier.background(
            color = color
        ).fillMaxWidth().padding(horizontal = 16.dp , vertical = 12.dp)
            .clickable {
                chat.chatId?.let {
                    showSingleChat(
                        userData, it
                    )
                }
            },
        verticalAlignment = Alignment.CenterVertically
     ){
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(userData.ppurl)
                    .crossfade(true)
                    .allowHardware(false)
                    .build(),
                placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                error = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(70.dp).clip(CircleShape)
            )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ){
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(.95f)
             ){
                Text(
                    text = if(userData.userId == (state.userData?.userId ?: "")){
                        userData.userName.orEmpty() + "(You)"
                     }else{
                        userData.userName.orEmpty()
                     },
                    modifier = Modifier.width(150.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (chat.last?.time != null) formatter.format(chat.last.time.toDate()) else "",
                    color = Color.Gray,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Light
                    )
                )
             }

            AnimatedVisibility(chat.last?.time != null && userData.typing){
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ){
                    if (chat.last?.senderId == state.userData?.userId){
                        Icon(
                            imageVector = Icons.Filled.AddTask,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 5.dp).size(10.dp),
                            tint = if (chat.last?.read == true) Color(0xFF13C70D) else Color.White
                        )
                    }
                }
            }
        }
     }
}