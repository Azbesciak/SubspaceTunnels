## Założenia wynikające z treści zadania:
1. N psychokinetyków 
	a) każdy otwiera i kontroluje jeden tunel (o pojemności P, N < P < M*N)
2. "Losowe" zgłoszenia pojawiające się u psychokinetyków
	a) Dotyczy: wycieczki od 1 do M osób LUB kuriera LUB obcego
	b) Osoba, kurier i obcy zajmują jedno miejsce
	c) Nowe zgłoszenie POJAWIA się dopiero po obsłużeniu poprzedniego
	d) + założenia odnośnie kuriera i obcego

## Algorytm
### Struktury
```` kotlin
enum class PasengerType {
    COMMON, COURIER, ALIEN
}

open class Message(
    val requestId: String,
    val senderId: Int
);

data class Request(
    val passengerType: PassengerType,
    val passengersNumber: Int,
    val time: Long,
    requestId: String,
    senderId: Int,
    isRunning: Boolean = false
): Message(requestId, senderId);

data class Acceptance(
    acceptId: String
    requestId: String,
    senderId: Int
): Message(requestId, senderId);

data class Release(
    val messageId: String
    requestId: String,
    senderId: Int
): Message(requestId, senderId);
````
### Szkic algorytmu
Każdy psychokinetyk utrzymuje dwie kolekcje - oczekujących requestów oraz obecnie wykonywanych.
Urzymywany jest również indywidualny timer logiczny.

Każdy psychokinetyk posiada trzy procesy:
##### Proces generujący żądania:
````kotlin
while (true) {
    delay(random)
    val req = generateRequest()
    addRequestToRequestsQueue(req)
    incrementTimerAndSendRequest(req)
    waitUntilAllWillAccept()
    waitUntilRequestCanBeInvoked(req)
    invoke(req)
    removeFromRequestsQueue(req)
    sendRelease(Release.from(req))
}

fun sortRequests() {
    requests.sortBy { req -> req.time } thenBy {req -> req.requestId}
    requests.notifyListener()
}

fun addRequestToRequestsQueue(req) {
    requests.add(req) // updates empty places also.
    sortRequests()
}

fun waitUntilRequestCanBeInvoked(req) {
    requests.addListener { currentRequests ->
        val (notRunningRequests: List<Requests>, runningRequests: List<Request>) = currentRequests.groupBy {it.isRunning}
        if (currentRequests.emptySlots == 0) // if lower throw.
		return
	for (r in notRunningRequests) { //we assume it is sorted by timers and ids
	    if (r.passengersNumber <= currentRequests.emptySlots) {
	    	when (r.passengerType) {
		    COURIER -> 
		    	if (runningRequests.containsOnlyCouriers()) {
			    val wasMyRequest = runRequest(runningRequests, r, req)
			    if (wasMyRequest) return else continue
			}
			return
		    PASSENGER ->
		        if (runningRequests.alienCount() == 0) {
			   val wasMyRequest = runRequest(runningRequests, r, req)
			   if (wasMyRequest) return else continue
			}
			return
		    ALIEN -> 
		        val wasMyRequest = runRequest(runningRequests, r, req)
			if (wasMyRequest) return else continue
                }
	    }
	}
    }
}

fun runRequest(runningRequests:List<Request>, toRun: Request, ownRequest: Request): Boolean {
    r.isRunning = true
    runningRequests.add(r)
    if (r == req) detachListener();return true
    else return false
}
````

##### Proces nasłuchujący na nowe wiadomości
````kotlin
while (true) {
    val mes = receiveMessage()
    updateTimer(mes)
    if (mes is Request) {
        updateTimer(mes)
        addRequestToRequestsQueue(req)
    } else if (mes is Release) {
        removeFromRequestsQueue(Request.from(mes))
    }
}

fun updateTimer(mes) {
	time = max(mes.time, this.time) + 1
}
````

##### Proces logiczny rozstrzygający o wykonywanych obecnie zadaniach
````kotlin
while (true) {
    List<Request> reqs = receiveRequests()
    reqs.forEach { req ->
    	updateTimer(req)
	addRequestToRequestsQueue(req)
    	sendAccept(req)
    }
}
````
