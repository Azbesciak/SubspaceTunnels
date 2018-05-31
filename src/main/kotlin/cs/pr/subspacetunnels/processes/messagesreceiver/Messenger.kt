package cs.pr.subspacetunnels.processes.messagesreceiver

import cs.pr.subspacetunnels.processes.Process
import cs.pr.subspacetunnels.world.*
import cs.pr.subspacetunnels.world.Informer.log

class Messenger(world: WorldProxy, subSpace: SubSpace): Process(world, subSpace) {
    override fun run() {
        if (isRunning.getAndSet(true)) return
        while (isRunning.get()) {
            val message = world.receiveMessages()
            log("received $message")
            when (message) {
                is Release -> {
                    subSpace.free(message)
                }
                is Request -> {
                    subSpace.add(message)
                    world.sendAccept(message, message.senderId)
                }
            }
        }
    }
}