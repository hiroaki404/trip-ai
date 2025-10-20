package org.example.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.example.agent.TripPlan

object FeedbackUserInUI : SimpleTool<FeedbackUserInUI.Args>() {
    private val _userFeedback: MutableStateFlow<String> = MutableStateFlow("")
    val userFeedback: StateFlow<String> = _userFeedback

    @Serializable
    data class Args(
        @property:LLMDescription("Trip plan to show to the user for feedback")
        val plan: TripPlan
    )

    override val name: String = "__feedback_user__"
    override val description: String = "Service tool, used by the agent to show trip plan to user and get feedback"
    override val argsSerializer: KSerializer<Args> = Args.serializer()

    fun setUserFeedback(feedback: String) {
        _userFeedback.value = feedback
    }

    override suspend fun doExecute(args: Args): String {
        println("Showing trip plan to user: ${args.plan}")
        return userFeedback.first { it.isNotEmpty() }.also {
            _userFeedback.value = ""
        }
    }
}
