# Tunele podprzestrzenne
W innej galaktyce podróże międzyplanetarne odbywają się poprzez skorzystanie z usług psychokinetyków. Psychokinetyk może przenieść dowolną liczbę osób otwierając tunel w podprzestrzeni. Podprzestrzeń niestety może naraz pomieścić tylko ograniczoną liczbę P osób. Do psychokinetyka zgłaszają się wycieczki, każda o innej liczbie osób, od 0 do M. Dodatkowo, czasami zgłaszają się specjalni kurierzy, oraz obcy. Występują następujące ograniczenia:

- Jest N psychotechników, każdy z nich losowo otrzymuje zgłoszenia pojawienia się losowej liczby pasażerów (max. M), albo kuriera, albo obcego.
- Podprzestrzeń naraz może pomieścić P osób, P >N, P < M*N
- Kurier i obcy zajmują tyle miejsca, co jedna osoba
- Kurier zanim wyruszy, nie może przed nim być w podprzestrzeni ani jednego pasażera, ani obcego, chociaż mogą być przed nim inni kurierzy. - Transport kuriera trwa szybciej niż pasażera. Pasażerowie mogą się pojawić w podprzestrzeni, jeżeli już są tam kurierzy.
- W chwili wyruszenia obcego w podprzestrzeni mogą znajdować się dowolni inni pasażerowie lub kurierzy, ale gdy już w podprzestrzeni znajdzie się obcy, nie może się w podprzestrzeni pojawić żaden inny pasażer lub kurier, chociaż mogą pojawić się obcy. Transport obcego trwa wolniej niż pasażera.
- Nowe zgłoszenie pojawia się u psychotechnika dopiero po obsłużeniu poprzedniego


Należy zapewnić, by żaden rodzaj (np. kurierów albo obcych) nie zmonopolizował podprzestrzeni, a więc by każde zgłoszenie do psychotechnika zostało ostatecznie obsłużone. Kurierzy zjawiają się rzadziej niż pasażerowie, obcy zjawiają się rzadziej niż kurierzy.

### Rozpiska
 - rosnący priorytet każdego zgłoszenia w zależności od czasu oczekiwania
 - stan globalny - podprzestrzeń
 - jeżeli jest miejsce pobierz stan podprzestrzeni oraz obecne zlecenie:
    1. jeżeli kurier:
	   - w podprzestrzeni mogą być jedynie inni kurierzy
	   - obecność kuriera nie jest ograniczeniem dla innych
	   - transport szybszy niż pasażera
	 2. jeżeli obcy:
	   - brak ograniczeń na typ obecnych w podprzestrzeni podróżników
	   - gdy w podprzestrzeni obcy, za nim mogą pojawić sie jedynie inni obcy
	   - transport wolniejszy niż pasażera
	 3. jeżeli wycieczka:
	   - w podprzestrzeni nie może być obcego przed dodaniem wycieczki
	   
#### Prędkości:
   kurier > pasażer > obcy --> sekwencyjne:
   - przed kurierem jedyne inni kurierzy
   - przed pasażerami pasażerowie lub kurierzy, za pasażerowie lub obcy
   - przed obcymi wszyscy (pasażer, obcy, kurier), za jedynie obcy.
#### Ograniczenia
- Jeżeli w podprzestrzeni jest obcy, należy poczekać aż będzie ona pusta - przyjmujemy
       jedynie zlecenia od obcych z zachowaniem priorytetów kurier/pasażer.
       
- Jeżeli w podprzestrzeni jest kurier, możemy wpuścić każdego, ale jeżeli
       będzie to typ inny niż kurier, nie możemy wpuszczać kolejnych kurierów.
