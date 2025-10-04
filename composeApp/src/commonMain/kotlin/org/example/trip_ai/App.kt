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
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val viewModel: ChatViewModel = viewModel { ChatViewModel() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MaterialTheme {
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
                onValueChange = { viewModel.updateUserInput(it) },
                label = { Text("旅行の内容を入力してください") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = viewModel::sendMessage,
                enabled = uiState.userInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isLoading) "実行中..." else "送信")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }

            if (uiState.agentResponse.isNotEmpty()) {
                Text(
                    text = "結果:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text = uiState.agentResponse,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}
