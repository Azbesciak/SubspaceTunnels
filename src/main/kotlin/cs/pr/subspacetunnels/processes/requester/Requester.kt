package cs.pr.subspacetunnels.processes.requester

import cs.pr.subspacetunnels.processes.Process
import cs.pr.subspacetunnels.world.*
import cs.pr.subspacetunnels.world.Informer.log

class Requester(world: WorldProxy, subSpace: SubSpace) : Process(world, subSpace) {
    companion object {
        private const val MIN_DELAY = 500
        private const val MAX_DELAY = 3000
        private const val SPEED_UNIT_VALUE = 500
    }
    private val requestGenerator = RequestGenerator(id, gen)

    override fun run() {
        if (isRunning.getAndSet(true)) return
        log("Started requester...")
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

    private fun generateRandomDelay() {
        val wait = gen.nextInt(MAX_DELAY - MIN_DELAY) + MIN_DELAY
        Thread.sleep(wait.toLong())
    }

    private fun waitTillPassengersWillTransfer(request: Request) {
        val timeRequired = request.passengerType.speed * SPEED_UNIT_VALUE
        Thread.sleep(timeRequired.toLong())
    }

    private fun onTravelFinish(request: Request) {
        log("travel of request ${request.requestId} is finished")
        subSpace.free(request)
        world.sendRelease(request)
    }
}