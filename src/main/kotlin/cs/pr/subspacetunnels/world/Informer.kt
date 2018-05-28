package cs.pr.subspacetunnels.world

object Informer {
    private var id: Int? = null

    @Volatile
    var time = 0L

    fun init(id: Int) {
        this.id = id
    }
    fun log(message: String) {
        println(wrapMessage(message))
    }

    fun wrapMessage(message: String) = "[$id|$time] $message"
}