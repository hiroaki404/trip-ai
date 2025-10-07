package org.example.trip_ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.agent.TripPlan
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun App() {
    val viewModel: ChatViewModel = viewModel { ChatViewModel() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MaterialTheme {
        AppContent(
            uiState = uiState,
            onUserInputChange = viewModel::updateUserInput,
            onSendMessage = viewModel::sendMessage
        )
    }
}

@Composable
fun AppContent(
    uiState: ChatUiState,
    onUserInputChange: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "旅行計画エージェント",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = uiState.userInput,
            onValueChange = onUserInputChange,
            label = { Text("旅行の内容を入力してください") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSendMessage,
            enabled = uiState.userInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (uiState.isLoading) "実行中..." else "送信")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }

        if (uiState.chatMessage.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                uiState.chatMessage.forEach { message ->
                    ChatMessageItem(message)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    when (message) {
        is ChatMessage.User -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "ユーザー",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        is ChatMessage.Assistant -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "アシスタント",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        is ChatMessage.ToolCall -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "ツール呼び出し",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        is ChatMessage.Structured -> {
            TripPlanCard(tripPlan = message.content)
        }
    }
}

@Composable
fun TripPlanCard(tripPlan: TripPlan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "旅行プラン",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tripPlan.summary,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            tripPlan.step.forEach { step ->
                StepCard(step = step)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun StepCard(step: TripPlan.Step) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = step.date,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            step.scheduleEntries.forEach { entry ->
                when (entry) {
                    is TripPlan.Step.ScheduleEntry.Activity -> ActivityItem(activity = entry)
                    is TripPlan.Step.ScheduleEntry.Transportation -> TransportationItem(transportation = entry)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ActivityItem(activity: TripPlan.Step.ScheduleEntry.Activity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row {
                Text(
                    text = "📍 ",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "${activity.duration} - ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = activity.location,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = activity.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun TransportationItem(transportation: TripPlan.Step.ScheduleEntry.Transportation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row {
                Text(
                    text = "🚃 ",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "${transportation.duration} - ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "${transportation.from} → ${transportation.to}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Row(modifier = Modifier.padding(start = 8.dp, top = 4.dp)) {
                Text(
                    text = "[${transportation.type}] ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = transportation.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppContentPreview() {
    val samplePlan = TripPlan(
        summary = "京都2泊3日の旅",
        step = listOf(
            TripPlan.Step(
                date = "1日目：2024年12月1日",
                scheduleEntries = listOf(
                    TripPlan.Step.ScheduleEntry.Transportation(
                        duration = "9:00-11:30",
                        from = "東京駅",
                        to = "京都駅",
                        type = "新幹線",
                        description = "のぞみ123号で移動"
                    ),
                    TripPlan.Step.ScheduleEntry.Activity(
                        duration = "12:00-13:00",
                        location = "京都駅周辺",
                        description = "昼食：京都ラーメン"
                    ),
                    TripPlan.Step.ScheduleEntry.Activity(
                        duration = "14:00-17:00",
                        location = "清水寺",
                        description = "清水寺を参拝、周辺の散策"
                    )
                )
            )
        )
    )

    val previewState = ChatUiState(
        userInput = "",
        isLoading = false,
        chatMessage = listOf(
            ChatMessage.User("京都に2泊3日で旅行に行きたいです"),
            ChatMessage.Assistant("承知しました。京都2泊3日の旅行プランを作成します。"),
            ChatMessage.Structured(samplePlan)
        )
    )

    MaterialTheme {
        AppContent(
            uiState = previewState,
            onUserInputChange = {},
            onSendMessage = {}
        )
    }
}
