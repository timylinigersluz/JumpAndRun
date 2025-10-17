# 🎮 JumpAndRun Plugin für Minecraft (KSR Edition)

Ein **komplettes JumpAndRun-System** für Minecraft-Server (Paper/Spigot), entwickelt auf Basis des grossartigen Codes von [Mooselux/JumpAndRun](https://github.com/Mooselux/JumpAndRun).  
Mooselux ist ein talentierter Coder, dessen Projekt wir erweitert und für unser Server-Netzwerk angepasst haben.

Dieses Plugin erlaubt es Spielern, **eigene JumpAndRun-Welten** zu erstellen, im Draft-Modus zu testen und anschliessend für alle freizugeben.  
Es enthält ein **Checkpoint-System**, **Schilder für Teleport & Leaderboards**, ein **Inventar-Management mit Aufgeben-Item**, sowie eine **Integration mit RankPointsAPI** für ein serverweites Punktesystem.

---

## 🚀 Features

- **Eigenes JumpAndRun erstellen**
    - Spieler mit `jumpandrun.create`-Permission können neue JumpAndRuns anlegen.
    - Automatische Erstellung von Start- und Zielplattformen.
    - Draft-Modus: nur der Ersteller kann spielen, bis es getestet und veröffentlicht ist.

- **Draft → Ready → Publish Flow**
    - `/jnr ready`: Setzt den Ersteller in den Testmodus (Survival, Timer aktiv).
    - Erfolgreicher Abschluss erfordert Alias-Eingabe → danach Veröffentlichung.
    - Nach Alias-Eingabe erhält der Ersteller automatisch ein **Schild**, um JNR-Schilder setzen zu können.

- **Checkpoints**
    - Spieler können beim Erstellen zusätzliche Checkpoints platzieren.
    - Beim Spielen wird der letzte Checkpoint gespeichert.
    - Bei Tod oder Absturz → Respawn beim letzten Checkpoint (oder Startpunkt).

- **Schilder**
    - `[JNR] <alias>` → Start-Schild: Spieler klicken darauf, um ins JumpAndRun teleportiert zu werden.
    - `[JNR-LEADER] <alias>` → Leader-Schild: zeigt den aktuellen Rekordhalter und die Bestzeit.
    - Automatische Aktualisierung bei neuen Rekorden.

- **Aufgeben-Item**
    - Jeder Spieler erhält beim Betreten einer JNR-Welt ein **Barrier-Item** im letzten Hotbar-Slot.
    - Mit Rechtsklick → Welt verlassen & Inventar clear.
    - Zusätzlich Sound- & Partikeleffekte beim Aufgeben.

- **Punktesystem (RankPointsAPI, optional)**
    - Neue Weltrekorde geben Punkte (Wert aus `config.yml`).
    - Integration mit [RankPointsAPI](https://github.com/timylinigersluz/RankPointsAPI).
    - Staff-Mitglieder können optional von der Punktevergabe ausgeschlossen werden.
    - Falls `pointsdb.enabled = false` → Punktevergabe ist deaktiviert.

- **World Locking**
    - Immer nur **ein Spieler pro Welt gleichzeitig**.
    - Blockiert die Welt, bis der Spieler sie verlässt.
    - Verhindert Überschneidungen und sorgt für faire Runs.

- **Flexible Konfiguration**
    - `config.yml` enthält:
        - Debug-Modus
        - Materialien für Start-/Ziel-/Checkpoint-Platten
        - MySQL/SQLite-Umschaltung für JNR-Datenbank
        - Optional: MySQL für RankPointsAPI
        - Punkte für Weltrekorde
        - Fallback-Welt für Rückteleports

---

## 🕹️ Commands

### Hauptbefehl `/jnr`
- `/jnr create <länge>` → Erstellt ein neues JumpAndRun.
- `/jnr delete <welt>` → Löscht ein JumpAndRun.
- `/jnr teleport <welt>` → Teleportiert dich in ein JumpAndRun (Staff).
- `/jnr list` → Listet alle JumpAndRuns mit Status, Leader & Bestzeit.
- `/jnr ready` → Setzt die Welt in den Testmodus (Ersteller).
- `/jnr continue <welt>` → Draft-Welt erneut betreten.
- `/jnr abort <keepworld|deleteworld>` → Testlauf abbrechen.
- `/jnr name <alias>` → Alias nachträglich setzen.
- `/jnr unpublish <welt>` → Veröffentlichtes JumpAndRun zurück in Draft.

### Permissions
- `jumpandrun.use` → Basis-Command.
- `jumpandrun.create` → JumpAndRuns erstellen.
- `jumpandrun.delete` → JumpAndRuns löschen.
- `jumpandrun.teleport` → Welten teleportieren (Staff).
- `jumpandrun.list` → JumpAndRuns auflisten.
- `jumpandrun.ready` → Testmodus starten.
- `jumpandrun.continue` → Draft-Welten fortsetzen.
- `jumpandrun.abort` → Testläufe abbrechen.
- `jumpandrun.abort.keepworld` → Testlauf abbrechen, Welt behalten.
- `jumpandrun.abort.deleteworld` → Testlauf abbrechen, Welt löschen.
- `jumpandrun.unpublish` → Welt wieder auf Draft setzen.
- `jumpandrun.sign.create` → Schilder platzieren ([JNR], [JNR-LEADER]).
- `jumpandrun.sign.use` → Start-Schilder benutzen.
- `jumpandrun.sign.leader` → Leader-Schilder benutzen.
- `jumpandrun.name` → Alias setzen.

---

## 📚 Typische Abläufe (Usecases)

1. **JumpAndRun erstellen**
    - Mit `/jnr create <länge>` eine neue Welt erstellen.
    - Start- und Zielplattform werden automatisch generiert.

2. **Im Draft-Modus testen**
    - Mit `/jnr ready` in den Testmodus wechseln.
    - Nach Abschluss → Alias eingeben.
    - Danach Teleport zurück & Inventar clear → Spieler erhält **ein Schild**, um Start- & Leaderboardschilder zu setzen.

3. **Spiel durch andere Spieler**
    - Spieler klicken ein `[JNR] <alias>`-Schild, um ins JNR teleportiert zu werden.
    - Jeder Spieler hat das **Aufgeben-Item**, um vorzeitig abbrechen zu können.

4. **Checkpoints nutzen**
    - Checkpoints werden gespeichert.
    - Bei Tod/Fall ins Void → Spieler wird zurück zum letzten Checkpoint teleportiert.

5. **Bestzeiten & Leaderboards**
    - Zeiten werden automatisch gespeichert.
    - Neue Rekorde → Leader-Schild wird aktualisiert + Feuerwerk.
    - Punktevergabe nur, wenn Punkte-DB aktiviert ist.

---

## ⚙️ config.yml (Beispiel)

```yaml
debug: true

jumpandrun:
  enabled: true
  host: localhost
  port: 3306
  database: jnr
  user: root
  password: geheim

pointsdb:
  enabled: false
  host: localhost
  port: 3306
  database: rankpoints
  user: rankuser
  password: anderespasswort
  excludeStaff: true

plates:
  start: HEAVY_WEIGHTED_PRESSURE_PLATE
  end: LIGHT_WEIGHTED_PRESSURE_PLATE
  checkpoint: STONE_PRESSURE_PLATE

points:
  new-record: 10

fallback-world: world

players: {}
```

---

## 🧑‍💻 Installation

1. Lade das Plugin herunter (`JumpAndRun-*.jar`).
2. Lege es in den `plugins/` Ordner.
3. Passe die `config.yml` an (DB-Daten, Platten, Punkte).
4. Falls Punkte gewünscht → [RankPointsAPI](https://github.com/timylinigersluz/RankPointsAPI) installieren.
5. Server neu starten.

---

## 📜 Credits

- Ursprünglicher Code: [Mooselux/JumpAndRun](https://github.com/Mooselux/JumpAndRun) ❤️
- Erweiterungen & Anpassungen: **KSR Minecraft Tecs**
