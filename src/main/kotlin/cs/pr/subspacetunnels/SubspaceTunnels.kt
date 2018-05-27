package cs.pr.subspacetunnels

import cs.pr.subspacetunnels.processes.Psycho
import cs.pr.subspacetunnels.world.WorldProxy
import kotlinx.coroutines.experimental.launch
import mpi.MPI
import java.util.*

object SubspaceTunnels {
    @JvmStatic
    fun main(args: Array<String>) {
        MPI.Init(args)
        val world = MPI.COMM_WORLD
        val rank = world.Rank()
        val size = world.Size()
        println("$size of $rank is running")
        val psycho = Psycho(WorldProxy(world))
        launch {
            psycho.run()
        }
        waitForTheEnd(rank, psycho)
        MPI.Finalize()
    }

    private fun waitForTheEnd(rank: Int, psycho: Psycho) {
        when (rank) {
            0 -> {
                val sc = Scanner(System.`in`)
                while (sc.hasNext()) {
                    if (sc.next() == "end") {
                        psycho.stopTheWorld()
                        break
                    }
                }
            }
            else -> psycho.waitForWorldEnd()
        }
        psycho.stop()
    }
}

