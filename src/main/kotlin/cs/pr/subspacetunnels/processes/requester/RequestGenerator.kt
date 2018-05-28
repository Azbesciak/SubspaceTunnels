package cs.pr.subspacetunnels.processes.requester

import cs.pr.subspacetunnels.world.Message
import cs.pr.subspacetunnels.world.PassengerType
import cs.pr.subspacetunnels.world.Request
import cs.pr.subspacetunnels.world.SubSpace
import java.util.*

class RequestGenerator(private val id: Int, private val gen: Random) {

    companion object {
        private const val ALIEN_THRESHOLD = 0.1
        private const val COURIER_THRESHOLD = 0.4
    }

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
                PassengerType.COMMON -> gen.nextInt(SubSpace.MAX_PASSENGERS)
                PassengerType.NULL -> throw Error("null type")
            }
}