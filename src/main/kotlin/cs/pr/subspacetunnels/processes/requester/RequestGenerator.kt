package cs.pr.subspacetunnels.processes.requester

import cs.pr.subspacetunnels.SubspaceSettings
import cs.pr.subspacetunnels.world.Message
import cs.pr.subspacetunnels.world.PassengerType
import cs.pr.subspacetunnels.world.Request
import java.util.*

class RequestGenerator(private val id: Int, private val gen: Random, settings: SubspaceSettings) {
    private val ALIEN_THRESHOLD = settings.getDouble("alien-probability")
    private val COURIER_THRESHOLD = ALIEN_THRESHOLD + settings.getDouble("courier-probability")
    val MAX_PASSENGERS = settings.getInt("max-passengers")

    fun generateRequest(): Request {
        val passengerType = getPassengerType()
        val passengerCount = getPassengersCount(passengerType)
        return Request(passengerType, passengerCount, Message.createId(), id)
    }

    private fun getPassengerType(): PassengerType {
        val threshold = gen.nextDouble()
        return when {
            threshold <= ALIEN_THRESHOLD -> PassengerType.ALIEN
            threshold <= COURIER_THRESHOLD -> PassengerType.COURIER
            else -> PassengerType.COMMON
        }
    }

    private fun getPassengersCount(passengerType: PassengerType) =
            when (passengerType) {
                PassengerType.COURIER, PassengerType.ALIEN -> 1
                PassengerType.COMMON -> gen.nextInt(MAX_PASSENGERS)
                PassengerType.NULL -> throw Error("null type")
            }
}