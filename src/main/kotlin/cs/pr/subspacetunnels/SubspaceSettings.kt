package cs.pr.subspacetunnels

import java.io.File

class SubspaceSettings(propertyFilePath: String) {
    private val settings = File(propertyFilePath).useLines {
        it.toList()
                .map { it.split("=").let { it.first() to it.last() } }
                .toMap()
    }

    fun getInt(field: String) = settings[field]!!.toInt()
    fun getDouble(field: String) = settings[field]!!.toDouble()
}