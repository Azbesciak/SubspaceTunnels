package cs.pr.subspacetunnels.processes.requester

import cs.pr.subspacetunnels.SubspaceSettings
import cs.pr.subspacetunnels.processes.Process
import cs.pr.subspacetunnels.world.*
import cs.pr.subspacetunnels.world.Informer.log

class Requester(world: WorldProxy, subSpace: SubSpace, settings: SubspaceSettings) : Process(world, subSpace) {
    private val MIN_DELAY = settings.getInt("min-request-delay")
    private val MAX_DELAY = settings.getInt("max-request-delay")
    private val BASE_SPEED = settings.getInt("base-speed")
    private val requestGenerator = RequestGenerator(id, gen, settings)

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
        val timeRequired = request.passengerType.speed * BASE_SPEED
        Thread.sleep(timeRequired.toLong())
    }

    private fun onTravelFinish(request: Request) {
        log("travel of request ${request.requestId} is finished")
        subSpace.finishTravel(request)
        world.sendRelease(request)
    }
}