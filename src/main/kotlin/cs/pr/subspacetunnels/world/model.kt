package cs.pr.subspacetunnels.world

import java.io.Serializable
import java.util.*

enum class PassengerType(val speed: Int) {
    COMMON(2), COURIER(1), ALIEN(3), NULL(999);
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

class Request(
        val passengerType: PassengerType,
        val passengersNumber: Int,
        requestId: String = "",
        senderId: Int = 0,
        var isRunning: Boolean = false
) : Message(requestId, senderId) {

    companion object {

        var logType = LogType.BASIC
    }

    override fun toString(): String {
        return when (logType) {
            LogType.VERBOSE -> "${if (isRunning) "Release" else "Request"}(passengerType=$passengerType," +
                    " passengersNumber=$passengersNumber," +
                    " isRunning=$isRunning," +
                    " requestId=$requestId," +
                    " senderId=$senderId," +
                    " time=$time)"
            LogType.BASIC -> requestId
            LogType.TRAVEL -> "{$passengerType:$passengersNumber($requestId)}"
        }
    }
}


class Acceptance(
        val acceptId: String,
        requestId: String,
        senderId: Int,
        val lastSentRequestId: String?
) : Message(requestId, senderId)