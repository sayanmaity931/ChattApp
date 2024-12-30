package com.example.chattapp.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.chattapp.dataType.AppState
import com.example.chattapp.dataType.ChatUserData
import com.example.chattapp.dataType.Message
import com.example.chattapp.viewmodel.ViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun ChatsUI(
    viewModel: ViewModel,
    userData: ChatUserData,
    chatId: String,
    messages: List<Message> = emptyList(),
    state : AppState,
    onBack : () -> Unit = {},
    context: Context = LocalContext.current,
    navController : NavController
) {
    val tp = viewModel.tp
    val focusRequester = remember { FocusRequester() }
    val brush = Brush.linearGradient(
        listOf(
            Color(0xFF6F8DE7),
            Color(0xFF185FEC)
        )
    )
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.popMessages(state.chatId)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        AsyncImage(
                            model = userData.ppurl, contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.clip(CircleShape)
                                .size(40.dp)
                        )
                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if(userData.userId == (state.userData?.userId ?: "")){
                                    userData.userName.orEmpty() + "(You)"
                                }else{
                                    userData.userName.orEmpty()
                                },
                                modifier = Modifier.padding(start = 16.dp)
                            )
                            if(userData.userId == tp.value.user1?.userId){
                                AnimatedVisibility(tp.value.user1!!.typing) {
                                    Text(text = "typing...", modifier = Modifier.padding(16.dp),
                                        color = MaterialTheme.colorScheme.onBackground,
                                        style = MaterialTheme.typography.titleSmall)
                                }
                            }
                            if(userData.userId == tp.value.user2?.userId){
                                AnimatedVisibility(tp.value.user2!!.typing) {
                                    Text(text = "typing...", modifier = Modifier.padding(16.dp),
                                        color = MaterialTheme.colorScheme.onBackground,
                                        style = MaterialTheme.typography.titleSmall)
                                }
                            }

                        }
                    }
                },
                navigationIcon = {
                    Icon(
                        Icons.Filled.ArrowBackIosNew,
                        contentDescription = null
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(Color(0xFF4A6598))
            )
        }
     ) {
        Box(modifier = Modifier
            .background(brush = brush)
            .fillMaxSize())
        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 8.dp).fillMaxSize()
        ){
            LazyColumn (
                modifier = Modifier.weight(1f).fillMaxSize(),
                reverseLayout = true,
                state = listState
            ){

                items(messages.size){index ->

                    val message = messages.get(index = index)
                    val prevMessage = if(index > 0) messages[index - 1] else null
                    val nextMessage = if(index < messages.size - 1) messages[index + 1] else null

                    Spacer(modifier = Modifier.height(5.dp))

                    MessageItem(
                        message = message,
                        index = index,
                        prevId = prevMessage?.senderId,
                        nextId = nextMessage?.senderId,
                        state = state
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.imePadding().padding(horizontal = 16.dp).padding(top = 8.dp)
            ){
            Icon(imageVector = Icons.Rounded.CameraAlt, contentDescription = null , modifier = Modifier.padding(end = 8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.focusRestorer { focusRequester }.background(
                    color = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(32.dp)
                )
            ) {

                TextField(
                    value = viewModel.reply, onValueChange = {
                        viewModel.reply = it
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(text = "Type a message")
                    },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )
                )
                AnimatedVisibility(visible = viewModel.reply.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send, contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp).padding(12.dp)
                            .combinedClickable (
                                onClick = {
                                    viewModel.sendReply(
                                        msg = viewModel.reply,
                                        chatId = chatId
                                    )
                                    viewModel.reply = ""
                                },
                                onDoubleClick = {

                                }
                            )
                    )
                }
            }
        }
    }
    }
}

@Composable
fun MessageItem(message: Message, index: Int, prevId: String?, nextId: String? , state: AppState) {

    val context = LocalContext.current

    val brush1 = Brush.linearGradient(
        listOf(
            Color(0xFF238CDD),
            Color(0xFF1952C4)
        )
    )

    val brush2 = Brush.linearGradient(
        listOf(
            Color(0xFF2A4783),
            Color(0xFF2F6086)
        )
    )

    val isCurrentUser = state.userData?.userId == message.senderId

    val shape = if(isCurrentUser){
        if (prevId == message.senderId && nextId == message.senderId){
            RoundedCornerShape(16.dp, 3.dp, 3.dp, 16.dp)
        }else if (prevId == message.senderId){
            RoundedCornerShape(16.dp , 16.dp , 3.dp , 3.dp)
        }else if (nextId == message.senderId){
            RoundedCornerShape(16.dp , 3.dp , 16.dp , 3.dp)
        } else {
            RoundedCornerShape(16.dp , 16.dp , 16.dp , 16.dp)
        }
    } else {
        if (prevId == message.senderId && nextId == message.senderId){
            RoundedCornerShape(3.dp, 16.dp, 16.dp, 3.dp)
        }else if (prevId == message.senderId){
            RoundedCornerShape(16.dp , 16.dp , 16.dp , 3.dp)
        }else if (nextId == message.senderId){
            RoundedCornerShape(3.dp , 16.dp , 16.dp , 16.dp)
        } else {
            RoundedCornerShape(16.dp , 16.dp , 16.dp , 16.dp)
        }
    }

    val color = if (isCurrentUser) brush1 else brush2

    val alignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart

    val formatter = remember {
        SimpleDateFormat(("hh:mm a"), Locale.getDefault())
    }

    val interactionSource = remember{
        MutableInteractionSource()
    }

    val indication = rememberRipple(
        bounded = true,
        color = Color(0xFFFFFFFF)
    )

    val clkColor = Color.Transparent

    Box(modifier = Modifier.indication(interactionSource ,indication).background(
            clkColor
        ).fillMaxWidth(),
        contentAlignment = alignment
    ){
        Column(
            verticalArrangement = Arrangement.Bottom
        ) {

            Column(
                modifier = Modifier.shadow(2.dp , shape = shape).widthIn(max = 270.dp).fillMaxHeight().background(color , shape), horizontalAlignment = Alignment.End
            ) {
                if(message.content != ""){
                    Text(text = message.content , modifier = Modifier.padding(top = 5.dp , start = 10.dp , end = 10.dp), color = Color.White)
                }
            }
        }
    }
}
