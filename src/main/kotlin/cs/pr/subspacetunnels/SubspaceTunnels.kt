package cs.pr.subspacetunnels

import mpi.MPI

object SubspaceTunnels {
    @JvmStatic
    fun main(args: Array<String>) {
        MPI.Init(args)
        val world = MPI.COMM_WORLD
        val rank = world.Rank()
        val size = world.Size()
        println("$size / $rank")
        MPI.Finalize()
    }
}

