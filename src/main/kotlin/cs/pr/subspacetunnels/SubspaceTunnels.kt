package cs.pr.subspacetunnels

import cs.pr.subspacetunnels.processes.Process.Companion.launch
import cs.pr.subspacetunnels.processes.Psycho
import cs.pr.subspacetunnels.world.Informer
import cs.pr.subspacetunnels.world.Informer.log
import cs.pr.subspacetunnels.world.LogType
import cs.pr.subspacetunnels.world.Request
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
        Request.logType = LogType.TRAVEL
        Informer.init(rank)
        val psycho = Psycho(WorldProxy(world), SubspaceSettings("config.properties"))
        launch {
            psycho.run()
        }
        waitForTheEnd(rank, psycho)
        MPI.Finalize()
        exitProcess(0)
    }

    private fun waitForTheEnd(rank: Int, psycho: Psycho) {
        while (true) {
            val file = File("status")
            if (file.exists())
                try {
                    file.readLines().run {
                        when {
                            contains("end") -> {
                                psycho.stop()
                                log("End requested")
                                return
                            }
                            any {it.showSubspace()} -> {
                                val activeOnly = find {it.showSubspace()}!!.endsWith("!")
                                if (rank == 0) {
                                    println("--------------------------------------------")
                                }
                                psycho.showSubspace(activeOnly)
                                Thread.sleep(2000)
                            }
                            any {it.findLevel() } -> {
                                val level = find { it.findLevel() }!!.takeLastWhile { it != '=' }
                                println("setting level to |$level|")
                                Informer.loggingLevel = level.trim().toInt()
                            }
                            else -> Thread.sleep(1000)
                        }
                    }
                } catch (t: Throwable) {}
        }
    }

    private fun String.showSubspace() = matches("subspace(!)?".toRegex())
    private fun String.findLevel() = matches("level=\\d+".toRegex())
}

