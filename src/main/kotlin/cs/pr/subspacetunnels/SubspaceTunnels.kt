package cs.pr.subspacetunnels

import cs.pr.subspacetunnels.processes.Process.Companion.launch
import cs.pr.subspacetunnels.processes.Psycho
import cs.pr.subspacetunnels.world.Informer
import cs.pr.subspacetunnels.world.Informer.log
import cs.pr.subspacetunnels.world.WorldProxy
import mpi.MPI
import java.io.File
import kotlin.system.exitProcess

object SubspaceTunnels {
    @JvmStatic
    fun main(args: Array<String>) {
        MPI.Init(args)
        val world = MPI.COMM_WORLD
        val rank = world.Rank()
        val size = world.Size()
        println("$size of $rank is running")
        Informer.init(rank)
        val psycho = Psycho(WorldProxy(world))
        launch {
            psycho.run()
        }
        waitForTheEnd(psycho)
        MPI.Finalize()
        exitProcess(0)
    }

    private fun waitForTheEnd(psycho: Psycho) {
        while(true) {
            val file = File("status")
            if (file.exists() && file.readLines().contains("end")) {
                psycho.stop()
                log("End requested")
                return
            } else {
                Thread.sleep(1000)
            }
        }
    }
}

