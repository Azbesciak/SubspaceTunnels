package cs.pr.subspacetunnels

import java.io.InputStreamReader
import java.io.BufferedReader
import java.io.File


object SshConnectorChecker {
    private val labs = listOf("sec")
    private val numbers = (16..31).toList()

    @JvmStatic
    fun main(args: Array<String>) {
        val machines = labs.flatMap { l -> numbers.map { n -> "lab-$l-$n" } }
                .map { it to execCmd("ssh -tt $it") }
                .filter { it.second }.joinToString("\n") { it.first }
        File("machines").printWriter().use { out ->
            out.print(machines)
        }
        execCmd("mpjboot machines")
    }

    @Throws(java.io.IOException::class)
    private fun execCmd(cmd: String): Boolean {
        val proc = ProcessBuilder(cmd.split(" "))
                .redirectErrorStream(true)
                .start()
        val inputStream = proc.inputStream
        return BufferedReader(InputStreamReader(inputStream)).use {
           val result =  it.lines()
                    .peek { println("cmd: $it") }
                    .map {
                        when {
                            it.contains("Have a lot of fun") -> true
                            it.contains("Permission denied") ||
                                    it.contains("failed") -> false
                            else -> null
                        }
                    }.filter { it != null }
                    .findFirst()
                    .orElse(false)
            println("leave process...")
            result!!
        }
    }
}

