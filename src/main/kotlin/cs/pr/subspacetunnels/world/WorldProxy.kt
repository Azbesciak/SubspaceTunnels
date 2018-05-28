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

    private inline fun <reified M: Message>sendToSelf(request: M, tag: Tag) {
        send(request, rank(), tag)
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
        log("sending accept ${acceptance.acceptId} for request ${acceptance.requestId}")
        send(acceptance, receiver, Tag.ACCEPT_TAG)
    }

    private inline fun <reified M: Message>send(message: M, receiver: Int, tag: Tag) {
        message.time = ++time
        val accPacked = arrayOf(message)
        world.Isend(accPacked, 0, 1, MPI.OBJECT, receiver, tag.num)
    }

    enum class Tag(val num: Int, val text: String) {
        REQUEST_TAG(1, "Request/Release"),
        ACCEPT_TAG(2, "Accept")
    }
}