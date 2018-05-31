package cs.pr.subspacetunnels.processes

import cs.pr.subspacetunnels.SubspaceSettings
import cs.pr.subspacetunnels.processes.messagesreceiver.Messenger
import cs.pr.subspacetunnels.processes.requester.Requester
import cs.pr.subspacetunnels.world.Informer.log
import cs.pr.subspacetunnels.world.SubSpace
import cs.pr.subspacetunnels.world.WorldProxy

class Psycho(world: WorldProxy, settings: SubspaceSettings) : Process(world, SubSpace(settings, world.size())) {
    private val subprocesses = listOf(Requester(world, subSpace, settings), Messenger(world, subSpace))
    override fun run() {
        subprocesses.forEach {
            launch { it.run() }
        }
    }

    override fun stop() {
        subprocesses.forEach { it.stop() }
    }

    fun showSubspace(onlyActive: Boolean) {
        if (onlyActive && !subSpace.isWorldEnabled)
            return
        log(subSpace.toString(), 999)
    }

}