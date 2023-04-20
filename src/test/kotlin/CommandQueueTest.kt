import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.random.Random

class CommandQueueTest {

    lateinit var commandBuffer: CommandBuffer

    var random: Random = Random(123)

    @BeforeEach
    fun beforeTest() {
        commandBuffer = CommandBuffer(random)
    }

    @Test
    fun `flush empty queue`() = runTest {
        assertNull(commandBuffer.flush())
    }

    @Test
    fun `handle receiving three commands`() = runTest {
        commandBuffer.store("a", SdCommand.IncrementSamplingSteps)
        commandBuffer.store("b", SdCommand.NewSeed)
        commandBuffer.store("c", SdCommand.DecrementCFGScale)

        assertNotNull(commandBuffer.flush())
    }

}