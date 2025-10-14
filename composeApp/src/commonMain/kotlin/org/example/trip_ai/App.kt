package org.example.trip_ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.agent.TripPlan
import org.example.tokyoToKamakura
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun App(modifier: Modifier = Modifier) {
    val viewModel: ChatViewModel = viewModel { ChatViewModel() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MaterialTheme {
        AppContent(
            modifier = modifier,
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
    modifier: Modifier = Modifier,
    onSendMessage: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
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
                .height(120.dp)
                .focusRequester(focusRequester)
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyDown && keyEvent.isMetaPressed && uiState.userInput.isNotBlank()) {
                        onSendMessage()
                        true
                    } else {
                        false
                    }
                },
            maxLines = 5
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
            TripMap(
                ActivityPoint(Coordinate(activity.longitude, activity.latitude)),
                modifier = Modifier.height(150.dp).clip(RoundedCornerShape(8.dp))
            )
            Row(modifier = Modifier.padding(top = 8.dp)) {
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
            TripMap(
                TransportationLine(emptyList()), // TODO: get line points from lineId
                modifier = Modifier.height(150.dp).clip(RoundedCornerShape(8.dp))
            )
            Row(modifier = Modifier.padding(top = 8.dp)) {
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

@Preview(showBackground = true, heightDp = 1200)
@Composable
fun AppContentPreview() {
    val lineMap = mapOf(
        "1" to tokyoToKamakura,
        "2" to listOf(
            Coordinate(longitude = 139.556443, latitude = 35.326124),
            Coordinate(longitude = 139.556, latitude = 35.325),
            Coordinate(longitude = 139.555, latitude = 35.324),
            Coordinate(longitude = 139.554, latitude = 35.323),
            Coordinate(longitude = 139.553571, latitude = 35.321708)
        )
    )

    val samplePlan = TripPlan(
        summary = "東京駅発の日帰り鎌倉旅プランです。10月中旬の穏やかな秋晴れの中、夫婦での街歩きと歴史散策を中心に、鶴岡八幡宮や長谷寺、大仏、高徳院を巡ります。",
        step = listOf(
            TripPlan.Step(
                date = "2023年10月15日",
                scheduleEntries = listOf(
                    TripPlan.Step.ScheduleEntry.Activity(
                        duration = "07:30-07:40",
                        location = "東京駅 新幹線南口改札付近",
                        description = "朝の東京駅で切符を購入。JR横須賀線快速鎌倉行きに乗車。",
                        longitude = 139.767125,
                        latitude = 35.681236
                    ),
                    TripPlan.Step.ScheduleEntry.Transportation(
                        duration = "07:40-08:40",
                        from = "東京駅",
                        to = "鎌倉駅",
                        type = "電車",
                        description = "JR横須賀線快速（運賃片道約620円）。車窓から東京湾や横浜の景色を楽しめます。",
                        lineId = "1"
                    ),
                    TripPlan.Step.ScheduleEntry.Activity(
                        duration = "08:50-09:30",
                        location = "鶴岡八幡宮",
                        description = "12世紀創建の鎌倉八幡宮で、参道の大鳥居や本宮を参拝。源氏池周辺の散策も◎。",
                        longitude = 139.556443,
                        latitude = 35.326124
                    ),
                    TripPlan.Step.ScheduleEntry.Transportation(
                        duration = "09:30-09:35",
                        from = "鶴岡八幡宮",
                        to = "小町通り入口",
                        type = "徒歩",
                        description = "鎌倉駅方面へ下る小道を散策しながら移動。",
                        lineId = "2"
                    ),
                )
            )
        )
    )

    val previewState = ChatUiState(
        userInput = "",
        isLoading = false,
        chatMessage = listOf(
            ChatMessage.User("東京駅から日帰りで鎌倉に行きたいです"),
            ChatMessage.Assistant("承知しました。東京駅発の日帰り鎌倉旅行プランを作成します。"),
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
