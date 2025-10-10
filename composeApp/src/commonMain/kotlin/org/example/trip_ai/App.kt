package org.example.trip_ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.agent.TripPlan
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
                TransportationLine(
                    transportation.line.map { point ->
                        Coordinate(point.longitude, point.latitude)
                    }
                ),
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
                        line = listOf(
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.767125, latitude = 35.681236),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.745, latitude = 35.658),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.720, latitude = 35.635),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.695, latitude = 35.610),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.670, latitude = 35.585),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.645, latitude = 35.560),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.620, latitude = 35.535),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.595, latitude = 35.520),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.575, latitude = 35.510),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.555, latitude = 35.505),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.534, latitude = 35.503)
                        )
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
                        line = listOf(
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.556443, latitude = 35.326124),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.556, latitude = 35.325),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.555, latitude = 35.324),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.554, latitude = 35.323),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.553571, latitude = 35.321708)
                        )
                    ),
                    TripPlan.Step.ScheduleEntry.Activity(
                        duration = "09:35-10:20",
                        location = "鎌倉小町通り",
                        description = "「鎌倉紅谷」のクルミっ子、「竹之内豆腐店」の揚げたて豆腐などを楽しみつつ土産物店をチェック。",
                        longitude = 139.550721,
                        latitude = 35.317292
                    ),
                    TripPlan.Step.ScheduleEntry.Transportation(
                        duration = "10:20-10:30",
                        from = "鎌倉駅",
                        to = "長谷駅",
                        type = "電車",
                        description = "江ノ電鎌倉駅2番線から長谷行き乗車（運賃200円）。海沿いの風景が魅力。",
                        line = listOf(
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.550721, latitude = 35.317292),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.549, latitude = 35.317),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.548, latitude = 35.317),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.547, latitude = 35.317),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.546, latitude = 35.317),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.545, latitude = 35.317),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.544, latitude = 35.317),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.543, latitude = 35.317),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.542157, latitude = 35.31682)
                        )
                    ),
                    TripPlan.Step.ScheduleEntry.Activity(
                        duration = "10:30-11:30",
                        location = "長谷寺",
                        description = "海を望む庭園と紅葉が始まる境内を散策。本尊十一面観音像を拝観（拝観料300円）。",
                        longitude = 139.533571,
                        latitude = 35.316693
                    ),
                    TripPlan.Step.ScheduleEntry.Transportation(
                        duration = "11:30-11:40",
                        from = "長谷寺",
                        to = "高徳院（大仏）",
                        type = "徒歩",
                        description = "長谷の通り沿いに歩くと大仏のシルエットが見えてきます。",
                        line = listOf(
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.533571, latitude = 35.316693),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.5334, latitude = 35.3166),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.5333, latitude = 35.3165),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.533315, latitude = 35.316458)
                        )
                    ),
                    TripPlan.Step.ScheduleEntry.Activity(
                        duration = "11:40-12:10",
                        location = "高徳院",
                        description = "国宝・鎌倉大仏を間近で見学。胎内にも入れる（拝観料200円）。写真スポット多数。",
                        longitude = 139.533058,
                        latitude = 35.316223
                    ),
                    TripPlan.Step.ScheduleEntry.Transportation(
                        duration = "12:10-12:13",
                        from = "高徳院最寄り長谷駅",
                        to = "由比ヶ浜駅",
                        type = "電車",
                        description = "昼食の「しらす問屋 とびっちょ」最寄り駅へ移動（運賃200円）。",
                        line = listOf(
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.542157, latitude = 35.31682),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.543, latitude = 35.317),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.543893, latitude = 35.317393)
                        )
                    ),
                    TripPlan.Step.ScheduleEntry.Activity(
                        duration = "12:15-13:15",
                        location = "しらす問屋 とびっちょ 由比ヶ浜店",
                        description = "生しらす丼や海鮮丼が名物。季節限定メニューも豊富（目安2,000円～）。",
                        longitude = 139.553807,
                        latitude = 35.317979
                    ),
                    TripPlan.Step.ScheduleEntry.Transportation(
                        duration = "13:15-13:25",
                        from = "由比ヶ浜駅",
                        to = "鎌倉駅",
                        type = "電車",
                        description = "江ノ電で鎌倉駅へ戻ります（運賃200円）。",
                        line = listOf(
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.543893, latitude = 35.317393),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.545, latitude = 35.317),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.547, latitude = 35.317),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.549, latitude = 35.317),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.551, latitude = 35.317),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.552561, latitude = 35.317639)
                        )
                    ),
                    TripPlan.Step.ScheduleEntry.Activity(
                        duration = "13:30-15:00",
                        location = "鎌倉駅周辺 or 江の島",
                        description = "鎌倉駅周辺でカフェ巡り（おすすめ「珈琲日記」）か、江ノ電で江の島へ足を伸ばす（往復運賃400円、徒歩展望灯台など）。",
                        longitude = 139.551317,
                        latitude = 35.318662
                    ),
                    TripPlan.Step.ScheduleEntry.Activity(
                        duration = "15:00-15:45",
                        location = "珈琲日記",
                        description = "湘南の海風を感じるテラス席で、鎌倉ブレンドを味わう（目安800円）。",
                        longitude = 139.550904,
                        latitude = 35.318098
                    ),
                    TripPlan.Step.ScheduleEntry.Transportation(
                        duration = "16:00-17:00",
                        from = "鎌倉駅",
                        to = "東京駅",
                        type = "電車",
                        description = "JR横須賀線快速で帰路へ。車内でお土産（クルミっ子など）を開封しながら湯河原の海景色を楽しめます。",
                        line = listOf(
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.534, latitude = 35.503),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.555, latitude = 35.505),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.575, latitude = 35.510),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.595, latitude = 35.520),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.620, latitude = 35.535),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.645, latitude = 35.560),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.670, latitude = 35.585),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.695, latitude = 35.610),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.720, latitude = 35.635),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.745, latitude = 35.658),
                            TripPlan.Step.ScheduleEntry.Transportation.Point(longitude = 139.767125, latitude = 35.681236)
                        )
                    )
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
