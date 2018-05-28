package cs.pr.subspacetunnels.world

object Informer {
    private var id: Int? = null
    @Volatile
    var loggingLevel = 0
    @Volatile
    var time = 0L

    fun init(id: Int) {
        this.id = id
    }
    fun log(message: String, level: Int = 0) {
        if (level >= loggingLevel)
            println(wrapMessage(message))
    }

    fun wrapMessage(message: String) = "[$id|$time] $message"
}