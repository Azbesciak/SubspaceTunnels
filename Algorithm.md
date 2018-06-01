## Założenia wynikające z treści zadania:
1. N psychokinetyków 
	- Każdy otwiera i kontroluje jeden tunel (o pojemności P, N < P < M*N).
	Możemy przyjąć, że tunele ostatecznie sprowadzają się do jednego ze względu
	na sekwencyjny charakter "klientów" (kurier najszybszy, musi  być pierwszy, obcy najwolniejszy, musi byc ostatni; stałe prędkości).
2. "Losowe" zgłoszenia pojawiające się u psychokinetyków
	- Dotyczy: wycieczki od 1 do M osób/kuriera/obcego
	- Osoba, kurier i obcy zajmują jedno miejsce
	- Nowe zgłoszenie POJAWIA się dopiero po obsłużeniu poprzedniego.
	 Implikuje to, że jednocześnie może być przesyłanych maksymalnie (n-1)<sup>2</sup> wiadomości.
	 W danej chwili czasu pomiędzy dwoma procesami mogą być jednak maksymalnie 2 wiadomości.
	- \+ założenia odnośnie kuriera i obcego

## Algorytm
### Struktury
```` kotlin
open class Message(
        val requestId: String,
        val senderId: Int,
        var time: Long = 0L // aktualizowane w trakcie działania algorytmu przez monitor
)

class Release(
        val releaseId: String,
        requestId: String,
        senderId: Int
): Message(requestId, senderId)

class Request(
        val passengerType: PassengerType,
        val passengersNumber: Int,
        requestId: String,
        senderId: Int,
        var isRunning: Boolean = false
) : Message(requestId, senderId) {
   
class Acceptance(
        val acceptId: String,
        requestId: String,
        senderId: Int,
        val lastSentRequestId: String? // null w przypadku gdy dany process nie wygenerował jeszcze żądania
) : Message(requestId, senderId)
````
### Szkic algorytmu
Każdy psychokinetyk ma swoją wizję podprzestrzeni.
Składa się ona z dwóch kolekcji - oczekujących requestów oraz obecnie wykonywanych.
Ponadto, urzymywany jest również indywidualny timer logiczny wpomagający synchronizację pomiędzy procesami.
Zegar ten aktualizowany wraz z przybyciem nowej wiadomości według wzorca
````kotlin
time = max(message.time, time) + 1
````
oraz z wysłaniem nowej poprzez inkrementacje obecnej jego wartości. Z tą też wartością nadawana jest nowa wiadomość.
Należy zaznaczyć, że obraz wykonywanych żądań jest synchronizowany dopiero w momencie, 
gdy dany process otrzyma akceptację na wykonanie od pozostałych.
Każdy psychokinetyk posiada dwa procesy:
##### Proces generujący nowe żądanie oraz kontrolujący jego wykonanie:
````kotlin
while (true) {
    generateRandomDelay()
    val request = generateRequest()
    world.run {
        sendRequest(request)
        receiveAccepts(request)
    }
    subSpace.runRequestWhenPossible(request)
    waitTillPassengersWillTransfer(request)
    onTravelFinish(request)
}

fun onTravelFinish(request: Request) {
    subSpace.finishTravel(request)
    world.sendRelease(Release(Message.createId(), request.requestId, id))
}
// Subspace
fun runRequestWhenPossible(request: Request) {
    unlockWorldWithRequest(request)
    while (!currentRequest.isRunning)
        wait()
}

fun unlockWorldWithRequest(request: Request) {
    currentRequest = request
    isWorldEnabled = true
    add(request)
}

fun add(request: Request) {
        waiting[request.requestId] = request
        onChange()
    }

fun onChange() {
    if (emptySlots == 0 || !isWorldEnabled) return
    waiting.sortedByTime().forEach {
        if (it.canRun()) {
            it.runRequest()
        } else return
    }
}

fun Request.canRun(): Boolean {
    if (passengersNumber > emptySlots)
        return false
    return when (passengerType) {
        PassengerType.COURIER -> running.containsOnlyCouriers()
        PassengerType.COMMON -> running.hasNoAlien()
        PassengerType.ALIEN -> true
    }
}
````

##### Proces nasłuchujący na nowe wiadomości
````kotlin
while (true) {
    val message = world.receiveMessages()
    when (message) {
        is Release -> {
            subSpace.free(message)
        }
        is Request -> {
            subSpace.add(message)
            world.sendAccept(message, message.senderId)
        }
    }
}
````

Jak wynika z algorytmu, złożoność komunikacyjna w przypadku rozpoczęcia żądania wynosi 2(n-1), natomiast w przypadku zakończenia - n-1.
Całkowita złożoność komunikacyjna wynosi więc 3(n-1), natomiast czasowa 3.

Ze względu na odrębne typy otrzymywanych wiadomości oraz możliwość zrównoleglenia, wiadomości typu `Request` oraz `Release`
wysyłane są z tym samym tagiem, natomiast `Acceptance` osobnym. Implikuje to konieczność manualnej synchronizacji otrzymywanych
wiadomości, ponieważ `Open MPI` gwarantuje kanał FIFO jedynie w obrębie kombinacji `communicator`/`tag`/`rank`.
Z tego też powodu konieczne jest buforowanie odpowiedzi typu `Acceptance` - możemy otrzymać taką wiadomość dla żądania
przebywającego jeszcze w kolejce `waiting` ponieważ wiadomości zwalniające dla
wykonywanych żądań nie dotarły jeszcze do zadanego wykonawcy (mimo ich wysłania).
Z tego samego też powodu każda taka wiadomość zawiera informację o ostatnio generowanym przez wysyłający ją process żądaniu (`Request`).
Wiadomość typu `Acceptance` nie powinna zostać obsłużona do czasu otrzymania danego żądania.
