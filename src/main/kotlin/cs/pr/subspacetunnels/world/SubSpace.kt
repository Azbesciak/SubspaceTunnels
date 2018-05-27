package cs.pr.subspacetunnels.world

import kotlinx.coroutines.experimental.delay
import java.util.concurrent.CopyOnWriteArrayList

class SubSpace(private val running: MutableList<Request> = CopyOnWriteArrayList<Request>(),
               private val waiting: MutableList<Request> = CopyOnWriteArrayList<Request>()) {
    companion object {
        const val MAX_PASSENGERS = 10
        const val SUBSPACE_SIZE = 40
    }

    @Volatile
    private var currentRequest: Request? = null

    private var emptySlots = SUBSPACE_SIZE

    @Synchronized
    fun add(request: Request) {
        waiting.add(request)
        onChange()
    }

    @Synchronized
    private fun runRequest(request: Request) {
        waiting.remove(request)
        request.isRunning = true
        running.add(request)
    }

    @Synchronized
    fun free(release: Release) {
        val request = running.first { it.requestId == release.requestId }
        free(request)
    }

    @Synchronized
    fun free(request: Request) {
        running.remove(request)
        emptySlots += request.passengersNumber
        onChange()
    }

    fun runRequestWhenPossible(request: Request) {
        currentRequest = request
        add(request)
        suspend {
            while (currentRequest != null)
                delay(100)
        }
    }

    private fun onChange() {
        if (emptySlots == 0) return
        waiting.forEach loop@{
            if (it.passengersNumber > emptySlots)
                return@loop
            val canRun = canRun(it)
            if (canRun) {
                runRequest(it)
                if (currentRequest?.requestId == it.requestId) {
                    currentRequest = null
                }
            } else {
                return
            }
        }
    }

    private fun canRun(request: Request): Boolean {
        if (request.passengersNumber > emptySlots)
            return false
        return when(request.passengerType) {
            PassengerType.COURIER -> running.containsOnlyCouriers()
            PassengerType.COMMON -> running.hasNoAlien()
            PassengerType.ALIEN -> true
        }
    }

    private fun List<Request>.containsOnlyCouriers(): Boolean =
            all { it.passengerType == PassengerType.COURIER }

    private fun List<Request>.hasNoAlien(): Boolean =
            count { it.passengerType == PassengerType.ALIEN } == 0

}