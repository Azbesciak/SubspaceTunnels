package cs.pr.subspacetunnels.processes.requester

import cs.pr.subspacetunnels.processes.Process
import cs.pr.subspacetunnels.world.*
import kotlinx.coroutines.experimental.delay

class Requester(world: WorldProxy, subSpace: SubSpace) : Process(world, subSpace) {
    companion object {
        private const val MIN_DELAY = 500
        private const val MAX_DELAY = 3000
        private const val SPEED_UNIT_VALUE = 500
    }
    private val requestGenerator = RequestGenerator(id, gen)

    override suspend fun run() {
        if (isRunning.getAndSet(true)) return
        while (isRunning.get()) {
            generateRandomDelay()
            val request = requestGenerator.generateRequest()
            world.run {
                sendRequest(request)
                receiveAccepts(request)
            }
            subSpace.runRequestWhenPossible(request)
            waitTillPassengersWillTransfer(request)
            onTravelFinish(request)
        }
    }

    private suspend fun generateRandomDelay() {
        val wait = gen.nextInt(MAX_DELAY - MIN_DELAY) + MIN_DELAY
        delay(wait)
    }

    private suspend fun waitTillPassengersWillTransfer(request: Request) {
        val timeRequired = request.passengerType.speed * SPEED_UNIT_VALUE
        delay(timeRequired)
    }

    private fun onTravelFinish(request: Request) {
        subSpace.free(request)
        world.sendRelease(Release(Message.createId(), request.requestId, id))
    }
}