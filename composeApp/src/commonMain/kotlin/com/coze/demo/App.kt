package com.coze.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
fun App() {
    val authDemo = AuthDemo
    val coroutineScope = rememberCoroutineScope()

    MaterialTheme {
        var inputText by remember { mutableStateOf("Why is the sky always blue") }
        var displayText by remember { mutableStateOf("") }
        var userMsg by remember { mutableStateOf("") }
        // Define greetings as a list of strings
        var greetings by remember { mutableStateOf(listOf<String>()) }
        var authResult by remember { mutableStateOf("") }

        LaunchedEffect(greetings) {
            displayText = greetings.joinToString("\n")
        }
        val chatDemo = ChatDemo()
//        val coroutineScope = rememberCoroutineScope()

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Chat With Coze Bot.") }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    coroutineScope.launch {
                        userMsg = inputText
                        inputText = ""
                        var text = "[Coze Response (Stream)]\n"
                        chatDemo.streamTest(userMsg).collect { phrase ->
                            text += phrase
                            greetings = greetings.dropLast(1) + text
                            displayText = greetings.joinToString("\n")
                        }
                    }
                }) {
                    Text("Send [Stream]")
                }
                Button(onClick = {
                    coroutineScope.launch {
                        userMsg = inputText
                        inputText = ""
                        var text = "[Coze Response (Non-Stream)]\n"
                        chatDemo.noneStreamCreateAndPoll(userMsg).collect { phrase ->
                            text += phrase
                            greetings = greetings.dropLast(1) + text
                            displayText = greetings.joinToString("\n")
                        }
                    }
                }) {
                    Text("Send [Non-Stream]")
                }
            }
            if (userMsg.isNotEmpty()) {
                Card(
                    modifier = Modifier.padding(8.dp),
                    elevation = 4.dp
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = "[User Msg]\n$userMsg",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                Divider()
            }
            if (displayText.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .height(500.dp), // Set card height
                    elevation = 4.dp
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = displayText,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            // 添加分隔线
            Divider()
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = {
                    coroutineScope.launch {
                        authResult = authDemo.testJWTAuth() ?: ""
                    }
                }) {
                    Text("Test JWT Auth")
                }
            }
            if (authResult.isNotEmpty()) {
                Card(
                    modifier = Modifier.padding(8.dp),
                    elevation = 4.dp
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = "[Auth Result]\n$authResult",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
