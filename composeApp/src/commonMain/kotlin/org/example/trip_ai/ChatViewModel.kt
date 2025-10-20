package org.example.trip_ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.agent.TripPlan
import org.example.agent.createTripAgent
import org.example.tools.AskUserInUI
import org.example.tools.FeedbackUserInUI

sealed interface ChatMessage {
    data class User(val content: String) : ChatMessage
    data class Assistant(val content: String) : ChatMessage
    data class AskToolCall(val content: String) : ChatMessage
    data class FeedbackToolCall(val plan: TripPlan) : ChatMessage
    data class ToolCall(val toolName: String, val content: String) : ChatMessage
    data class Structured(val content: TripPlan) : ChatMessage
}

data class ChatUiState(
    val userInput: String = "",
    val chatMessage: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isAgentActive: Boolean = false,
)

class ChatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    val askUser = AskUserInUI
    val feedbackTool = FeedbackUserInUI

    fun updateUserInput(input: String) {
        _uiState.update { it.copy(userInput = input) }
    }

    fun sendMessage() {
        viewModelScope.launch {
            if (_uiState.value.isAgentActive) {
                askUser.setUserInput(_uiState.value.userInput)
                _uiState.update {
                    it.copy(
                        userInput = "",
                        chatMessage = it.chatMessage + ChatMessage.User(it.userInput),
                        isLoading = true,
                    )
                }
            } else {
                val userInput = _uiState.value.userInput
                _uiState.update {
                    it.copy(
                        userInput = "",
                        isAgentActive = true,
                        isLoading = true,
                        chatMessage = it.chatMessage + ChatMessage.User(it.userInput),
                    )
                }
                try {
                    val agent = createTripAgent(askUser, feedbackTool) { message ->
                        when (message) {
                            is ChatMessage.AskToolCall -> {
                                _uiState.update {
                                    it.copy(
                                        chatMessage = it.chatMessage + message,
                                        isLoading = false
                                    )
                                }
                            }

                            is ChatMessage.FeedbackToolCall -> {
                                _uiState.update {
                                    it.copy(
                                        chatMessage = it.chatMessage + message,
                                        isLoading = false
                                    )
                                }
                            }

                            is ChatMessage.ToolCall -> {
                                _uiState.update {
                                    it.copy(
                                        chatMessage = it.chatMessage + message,
                                    )
                                }
                            }

                            is ChatMessage.Assistant -> {
                                _uiState.update {
                                    it.copy(chatMessage = it.chatMessage + message)
                                }
                            }

                            else -> {}
                        }
                    }
                    val response = agent.run(userInput)
                    _uiState.update {
                        it.copy(
                            chatMessage = it.chatMessage + ChatMessage.Structured(response),
                            isLoading = false
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            userInput = userInput, // rollback
                            chatMessage = it.chatMessage + ChatMessage.Assistant("Error: ${e.message}"),
                            isLoading = false,
                            isAgentActive = false
                        )
                    }
                }
            }
        }
    }
}
