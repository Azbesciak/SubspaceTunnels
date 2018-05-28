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
        val wasRemoved = running.removeIf { request.requestId == it.requestId }
        if (!wasRemoved)
            throw Error(wrapMessage("Request ${request.requestId} was requested to remove but not present. $this}"))
        emptySlots += request.passengersNumber
        onChange()
    }

    fun runRequestWhenPossible(request: Request) {
        currentRequest = request
        add(request)
        while (currentRequest != null)
            Thread.sleep(100)
    }

    private fun onChange() {
        if (emptySlots == 0) return
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
            |   running=${running.sortedByTime()},
            |   waiting=${waiting.sortedByTime()},
            |   currentRequest=$currentRequest,
            |   emptySlots=$emptySlots
            |)""".trimMargin()
    }
}