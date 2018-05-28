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
): Serializable {
    companion object {
        fun createId() = UUID.randomUUID().toString()
    }
}

class Request(
        val passengerType: PassengerType,
        val passengersNumber: Int,
        requestId: String = "",
        senderId: Int = 0,
        var isRunning: Boolean = false
): Message(requestId, senderId) {
    companion object {
        var isVerbose = false
    }

    override fun toString(): String {
        return if (isVerbose) {
            "${if (isRunning) "Release" else "Request"}(passengerType=$passengerType," +
                    " passengersNumber=$passengersNumber," +
                    " isRunning=$isRunning," +
                    " requestId=$requestId," +
                    " senderId=$senderId," +
                    " time=$time)"
        } else {
            requestId
        }
    }
}

class Acceptance(
        val acceptId: String,
        requestId: String,
        senderId: Int
): Message(requestId, senderId)