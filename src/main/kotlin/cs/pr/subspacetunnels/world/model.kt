package cs.pr.subspacetunnels.world

import java.io.Serializable
import java.util.*

enum class PassengerType(val transferTime: Int) {
    COURIER(1), COMMON(2), ALIEN(3);
}

open class Message(
        val requestId: String,
        val senderId: Int,
        var time: Long = 0L
) : Serializable {
    companion object {
        fun createId() = UUID.randomUUID().toString()
    }
}

enum class LogType {
    BASIC, VERBOSE, TRAVEL
}

class Release(
        val releaseId: String,
        requestId: String,
        senderId: Int
) : Message(requestId, senderId) {
    override fun toString() =
            "Release(releaseId=$releaseId, requestId=$requestId, sender=$senderId, time=$time)"
}

class Request(
        val passengerType: PassengerType,
        val passengersNumber: Int,
        requestId: String,
        senderId: Int,
        var isRunning: Boolean = false
) : Message(requestId, senderId) {
    companion object {
        var logType = LogType.BASIC
    }

    override fun toString(): String {
        return when (logType) {
            LogType.VERBOSE -> "Request(type=$passengerType," +
                    " passengersNumber=$passengersNumber," +
                    " isRunning=$isRunning," +
                    " requestId=$requestId," +
                    " senderId=$senderId," +
                    " time=$time)"
            LogType.BASIC -> requestId
            LogType.TRAVEL -> "{$passengerType:$passengersNumber($requestId|$senderId|$time)}"
        }
    }
}

class Acceptance(
        val acceptId: String,
        requestId: String,
        senderId: Int,
        val lastSentRequestId: String?
) : Message(requestId, senderId)