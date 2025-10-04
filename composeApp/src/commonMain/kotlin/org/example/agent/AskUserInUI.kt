package org.example.agent

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

object AskUserInUI : SimpleTool<AskUserInUI.Args>() {
    private val _userInput: MutableStateFlow<String> = MutableStateFlow("")
    val userInput: StateFlow<String> = _userInput

    @Serializable
    data class Args(
        @property:LLMDescription("Message from the agent")
        val message: String
    )

    override val name: String = "__ask_user__"
    override val description: String = "Service tool, used by the agent to talk with user"
    override val argsSerializer: KSerializer<Args> = Args.serializer()

    fun setUserInput(input: String) {
        _userInput.value = input
    }

    override suspend fun doExecute(args: Args): String {
        println(args.message)
        return userInput.first { it.isNotEmpty() }.also {
            _userInput.value = ""
        }
    }
}
