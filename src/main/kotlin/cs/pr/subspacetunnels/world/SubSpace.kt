package cs.pr.subspacetunnels.world

import cs.pr.subspacetunnels.SubspaceSettings
import cs.pr.subspacetunnels.world.Informer.log
import cs.pr.subspacetunnels.world.Informer.wrapMessage
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.exitProcess

class SubSpace(subspaceSettings: SubspaceSettings,
               private val releasesLimit: Int,
               private val running: MutableMap<String, Request> = ConcurrentHashMap(),
               private val waiting: MutableMap<String, Request> = ConcurrentHashMap(),
               private val releases: MutableMap<String, Message> = ConcurrentHashMap()) {
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
        waiting[request.requestId] = request
        log("$request added to waiting")
        onChange()
    }

    @Synchronized
    private fun Request.runRequest() {
        waiting -= requestId
        isRunning = true
        emptySlots -= passengersNumber
        log("Running request $requestId")
        running[requestId] = this
    }

    @Synchronized
    fun finishTravel(request: Request) {
        free(request)
        currentRequest = null
        isWorldEnabled = false
    }

    @Synchronized
    fun free(message: Message) {
        if (!isWorldEnabled) {
            val removedRequest = waiting.remove(message)
            if (removedRequest != null) return
        }
        releases[message.requestId] = message

        running.sortedByTime().forEach {
            val removedRelease = releases.remove(it.requestId)
            when {
                removedRelease != null -> {
                    running -= it.requestId
                    emptySlots += it.passengersNumber
                }
                releases.size > releasesLimit -> {
                    log(wrapMessage("Request ${message.requestId} was requested to remove but not present. $this}"))
                    exitProcess(-1)
                }
                else -> {
                    onChange()
                    return
                }
            }
        }
    }

    private fun MutableMap<String, Request>.remove(req: Message) = remove(req.requestId)

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
        waiting.sortedByTime().forEach {
            if (it.canRun()) {
                it.runRequest()
            } else {
                log("could not run $it")
                return
            }
        }
    }

    private fun Map<*, Request>.sortedByTime() =
            values.sortedWith(compareBy(
                Request::time,
                { it.passengerType.transferTime },
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
        }
    }

    private fun Map<*, Request>.containsOnlyCouriers(): Boolean =
            all { it.value.passengerType == PassengerType.COURIER }

    private fun Map<*, Request>.hasNoAlien(): Boolean =
            count { it.value.passengerType == PassengerType.ALIEN } == 0

    override fun toString(): String {
        return """SubSpace(
            |   isActive=$isWorldEnabled
            |   running=${running.sortedByTime()}
            |   waiting=${waiting.sortedByTime()}
            |   releases=${releases.values.sortedBy { it.time }}
            |   currentRequest=$currentRequest
            |   emptySlots=$emptySlots
            |)""".trimMargin()
    }
}