import kotlin.time.Duration

sealed class Command

sealed class SdCommand : Command() {
    data class AddTerm(val term: String, val position: Int) : SdCommand() {
        override fun toString(): String {
            return "Prompt: Add [$position] $term"
        }
    }
    data class RemoveTerm(val position: Int) : SdCommand() {
        override fun toString(): String {
            return "Prompt: Remove [$position]"
        }
    }
    data class UpdateTerm(val term: String, val position: Int) : SdCommand() {
        override fun toString(): String {
            return "Prompt: Update [$position] $term"
        }
    }
    data class EmphasizeTerm(val position: Int) : SdCommand() {
        override fun toString(): String {
            return "Prompt: [$position][Weight + 1]"
        }
    }
    data class DeemphasizeTerm(val position: Int) : SdCommand() {
        override fun toString(): String {
            return "Prompt: [$position][Weight - 1]"
        }
    }
    data class AddNegativeTerm(val term: String, val position: Int) : SdCommand() {
        override fun toString(): String {
            return "Negative Prompt: Add [$position] $term"
        }
    }
    data class UpdateNegativeTerm(val term: String, val position: Int) : SdCommand() {
        override fun toString(): String {
            return "Negative Prompt: Update [$position] $term"
        }
    }
    data class RemoveNegativeTerm(val position: Int) : SdCommand() {
        override fun toString(): String {
            return "Negative Prompt: Remove [$position]"
        }
    }
    data class EmphasizeNegativeTerm(val position: Int) : SdCommand() {
        override fun toString(): String {
            return "Negative Prompt: [$position][Weight + 1]"
        }
    }
    data class DeemphasizeNegativeTerm(val position: Int) : SdCommand() {
        override fun toString(): String {
            return "Negative Prompt: [$position][Weight - 1]"
        }
    }
    data class IncrementHeight(val dimension: Int = 8) : SdCommand() {
        override fun toString(): String {
            return "Height Increased"
        }
    }
    data class DecrementHeight(val dimension: Int = 8) : SdCommand() {
        override fun toString(): String {
            return "Height Decreased"
        }
    }
    data class IncrementWidth(val dimension: Int = 8) : SdCommand() {
        override fun toString(): String {
            return "Width Increased"
        }
    }
    data class DecrementWidth(val dimension: Int = 8) : SdCommand() {
        override fun toString(): String {
            return "Width Decreased"
        }
    }
    object IncrementSamplingSteps : SdCommand() {
        override fun toString(): String {
            return "SamplingSteps +1"
        }
    }
    object DecrementSamplingSteps : SdCommand() {
        override fun toString(): String {
            return "SamplingSteps -1"
        }
    }
    object IncrementCFGScale : SdCommand() {
        override fun toString(): String {
            return "CFG Increased"
        }
    }
    object DecrementCFGScale : SdCommand() {
        override fun toString(): String {
            return "CFG Decreased"
        }
    }
    object NewSeed : SdCommand() {
        override fun toString(): String {
            return "X"
        }
    }
    object Noop : SdCommand() {
        override fun toString(): String {
            return "Nothing"
        }
    }
}

sealed class AdminCommand : Command() {
    data class SetBufferTime(val duration: Duration): AdminCommand()
    data class ListenToChat(val listenToChat: Boolean): AdminCommand()
    data class SetIdleMode(val idleMode: Boolean): AdminCommand()
    object RunIdleCommand: AdminCommand()
    object RunCommandQueue: AdminCommand()
    object StopCommandQueue: AdminCommand()
}