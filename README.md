# SM Tweaks
Widgets für https://schulmanager-online.de

<img width="40%" height="auto" alt="Calendar Widget" src="https://github.com/user-attachments/assets/ca90bd6d-e0f2-4d51-a654-eede4870b1a4" />

## Vorab
Die Angabe von einem korrekten Schulmanager Passwort und Nutzernamen ist für die App erforderlich!

Momentan gibt es nur ein Widget für den Stundenplan und eine (fast leere) Applikation, die für Einstellungen genutzt wird

Bei Problemen gerne einen Issue erstellen oder eine E-mail an mich schreiben justusreiterdevelopment@gmail.com (super E-mail, ich weiß)

## Anforderungen
- Mindestens Android 8 (API 26)
- Internetverbindung
- Ein Schulmanager (Schüler-)Account
- Deaktivierte 2-Faktor Authentifizierung

## Installieren
- neueste Version [Herunterladen](https://github.com/Chase6477/SMTweaks/releases/latest)

  *Beispiel: SMT_1-0.apk*
  
  <img width="70%" height="auto" alt="image" src="https://github.com/user-attachments/assets/a9d81ff6-6af6-4c91-8f43-b0125bebd39d" />

- Wenn die Datei heruntergeladen wurde kann sie in den Eigenen Dateien unter APK oder Downloads gefunden und ausgeführt werden
- Es kann sein, dass zuerst die Berechtigung vom Installieren von APK-Dateien erteilt werden muss
- Nach einem kurzen Moment auf "installieren" bzw. "überprüfen" klicken
- Nach einer Erfolgreichen Installation kann die Kategorie "SM Tweaks" jetzt im Widgetmenü gefunden werden

## Updaten
- Ab Version 1.1 wird bei einem neuen verfügbaren Update eine Popup-Nachricht angezeigt
   - Später -- später
   - Nicht mehr anzeigen -- Diese Meldung nicht mehr anzeigen, kann in der SM Tweaks app wieder angeschaltet werden
   - Im Browser anzeigen -- Die Neueste Version auf GitHub öffnen
 - Wie bei der Erstinstallation die neueste APK-Datei herunterladen
 - In den eigenen Dateien anklicken / installieren
 - Das Update sollte aumtomatisch installiert werden

#### Probleme
Sollte es bei dem Update-Vorgang ein Problem geben:
- Überprüfen, ob die neueste Version bereits installiert ist (App Einstellungen, oben "Version: 1.x")
- Ansonsten muss die App deinstalliert und neu installiert werden. Dies wird allerdings alle bereits vorhaneden (Wochen-)Daten Löschen

## Stundenplan

<img width="40%" height="auto" alt="Calendar Widget" src="https://github.com/user-attachments/assets/ca90bd6d-e0f2-4d51-a654-eede4870b1a4" />

#### Einstellungen
- Passwort
- Benutzername
- Speichern des Stundenplanes der letzten Woche, um ihn in der Nächsten wieder anzuzeigen
- Ferien und Feiertage als rotes Overlay anzeigen
- Bundesland angeben für Richtige Ferien und Feiertags Angaben (Daten aus der [mehr-schulferien.de](https://mehr-schulferien.de) Api)


## Legende
|Format|Bedeutung|Referenz|
|-|-|-|
|Grüne Schriftfarbe|Vertretungs- Lehrer/Raum|Siehe Montag, Stunde 2|
|Rot Durchgestrichen|Stunde fällt komplett aus|Siehe Freitag, Stunde 3 & 4|
|Rot Hinterlegt|Ferien/Feiertag|Siehe Dienstag, ganzer Tag|


## Sicherheit
Für die Sicherheit und den Datenschutz der App wird gesorgt

1. Komplette Verschlüsselung der Benutzerdaten
    - Kann nur von der App entschlüsselt werden
    - Wird nur privat zum Anmelden am Schulmanager verwendet
2. Es werden keine Daten gesammelt
3. Der einzige Austausch der über das Internet erfolgt, neben der Datenanfrage an Schulmanager, ist eine deaktivierbare App-Update Suche

## QnA
### Android Version
- Nativ getestet auf
  - 8.1
  - 11
  - 16
### Launcher
- Nativ getestet
  - Samsung OneUI
  - Moto Launcher
- Auf Emulator getestet
  - Pixel Launcher
### Aktualisierungsdauer
Bei gutem/normalem Internet ca. 1 Sekunde

Bei schlechtem Internet ca. 10-20 Sekunden

## Zukunftspläne
- 2FA Unterstützung
  - Der Code ist da, aber hatte noch keine Lust ihn zu implementieren
- Nachträgliche veränderung der Einstellungen
  - Ist nicht wirklich kompliziert, aber ich weiß nicht was eine elegante Lösung dafür wäre so dass es nicht ausversehen aktiviert wird
- Automatisches fetching
  - Da momentan keine Einstellungen nachträglich verändert werden können bin ich mir unsicher wo es hin sollte. Außerdem muss ich auch auf den Akkuverbrauch davon achten...
- Weitere Widgets
  - An sich kein Problem, ich habe aber persöhnlich grade keinen Grund oder Lust welche zu erstellen
- Bug fixxen der bei jeder Aktualisierung das Widget 4 mal blinken lässt (nur auf manchen Launchern, fsr auf dem neuesten von Samsung, aber nicht auf älteren Versionen)
  - weil für die 2 Button states, ein Failsave und einfach noch so ein Update Event ausgeführt wird, die ich aber alle nicht problemlos rausnehmen kann
