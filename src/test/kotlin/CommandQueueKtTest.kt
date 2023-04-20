import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CommandTest {

    @Test
    fun convertStringsToCommands() {
        assertEquals(SdCommand.IncrementCFGScale,parseCommand("sd cfg increment").getOrThrow())
        assertEquals(SdCommand.DecrementCFGScale,parseCommand("sd cfg decrement").getOrThrow())
        assertEquals(SdCommand.IncrementHeight(),parseCommand("sd height increment").getOrThrow())
        assertEquals(SdCommand.DecrementHeight(),parseCommand("sd height decrement").getOrThrow())
        assertEquals(SdCommand.IncrementWidth(), parseCommand("sd width increment").getOrThrow())
        assertEquals(SdCommand.DecrementWidth(), parseCommand("sd width decrement").getOrThrow())
        assertEquals(SdCommand.NewSeed, parseCommand("sd seed").getOrThrow())
        assertEquals(SdCommand.IncrementSamplingSteps, parseCommand("sd steps increment").getOrThrow())

        assertEquals(SdCommand.AddTerm("banana pie", -1), parseCommand("sd prompt add -1 banana pie").getOrThrow())
        assertEquals(SdCommand.AddTerm("lemon pie", 3), parseCommand("sd prompt add 3 lemon pie").getOrThrow())
        assertEquals(SdCommand.RemoveTerm(5), parseCommand("sd prompt remove 5").getOrThrow())
        assertEquals(SdCommand.EmphasizeTerm(8), parseCommand("sd prompt increment 8").getOrThrow())
        assertEquals(SdCommand.DeemphasizeTerm(2), parseCommand("sd prompt decrement 2").getOrThrow())

        assertEquals(SdCommand.AddNegativeTerm("banana pie", -1), parseCommand("sd nprompt add -1 banana pie").getOrThrow())
        assertEquals(SdCommand.AddNegativeTerm("lemon pie", 3), parseCommand("sd nprompt add 3 lemon pie").getOrThrow())
        assertEquals(SdCommand.RemoveNegativeTerm(5), parseCommand("sd nprompt remove 5").getOrThrow())
        assertEquals(SdCommand.EmphasizeNegativeTerm(8), parseCommand("sd nprompt increment 8").getOrThrow())
        assertEquals(SdCommand.DeemphasizeNegativeTerm(2), parseCommand("sd nprompt decrement 2").getOrThrow())
    }
}