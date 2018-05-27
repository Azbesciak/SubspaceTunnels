package cs.pr.subspacetunnels.world

import java.io.Serializable
import java.util.*

enum class PassengerType(val speed: Int) {
    COMMON(2), COURIER(1), ALIEN(3);
}

open class Message(
        val requestId: String,
        val senderId: Int,
        var time: Long = 0L
): Serializable {
    companion object {
        fun createId() = UUID.randomUUID().toString()
    }
}

class Request(
        val passengerType: PassengerType,
        val passengersNumber: Int,
        requestId: String,
        senderId: Int,
        var isRunning: Boolean = false
): Message(requestId, senderId)

class Acceptance(
        val acceptId: String,
        requestId: String,
        senderId: Int
): Message(requestId, senderId)

class Release(
        val messageId: String,
        requestId: String,
        senderId: Int
): Message(requestId, senderId)

typealias SubspaceObserver = (running: List<Request>, waiting: List<Request>) ->Unit