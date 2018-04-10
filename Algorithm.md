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
    senderId: Int
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
    waitUntilAllWillAccept()
    moveRequestToExecutedQueue(req)
    invoke(req)
    removeFromExecutedQueue(req)
    sendRelease(Relase.from(req))
}
````

##### Proces nasłuchujący na nowe wiadomości
````kotlin
while (true) {
    val mes = receiveMessage()
    if (mes is Request) {
        updateTimer(mes)
        addRequestToRequestsQueue(req)
    } else if (mes is Release) {
        removeFromExecutedQueue(Request.from(mes))
    }
}
````

##### Proces logiczny rozstrzygający o wykonywanych obecnie zadaniach
````kotlin
while (true) {
    sortRequests()
    forEachRequest { req ->
        if (canInvoke(req) {
            sendAccept(req)
            moveRequestToExecutedQueue(req)
        }
    }
}

fun sortRequests() {
    requests.sortBy { req -> (time - req.time) * req.passengerType } thenBy {req -> req.requestId}
}
````
