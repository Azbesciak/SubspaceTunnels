package cs.pr.subspacetunnels.processes.messagesreceiver

import cs.pr.subspacetunnels.processes.Process
import cs.pr.subspacetunnels.world.*
import cs.pr.subspacetunnels.world.Informer.log

class Messenger(world: WorldProxy, subSpace: SubSpace): Process(world, subSpace) {
    override fun run() {
        if (isRunning.getAndSet(true)) return
        while (isRunning.get()) {
            val message = world.receiveRequests()
            if (message.isRunning) {
                log("received release $message")
                subSpace.free(message)
            } else {
                log("received request $message")
                subSpace.add(message)
                world.sendAccept(message, message.senderId)
            }
        }
    }
}