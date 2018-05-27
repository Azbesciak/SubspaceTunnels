package cs.pr.subspacetunnels.processes

import cs.pr.subspacetunnels.world.SubSpace
import cs.pr.subspacetunnels.world.WorldProxy
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

abstract class Process(protected val world: WorldProxy, protected val subSpace: SubSpace) {
    protected val isRunning = AtomicBoolean(false)
    protected val id = world.rank()
    protected val total = world.size()
    protected val gen = Random(id.toLong())
    open fun stop() {
        isRunning.set(false)
    }
    abstract suspend fun run()
}