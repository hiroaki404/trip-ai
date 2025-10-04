package org.example.trip_ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.agent.AskUserInUI
import org.example.agent.createTripAgent

data class ChatUiState(
    val userInput: String = "",
    val agentResponse: String = "",
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
            } else {
                _uiState.update { it.copy(isLoading = true, agentResponse = "") }
                try {
                    val agent = createTripAgent(askUser)
                    val response = agent.run(_uiState.value.userInput)
                    _uiState.update { it.copy(agentResponse = response, isLoading = false) }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            agentResponse = "エラーが発生しました: ${e.message}",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
}
