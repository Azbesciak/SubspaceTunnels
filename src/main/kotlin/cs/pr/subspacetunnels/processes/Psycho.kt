package cs.pr.subspacetunnels.processes

import cs.pr.subspacetunnels.processes.messagesreceiver.Messenger
import cs.pr.subspacetunnels.processes.requester.Requester
import cs.pr.subspacetunnels.world.SubSpace
import cs.pr.subspacetunnels.world.WorldProxy
import kotlinx.coroutines.experimental.launch

class Psycho(world: WorldProxy) : Process(world, SubSpace()) {
    private val subprocesses = listOf(Requester(world, subSpace), Messenger(world, subSpace))
    override suspend fun run() {
        subprocesses.forEach {
            launch { it.run() }
        }
    }

    override fun stop() {
        subprocesses.forEach { it.stop() }
    }

    fun waitForWorldEnd() = world.waitForWorldEnd()

    fun stopTheWorld() = world.stopTheWorld()
}