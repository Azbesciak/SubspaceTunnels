### Założenia wynikające z treści zadania:
1. N psychokinetyków 
	a) każdy otwiera i kontroluje jeden tunel (o pojemności P, N < P < M*N)
2. "Losowe" zgłoszenia pojawiające się u psychokinetyków
	a) Dotyczy: wycieczki od 1 do M osób LUB kuriera LUB obcego
	b) Osoba, kurier i obcy zajmują jedno miejsce
	c) Nowe zgłoszenie POJAWIA się dopiero po obsłużeniu poprzedniego
	d) + założenia odnośnie kuriera i obcego

### Wstępny pomysł na rozproszenie problemu - force:
- Niech K = ilość_komputerów
- W ramach jednego procesu / hosta / komputera / węzła działa N wątków (psychokinetyków)
- Każdy wątek dotyczy jakiejś części tunelu
   > czyli np. tunel_0 to uporządkowany ciąg węzeł_0.wątek_0, węzeł_1.wątek_0,... węzeł_(K-1).wątek_0
- P = C * K, przy czym C jest częścią długości tunelu w ramach jednego węzła
   > czyli droga w ramach całego tunelu ma długość P
- Komunikacja odbywała by się między sąsiednimi częściami drogi
   > np. węzeł n-ty z węzłem n-1 i węzłem n+1
- Każdy wątek w pierwszym hoście (psychokinetyk) generowałby "sobie" losowo nowe zgłoszenie, gdy ogarnął już jakoś poprzednie
- Przebycie jednej jednostki drogi trwałoby jakiś ustalony z góry czas

### Do ustalenia m.in.:
- jak po wygenerowaniu zgłoszenia sprawdzać warunki wymagane dla obcych i kuriera (chyba, że robimy wersję uproszczoną)
