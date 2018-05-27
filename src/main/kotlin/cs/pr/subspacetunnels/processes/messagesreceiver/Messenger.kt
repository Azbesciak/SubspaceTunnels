package cs.pr.subspacetunnels.processes.messagesreceiver

import cs.pr.subspacetunnels.processes.Process
import cs.pr.subspacetunnels.world.*

class Messenger(world: WorldProxy, subSpace: SubSpace): Process(world, subSpace) {
    override suspend fun run() {
        if (isRunning.getAndSet(true)) return
        while (isRunning.get()) {
            val message = world.receiveMessage()
            when(message) {
                is Request -> {
                    subSpace.add(message)
                    world.sendAccept(Acceptance(Message.createId(), message.requestId, id), message.senderId)
                }
                is Release -> subSpace.free(message)
            }
        }
    }
}