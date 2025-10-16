package org.example.trip_ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.agent.TripPlan
import org.example.storage.LineStorage
import org.example.tokyoToKamakura
import org.example.trip_ai.theme.TripAITheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider
import org.jetbrains.compose.resources.painterResource
import trip_ai.composeapp.generated.resources.Res
import trip_ai.composeapp.generated.resources.ic_smart_toy

@Composable
fun App(modifier: Modifier = Modifier) {
    val viewModel: ChatViewModel = viewModel { ChatViewModel() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TripAITheme {
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
        TitleSection(modifier = Modifier.padding(bottom = 16.dp))

        // チャットメッセージリスト（スクロール可能）
        ChatMessageList(
            messages = uiState.chatMessage,
            isLoading = uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        // 入力エリア（画面下部に固定）
        ChatInputSection(
            userInput = uiState.userInput,
            isLoading = uiState.isLoading,
            onUserInputChange = onUserInputChange,
            onSendMessage = onSendMessage,
        )
    }
}

@Composable
fun TitleSection(
    modifier: Modifier = Modifier
) {
    Text(
        text = "旅行計画エージェント",
        style = MaterialTheme.typography.headlineMedium,
        modifier = modifier
    )
}

@Composable
fun ChatMessageList(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        if (messages.isNotEmpty()) {
            messages.forEach { message ->
                ChatMessageItem(message)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
fun ChatInputSection(
    userInput: String,
    isLoading: Boolean,
    onUserInputChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSendMessage: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        modifier = modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            value = userInput,
            onValueChange = onUserInputChange,
            label = { Text("旅行の内容を入力してください") },
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyDown && keyEvent.isMetaPressed && userInput.isNotBlank()) {
                        onSendMessage()
                        true
                    } else {
                        false
                    }
                },
            maxLines = 5
        )

        FilledTonalIconButton(
            onClick = onSendMessage,
            enabled = userInput.isNotBlank() && !isLoading,
            modifier = Modifier.align(androidx.compose.ui.Alignment.Bottom)
                .padding(bottom = 2.dp)
                .offset(x = 2.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "icon",
            )
        }
    }
}

@Composable
fun MessageCard(
    iconContent: @Composable () -> Unit,
    label: String,
    content: String?,
    containerColor: Color,
    contentColor: Color,
    alignEnd: Boolean = false,
    contentTextStyle: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        if (alignEnd) {
            Spacer(modifier = Modifier.weight(1f))
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = containerColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(
                topStart = if (alignEnd) 16.dp else 0.dp,
                topEnd = if (alignEnd) 0.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp,
            ),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    iconContent()
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = contentColor
                    )
                }
                content?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = content,
                        style = contentTextStyle,
                        color = contentColor
                    )
                }
            }
        }

        if (!alignEnd) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    when (message) {
        is ChatMessage.User -> {
            MessageCard(
                iconContent = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                },
                label = "ユーザー",
                content = message.content,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                alignEnd = true
            )
        }

        is ChatMessage.Assistant -> {
            MessageCard(
                iconContent = {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                },
                label = "アシスタント",
                content = message.content,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                alignEnd = false
            )
        }

        is ChatMessage.AskToolCall -> {
            MessageCard(
                iconContent = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_smart_toy),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                },
                label = "アシスタント（AskTool）",
                content = message.content,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface,
                alignEnd = false
            )
        }

        is ChatMessage.ToolCall -> {
            MessageCard(
                iconContent = {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                },
                label = "ツール: ${message.toolName}",
                content = null,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                alignEnd = false,
                contentTextStyle = MaterialTheme.typography.bodySmall
            )
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
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "旅行プラン",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
            val line = LineStorage.getLine(transportation.lineId)?.toCoordinate().orEmpty()
            TripMap(
                TransportationLine(line),
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

data class ChatInputState(
    val userInput: String,
    val isLoading: Boolean
)

class ChatInputPreviewParameterProvider : PreviewParameterProvider<ChatInputState> {
    val inputText = "日帰り鎌倉旅行の計画を立てて\n" +
            "東京から出発します。\n" +
            "10月中旬での予定で、夫婦二人でいきます。\n" +
            "特に行きたいところの希望はありません。"

    override val values = sequenceOf(
        ChatInputState("", false), // 入力なし
        ChatInputState(inputText, false), // 入力あり
        ChatInputState(inputText, true) // ローディング中
    )
}

@Preview(showBackground = true)
@Composable
fun ChatInputSectionPreview(
    @PreviewParameter(ChatInputPreviewParameterProvider::class) state: ChatInputState
) {
    TripAITheme {
        ChatInputSection(
            userInput = state.userInput,
            isLoading = state.isLoading,
            onUserInputChange = {},
            onSendMessage = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 1500)
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
        isLoading = true,
        chatMessage = listOf(
            ChatMessage.User(
                "日帰り鎌倉旅行の計画を立てて\n" +
                        "東京から出発します。\n" +
                        "10月中旬での予定で、夫婦二人でいきます。\n" +
                        "特に行きたいところの希望はありません。"
            ),
            ChatMessage.AskToolCall(
                content = "鎌倉への日帰り旅行のご相談ですね！承知いたしました。東京からのご出発で、10月中旬に夫婦お二人でのご旅行とのこと、かしこまりました。鎌倉は私も大好きな場所なので、ぜひ素敵な思い出作りのお手伝いをさせてください。予算についてお伺いしてもよろしいでしょうか？大体の目安で構いませんので、教えていただけますでしょうか。"
            ),
            ChatMessage.User("予算は一人2万円です"),
            ChatMessage.AskToolCall(
                content = "ご予算は一人2万円ですね、承知いたしました。鎌倉は日帰りでも十分に楽しめるスポットがたくさんあります。せっかくなので、お二人のご興味や体力に合わせて、どのような過ごし方がご希望かもう少し詳しくお伺いしてもよろしいでしょうか？例えば、お寺巡りがお好きとか、海を見ながらのんびりしたい、美味しいものを食べ歩きしたい、など何かイメージがあれば教えてください。"
            ),
            ChatMessage.User("特にありません"),
            ChatMessage.ToolCall(
                toolName = "websearch",
                content = "search is Called"
            ),
            ChatMessage.ToolCall(
                toolName = "scrape",
                content = "scrape is Called"
            ),
            ChatMessage.ToolCall(
                toolName = "directions",
                content = "directions is Called"
            ),
            ChatMessage.Structured(samplePlan)
        )
    )

    TripAITheme {
        AppContent(
            uiState = previewState,
            onUserInputChange = {},
            onSendMessage = {}
        )
    }
}
