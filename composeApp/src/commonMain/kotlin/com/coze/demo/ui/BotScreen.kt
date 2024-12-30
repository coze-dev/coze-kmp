package com.coze.demo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.coze.api.model.bot.BotInfo
import com.coze.api.model.bot.SimpleBot
import com.coze.demo.BotDemo
import com.coze.demo.ui.components.ErrorMessage
import kotlinx.coroutines.launch

private val cardShape = RoundedCornerShape(4.dp)
private val buttonShape = RoundedCornerShape(4.dp)
private val itemShape = RoundedCornerShape(2.dp)

@Composable
fun BotScreen() {
    val botDemo = remember { BotDemo() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var botName by remember { mutableStateOf("") }
    var botDescription by remember { mutableStateOf("") }
    var botId by remember { mutableStateOf("") }
    var botInfo by remember { mutableStateOf<BotInfo?>(null) }
    var botResult by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Loading states
    var isCreateBotLoading by remember { mutableStateOf(false) }
    var isListBotsLoading by remember { mutableStateOf(false) }
    var isGetBotLoading by remember { mutableStateOf(false) }
    var isPublishBotLoading by remember { mutableStateOf(false) }

    var botList by remember { mutableStateOf<List<SimpleBot>>(emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 错误信息显示
        errorMessage?.let {
            ErrorMessage(
                message = it,
                onDismiss = { errorMessage = null }
            )
        }

        // Bot 创建区域
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = 4.dp,
            backgroundColor = MaterialTheme.colors.surface,
            shape = cardShape
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "创建 Bot",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.primary
                )
                OutlinedTextField(
                    value = botName,
                    onValueChange = { botName = it },
                    label = { Text("Bot 名称") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCreateBotLoading,
                    shape = MaterialTheme.shapes.small,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colors.primary,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                    )
                )
                OutlinedTextField(
                    value = botDescription,
                    onValueChange = { botDescription = it },
                    label = { Text("Bot 描述") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCreateBotLoading,
                    shape = MaterialTheme.shapes.small,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colors.primary,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                    )
                )
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isCreateBotLoading = true
                            try {
                                val newBotId = botDemo.createBot(botName, botDescription)
                                botId = newBotId
                                botResult = "Bot 创建成功: $newBotId"
                                errorMessage = null
                            } catch (e: Exception) {
                                println("[Error] Create bot failed: ${e.message}")
                                errorMessage = e.message
                            } finally {
                                isCreateBotLoading = false
                            }
                        }
                    },
                    enabled = !isCreateBotLoading && botName.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = MaterialTheme.colors.onPrimary,
                        disabledBackgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                    )
                ) {
                    if (isCreateBotLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colors.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("创建 Bot")
                    }
                }
            }
        }

        // Bot 操作区域
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp,
            backgroundColor = MaterialTheme.colors.surface,
            shape = cardShape
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Bot 操作",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.primary
                )
                OutlinedTextField(
                    value = botId,
                    onValueChange = { botId = it },
                    label = { Text("Bot ID") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isListBotsLoading && !isGetBotLoading && !isPublishBotLoading,
                    shape = MaterialTheme.shapes.small,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colors.primary,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isListBotsLoading = true
                                try {
                                    val bots = botDemo.listBots()
                                    if (bots.spaceBots.isNotEmpty()) {
                                        botId = bots.spaceBots.first().botId
                                    }
                                    botList = bots.spaceBots
                                    errorMessage = null
                                } catch (e: Exception) {
                                    println("[Error] List bots failed: ${e.message}")
                                    errorMessage = e.message
                                    botList = emptyList()
                                } finally {
                                    isListBotsLoading = false
                                }
                            }
                        },
                        enabled = !isListBotsLoading,
                        modifier = Modifier.weight(1f),
                        shape = buttonShape,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary,
                            contentColor = MaterialTheme.colors.onPrimary,
                            disabledBackgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                        )
                    ) {
                        if (isListBotsLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colors.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("获取列表")
                        }
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isGetBotLoading = true
                                try {
                                    botInfo = botDemo.getBot(botId)
                                    botResult = if (botInfo == null) {
                                        "Bot 不存在"
                                    } else {
                                        buildString {
                                            appendLine("[Bot 详情]")
                                            appendLine("ID: ${botInfo?.botId}")
                                            appendLine("名称: ${botInfo?.name}")
                                            appendLine("描述: ${botInfo?.description ?: "N/A"}")
                                            appendLine("版本: ${botInfo?.version ?: "N/A"}")
                                        }
                                    }
                                    errorMessage = null
                                } catch (e: Exception) {
                                    println("[Error] Get bot failed: ${e.message}")
                                    errorMessage = e.message
                                    botInfo = null
                                    botResult = "获取 Bot 详情失败：${e.message}"
                                } finally {
                                    isGetBotLoading = false
                                }
                            }
                        },
                        enabled = !isGetBotLoading && botId.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        shape = buttonShape,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary,
                            contentColor = MaterialTheme.colors.onPrimary,
                            disabledBackgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                        )
                    ) {
                        if (isGetBotLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colors.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("获取详情")
                        }
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isPublishBotLoading = true
                                try {
                                    botDemo.publishBot(botId)
                                    botResult = "Bot 发布成功"
                                    errorMessage = null
                                } catch (e: Exception) {
                                    println("[Error] Publish bot failed: ${e.message}")
                                    errorMessage = e.message
                                    botResult = "发布 Bot 失败：${e.message}"
                                } finally {
                                    isPublishBotLoading = false
                                }
                            }
                        },
                        enabled = !isPublishBotLoading && botId.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        shape = buttonShape,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary,
                            contentColor = MaterialTheme.colors.onPrimary,
                            disabledBackgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                        )
                    ) {
                        if (isPublishBotLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colors.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("发布")
                        }
                    }
                }

                if (botList.isNotEmpty()) {
                    Text(
                        "共 ${botList.size} 条记录",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.primary
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        botList.forEach { bot ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = 2.dp,
                                shape = itemShape,
                                backgroundColor = MaterialTheme.colors.surface
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            bot.botName,
                                            style = MaterialTheme.typography.subtitle1,
                                            color = MaterialTheme.colors.primary
                                        )
                                        Text(
                                            "ID: ${bot.botId}",
                                            style = MaterialTheme.typography.caption,
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    if (bot.description.isNotEmpty()) {
                                        Text(
                                            bot.description,
                                            style = MaterialTheme.typography.body2,
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                                        )
                                    }
                                    Text(
                                        "发布时间: ${bot.publishTime}",
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 结果显示区域
        if (botResult.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                elevation = 4.dp,
                backgroundColor = MaterialTheme.colors.surface,
                shape = cardShape
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "操作结果",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.primary
                    )
                    Text(
                        botResult,
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface
                    )
                }
            }
        }
    }
} 