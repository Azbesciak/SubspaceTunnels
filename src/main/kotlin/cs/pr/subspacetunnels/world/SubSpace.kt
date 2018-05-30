package cs.pr.subspacetunnels.world

import cs.pr.subspacetunnels.SubspaceSettings
import cs.pr.subspacetunnels.world.Informer.log
import cs.pr.subspacetunnels.world.Informer.wrapMessage
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.system.exitProcess

class SubSpace(subspaceSettings: SubspaceSettings,
        private val running: MutableList<Request> = CopyOnWriteArrayList(),
               private val waiting: MutableList<Request> = CopyOnWriteArrayList()) {
    val SUBSPACE_SIZE = subspaceSettings.getInt("subspace-size")

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
    fun finishTravel(request: Request) {
        free(request)
        currentRequest = null
        isWorldEnabled = false
    }

    @Synchronized
    fun free(request: Request) {
        if (!isWorldEnabled) {
            val wasRemoved = waiting.removeRequest(request)
            if (wasRemoved) return
        }

        val wasRemoved = running.removeRequest(request)
        if (!wasRemoved){
            log(wrapMessage("Request ${request.requestId} was requested to remove but not present. $this}"))
            exitProcess(-1)
        }
        emptySlots += request.passengersNumber
        onChange()
    }

    private fun MutableList<Request>.removeRequest(req: Request) = removeIf{ req.requestId == it.requestId }

    fun runRequestWhenPossible(request: Request) {
        unlockWorldWithRequest(request)
        while (currentRequest?.isRunning == false)
            Thread.sleep(100)
    }

    @Synchronized
    private fun unlockWorldWithRequest(request: Request) {
        currentRequest = request
        isWorldEnabled = true
        add(request)
    }

    private fun onChange() {
        if (emptySlots == 0 || !isWorldEnabled) return
        waiting.sortedByTime().forEach loop@{
            val canRun = it.canRun()
            if (canRun) {
                runRequest(it)
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