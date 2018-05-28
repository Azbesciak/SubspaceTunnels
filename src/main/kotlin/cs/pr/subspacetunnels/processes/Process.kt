package cs.pr.subspacetunnels.processes

import cs.pr.subspacetunnels.world.SubSpace
import cs.pr.subspacetunnels.world.WorldProxy
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

abstract class Process(protected val world: WorldProxy, protected val subSpace: SubSpace) {
    companion object {
        inline fun launch(crossinline f: () -> Unit) {
            Thread { f() }.start()
        }
    }

    protected val isRunning = AtomicBoolean(false)
    protected val id = world.rank()
    protected val total = world.size()
    protected val gen = Random(id.toLong())
    open fun stop() {
        isRunning.set(false)
    }

    abstract fun run()
}