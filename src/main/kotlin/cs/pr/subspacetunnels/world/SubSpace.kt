package cs.pr.subspacetunnels.world

import cs.pr.subspacetunnels.world.Informer.log
import cs.pr.subspacetunnels.world.Informer.wrapMessage
import java.util.concurrent.CopyOnWriteArrayList

class SubSpace(private val running: MutableList<Request> = CopyOnWriteArrayList(),
               private val waiting: MutableList<Request> = CopyOnWriteArrayList()) {
    companion object {
        const val MAX_PASSENGERS = 10
        const val SUBSPACE_SIZE = 40
    }

    @Volatile
    private var currentRequest: Request? = null
    @Volatile
    var isWorldEnabled = false
        private set(value) {
            field = value
        }
    @Volatile
    private var emptySlots = SUBSPACE_SIZE

    @Synchronized
    fun add(request: Request) {
        waiting.add(request)
        log("$request added to waiting")
        onChange()
    }

    @Synchronized
    private fun runRequest(request: Request) {
        waiting.remove(request)
        request.isRunning = true
        emptySlots -= request.passengersNumber
        log("Running request ${request.requestId}")
        running.add(request)
    }

    @Synchronized
    fun free(request: Request) {
        if (!isWorldEnabled) {
            val wasRemoved = waiting.removeRequest(request)
            if (wasRemoved) return
        }

        val wasRemoved = running.removeRequest(request)
        if (!wasRemoved)
            throw Error(wrapMessage("Request ${request.requestId} was requested to remove but not present. $this}"))
        emptySlots += request.passengersNumber
        onChange()
    }

    private fun MutableList<Request>.removeRequest(req: Request) = removeIf{ req.requestId == it.requestId }

    fun runRequestWhenPossible(request: Request) {
        currentRequest = request
        isWorldEnabled = true
        add(request)
        while (currentRequest != null)
            Thread.sleep(100)
        isWorldEnabled = false
    }

    private fun onChange() {
        if (emptySlots == 0 || !isWorldEnabled) return
        waiting.sortedByTime().forEach loop@{
            val canRun = it.canRun()
            if (canRun) {
                runRequest(it)
                if (currentRequest?.requestId == it.requestId) {
                    currentRequest = null
                }
            } else {
                log("could not run $it")
                return
            }
        }
    }

    private fun List<Request>.sortedByTime() =
            sortedWith(compareBy(
                Request::time,
                { it.passengerType.speed },
                Request::passengersNumber,
                Request::senderId
            ))

    private fun Request.canRun(): Boolean {
        if (passengersNumber > emptySlots)
            return false
        return when (passengerType) {
            PassengerType.COURIER -> running.containsOnlyCouriers()
            PassengerType.COMMON -> running.hasNoAlien()
            PassengerType.ALIEN -> true
            PassengerType.NULL -> throw Error("Null type")
        }
    }

    private fun List<Request>.containsOnlyCouriers(): Boolean =
            all { it.passengerType == PassengerType.COURIER }

    private fun List<Request>.hasNoAlien(): Boolean =
            count { it.passengerType == PassengerType.ALIEN } == 0

    override fun toString(): String {
        return """SubSpace(
            |   isActive=$isWorldEnabled
            |   running=${running.sortedByTime()}
            |   waiting=${waiting.sortedByTime()}
            |   currentRequest=$currentRequest
            |   emptySlots=$emptySlots
            |)""".trimMargin()
    }
}