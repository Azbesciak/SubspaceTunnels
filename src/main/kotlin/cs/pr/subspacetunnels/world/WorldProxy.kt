package cs.pr.subspacetunnels.world

import cs.pr.subspacetunnels.world.Informer.log
import cs.pr.subspacetunnels.world.Informer.time
import mpi.Intracomm
import mpi.MPI
import java.lang.Math.max
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class WorldProxy(private val world: Intracomm) {
    companion object {
        const val SYNCH_DELAY = 10L
    }

    private val bcastReceivers = (0 until world.Size()).filter { it != world.Rank() }.toList()
    private val lastRequests = ConcurrentHashMap(
            bcastReceivers.map { it to CopyOnWriteArrayList<RequestView>() }.toMap()
    )

    @Volatile
    private var lastSentRequestId: String? = null

    fun rank() = world.Rank()
    fun size() = world.Size()

    @Synchronized
    fun sendRequest(request: Request) {
        log("sending request ${request.requestId}")
        lastSentRequestId = request.requestId
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

    fun receiveRequests(): Request =
            receiveMessages<Request>(Tag.REQUEST_TAG).also {
                lastRequests[it.senderId]!!.run {
                    add(RequestView(it.requestId))
                    log("Updated request from ${it.senderId}: $this")
                }
            }

    fun receiveAccepts(request: Request) {
        var left = bcastReceivers - 1
        while (left.isNotEmpty()) {
            val acceptance = receiveMessages<Acceptance>(Tag.ACCEPT_TAG)
            synchronizeWithAcceptSender(acceptance)
            left -= acceptance.senderId
        }
        log("received all accepts for request ${request.requestId}")
    }

    private fun synchronizeWithAcceptSender(acceptance: Acceptance) {
        val requests = lastRequests[acceptance.senderId]!!
        log("Check already sent requests from ${acceptance.senderId}: $requests")
        var requestWasFound = acceptance.lastSentRequestId == null
        while (!requestWasFound) {
            val lastRequest = requests.indexOfFirst { it.requestId == acceptance.lastSentRequestId }
            if (lastRequest >= 0) {
                requests[lastRequest].wasSeen = true
                if (lastRequest > 0) {
                    requests.removeAll(requests.subList(0, lastRequest - 1))
                }
                requestWasFound = true
            } else {
                log("waiting for ${acceptance.lastSentRequestId}")
                Thread.sleep(SYNCH_DELAY)
            }
        }
    }

    private inline fun <reified M : Message> receiveMessages(tag: Tag): M {
        val buffer = arrayOfNulls<M>(1)
        world.Recv(buffer, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, tag.num)
        val message = buffer[0]!!
        time = max(message.time, time) + 1
        return message
    }

    @Synchronized
    fun sendAccept(request: Request, receiver: Int) {
        val acceptance = Acceptance(Message.createId(), request.requestId, rank(), lastSentRequestId)
        log("sending accept ${acceptance.acceptId} for request ${acceptance.requestId}")
        send(acceptance, receiver, Tag.ACCEPT_TAG)
    }

    private inline fun <reified M : Message> send(message: M, receiver: Int, tag: Tag) {
        message.time = ++time
        val accPacked = arrayOf(message)
        world.Isend(accPacked, 0, 1, MPI.OBJECT, receiver, tag.num)
    }

    private enum class Tag(val num: Int, val text: String) {
        REQUEST_TAG(1, "Request/Release"),
        ACCEPT_TAG(2, "Accept")
    }

    private data class RequestView(val requestId: String, var wasSeen: Boolean = false)
}