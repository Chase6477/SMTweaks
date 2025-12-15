# SM Tweaks
 Widgets für https://schulmanager-online.de
 
<img width="40%" height="auto" alt="image" src="https://github.com/user-attachments/assets/836da2f1-ad03-473b-b7fa-287bf82bf8c6" />


## Vorab
Bei Erstellung eines neuen Widgets müssen die Logindaten für den Schulmanager angegeben werden. Diese werden verschlüsselt auf dem Gerät gespeichert und nur zum anfordern der Daten verwendet. Momentan wird 2FA nicht unterstützt und muss bei benutzung des widgets ausgestellt sein. (Aber ist nicht schwer zu implementieren, vielleicht kommts irgendwann)

Aufgrund der Richtlinien von Android ist es leider nicht möglich dieses Anfordern im Hintergrund auszuführen (Genauere Erklärung unten), darum wird beim updaten des Widgets immer ein Wartefenster erscheinen, welches je nach Internet und Performance 2-30 Sekunden dauern kann.

Alle Widgets greifen auf die gleiche Konfiguration zu, wesswegen es reicht eines zu updaten. (Für den Fall dass ich mal mehr als eines einbaue)

Bei Problemen gerne einen Issue erstellen oder eine Email an mich schreiben justusreiterdevelopment@gmail.com (super Email, ich weiß)

Ansonsten kann es helfen den Prozess zu beenden, den Speicherplatz zu löschen oder das Widget neu zu platzieren

<img width="20%" height="auto" alt="image" src="https://github.com/user-attachments/assets/bf3515dc-b038-4783-9eb6-434e94580102" />


## Anforderungen
- Mindestens Android 8 (API 26)
- Internetverbindung
- Ein Schulmanager (Schüler-)Account

## Installieren
- neueste version [Herunterladen](https://github.com/Chase6477/SMTweaks/releases)

  *Beispiel: SMT_1-0.apk*
  
  <img width="70%" height="auto" alt="image" src="https://github.com/user-attachments/assets/a9d81ff6-6af6-4c91-8f43-b0125bebd39d" />

- Wenn die Datei heruntergeladen wurde kann sie in den eigenen datein unter APK oder Downloads gefunden und ausgeführt werden
- Es kann sein dass zuerst die Berechtigung vom Installieren von APK dateien angestellt werden muss
- Nach einem kurzen moment auf installieren / überprüfen lassen klicken
- Nach einer Erfolgreichen Installation kann die Kategorie "SM Tweaks" jetzt in dem Widgetmenü gefunden werden



## Stundenplan

<img width="20%" height="auto" alt="Widget im Darkmode" src="https://github.com/user-attachments/assets/64e20f16-ad5a-4984-bbe2-1b9cae15cc84" />
<img width="20%" height="auto" alt="Widget im Lightmode" src="https://github.com/user-attachments/assets/39e1bc2b-7efc-4b96-a98a-ae46aa7a90d0" />

#### Einstellungen
- Passwort
- Benutzername
- Speichern des Stundenplanes der letzten woche um ihn in der nächsten wieder anzuzeigen



## Warum kann das Widget das Update nicht im Hintergrund ausführen?

Auf Android sind die Berechtigungen von Widgets im vergleich zu Apps stark eingeschränkt. Das anfordern der Daten funktioniert einfach nur durch ein skript welches automatisch die Logindaten in den android built-in Browser (WebView) eingibt und dann die nötigen Daten aus dem HTML Dokument auszulesen (Ich bin mir auch ziemlich sicher dass die Schulmanager app es genauso macht). WebView kann aber nur in einem UI-Thread laufen auf den nur Apps zugriff haben. Es ist nicht möglich ihn offiziell mit einem Widget zu erreichen. Es gibt ein paar Wege diese Sperre zu umgehen, aber die meisten davon sind relativ Instabil oder nicht auf jeder android version verfügbar, jedenfalls hat keiner der Wege für mich funktioniert :(
