package cs.pr.subspacetunnels.world

import cs.pr.subspacetunnels.world.Informer.log
import cs.pr.subspacetunnels.world.Informer.time
import mpi.Intracomm
import mpi.MPI
import mu.KLogging
import java.lang.Math.max

class WorldProxy(private val world: Intracomm) {
    private val bcastReceivers = (0 until world.Size()).filter { it != world.Rank() }.toList()

    fun rank() = world.Rank()
    fun size() = world.Size()

    fun sendRequest(request: Request) {
        log("sending request ${request.requestId}")
        sendToAll(request, Tag.REQUEST_TAG)
    }

    fun sendRelease(release: Request) {
        log("sending release for request ${release.requestId}")
        sendToAll(release, Tag.REQUEST_TAG)
    }

    private fun sendToAll(message: Request, tag: Tag) {
        message.time = ++time
        val reqPacked = arrayOf(message)
        bcastReceivers.forEach {
            world.Isend(reqPacked, 0, 1, MPI.OBJECT, it, tag.num)
        }
    }

    fun receiveRequests(): Request = receiveMessages(Tag.REQUEST_TAG)

    fun receiveAccepts(request: Request) {
        var left = bcastReceivers - 1
        while(left.isNotEmpty()) {
            val acceptance = receiveMessages<Acceptance>(Tag.ACCEPT_TAG)
            left -= acceptance.senderId
        }
        log("received all accepts for request ${request.requestId}")
    }

    private inline fun <reified M: Message>receiveMessages(tag: Tag): M {
        val buffer = arrayOfNulls<M>(1)
        world.Recv(buffer, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, tag.num)
        val message = buffer[0]!!
        time = max(message.time, time) + 1
        return message
    }

    fun sendAccept(acceptance: Acceptance, receiver: Int) {
        acceptance.time = ++time
        log("sending accept ${acceptance.acceptId} for request ${acceptance.requestId}")
        val accPacked = arrayOf(acceptance)
        world.Send(accPacked, 0, 1, MPI.OBJECT, receiver, Tag.ACCEPT_TAG.num)
    }

    fun stopTheWorld() {
        log("requested stop the world")
        sendToAll(Request.END, Tag.TECH_MESSAGE)
        sendToAll(Request.END, Tag.REQUEST_TAG)
    }

    fun waitForWorldEnd() {
        val worldEndMessage = receiveMessages<Message>(Tag.TECH_MESSAGE)
        if (worldEndMessage.requestId != Request.END.requestId)
            throw Error("Invalid world end message")
    }

    enum class Tag(val num: Int, val text: String) {
        REQUEST_TAG(1, "Request/Release"),
        RELEASE_TAG(2, "Request/Release"),
        ACCEPT_TAG(3, "Accept"),
        TECH_MESSAGE(4, "Tech")
    }
}