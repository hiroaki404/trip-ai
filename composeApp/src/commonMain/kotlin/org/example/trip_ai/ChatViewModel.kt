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

sealed interface ChatMessage {
    data class User(val content: String) : ChatMessage
    data class Assistant(val content: String) : ChatMessage
    data class ToolCall(val toolName: String, val content: String) : ChatMessage
    data class Structured(val content: TripPlan) : ChatMessage
}

data class ChatUiState(
    val userInput: String = "",
    val chatMessage: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false
)

class ChatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    val askUser = AskUserInUI

    fun updateUserInput(input: String) {
        _uiState.update { it.copy(userInput = input) }
    }

    fun sendMessage() {
        viewModelScope.launch {
            if (_uiState.value.isLoading) {
                askUser.setUserInput(_uiState.value.userInput)
                _uiState.update { it.copy(chatMessage = it.chatMessage + ChatMessage.User(it.userInput)) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        chatMessage = it.chatMessage + ChatMessage.User(it.userInput),
                    )
                }
                try {
                    val agent = createTripAgent(askUser) { message ->
                        when (message) {
                            is ChatMessage.ToolCall -> {
                                _uiState.update {
                                    it.copy(
                                        userInput = "",
                                        chatMessage = it.chatMessage + message,
//                                        isLoading = false
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
                    val response = agent.run(_uiState.value.userInput)
                    _uiState.update {
                        it.copy(
                            chatMessage = it.chatMessage + ChatMessage.Structured(response),
                            isLoading = false
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            chatMessage = it.chatMessage + ChatMessage.Assistant("Error: ${e.message}"),
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
}
