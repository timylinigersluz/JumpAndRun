# 🎮 JumpAndRun Plugin für Minecraft (KSR Edition)

Ein **komplettes JumpAndRun-System** für Minecraft-Server (Paper/Spigot), entwickelt auf Basis des grossartigen Codes von [Mooselux/JumpAndRun](https://github.com/Mooselux/JumpAndRun).  
Mooselux ist ein talentierter Coder, dessen Projekt wir erweitert und für unser Server-Netzwerk angepasst haben.

Dieses Plugin erlaubt es Spielern, **eigene JumpAndRun-Welten** zu erstellen, im Draft-Modus zu testen und anschliessend für alle freizugeben.  
Es enthält ein **Checkpoint-System**, **Schilder für Teleport & Leaderboards**, sowie eine **Integration mit RankPointsAPI** für ein serverweites Punktesystem.

---

## 🚀 Features

- **Eigenes JumpAndRun erstellen**
    - Spieler mit `jumpandrun.create`-Permission können neue JumpAndRuns anlegen.
    - Automatische Erstellung von Start- und Zielplattformen.
    - Draft-Modus: nur der Ersteller kann spielen, bis es getestet und veröffentlicht ist.

- **Draft → Ready → Publish Flow**
    - `/jnr ready`: Setzt den Ersteller in den Testmodus (Survival, Timer aktiv).
    - Erfolgreicher Abschluss = automatische Veröffentlichung (Published = true).
    - Ab dann können alle Spieler die Welt spielen.

- **Checkpoints**
    - Spieler können beim Erstellen zusätzliche Checkpoints platzieren.
    - Beim Spielen wird der letzte Checkpoint gespeichert.
    - Bei Tod oder Absturz → Respawn beim letzten Checkpoint (oder Startpunkt).

- **Schilder**
    - `[JNR] <welt>` → Start-Schild: Spieler klicken darauf, um ins JumpAndRun teleportiert zu werden.
    - `[JNR-LEADER] <welt>` → Leader-Schild: zeigt den aktuellen Rekordhalter und die Bestzeit.
    - Automatische Aktualisierung bei neuen Rekorden.

- **Punktesystem (RankPointsAPI)**
    - Neue Weltrekorde geben Punkte (Wert aus `config.yml`).
    - Integration mit [RankPointsAPI](https://github.com/timylinigersluz/RankPointsAPI).
    - Staff-Mitglieder können optional von der Punktevergabe ausgeschlossen werden.

- **World Locking**
    - Immer nur **ein Spieler pro Welt gleichzeitig**.
    - Blockiert die Welt, bis der Spieler sie verlässt.
    - Verhindert Überschneidungen und sorgt für faire Runs.

- **Flexible Konfiguration**
    - `config.yml` enthält:
        - Debug-Modus
        - Materialien für Start-/Ziel-/Checkpoint-Platten
        - Punkte für Weltrekorde
        - MySQL-Zugangsdaten für JnR-DB und RankPointsAPI

---

## 🕹️ Commands

### Hauptbefehl `/jnr`
- `/jnr create <länge>` → Erstellt ein neues JumpAndRun.
- `/jnr delete <welt>` → Löscht ein JumpAndRun.
- `/jnr teleport <welt>` → Teleportiert dich in ein JumpAndRun (falls frei).
- `/jnr list` → Listet alle JumpAndRuns mit Status, Leader & Bestzeit.
- `/jnr ready` → Setzt die Welt in den Testmodus (Ersteller).
- `/jnr publish` → (Optional) Veröffentlicht ein JumpAndRun manuell.

### Permissions
- `jumpandrun.use` → erlaubt die Nutzung der Basis-Commands.
- `jumpandrun.create` → JumpAndRuns erstellen.
- `jumpandrun.delete` → JumpAndRuns löschen.
- `jumpandrun.teleport` → JumpAndRuns betreten.
- `jumpandrun.list` → Liste der JumpAndRuns sehen.
- `jumpandrun.ready` → Testmodus starten.
- `jumpandrun.publish` → JumpAndRuns veröffentlichen.
- `jumpandrun.sign.create` → JnR-Schilder erstellen.
- `jumpandrun.sign.use` → Start-Schilder benutzen.
- `jumpandrun.sign.leader` → Leader-Schilder erstellen/anzeigen.

---

## 📚 Typische Abläufe (Usecases)

1. **JumpAndRun erstellen**
    - Ein Spieler mit der Permission `jumpandrun.create` erstellt eine neue Welt mit `/jnr create`.
    - Start- und Zielplattform werden automatisch generiert.

2. **Im Draft-Modus testen**
    - Der Ersteller gibt `/jnr ready` ein und wird in den Testmodus gesetzt.
    - Er spielt sein eigenes JumpAndRun durch.
    - Nach erfolgreichem Abschluss wird die Welt automatisch veröffentlicht.

3. **Spiel durch andere Spieler**
    - Andere Spieler können das JumpAndRun erst betreten, wenn es veröffentlicht wurde.
    - Über `/jnr teleport <welt>` oder durch ein `[JNR]`-Schild gelangen sie in die Welt.

4. **Checkpoints nutzen**
    - Während des Spiels erreicht der Spieler Checkpoints, die seinen Fortschritt speichern.
    - Bei einem Tod oder Fall ins Void wird er zum letzten Checkpoint zurückgesetzt.

5. **Bestzeiten & Leaderboards**
    - Beim Betreten des Ziels wird die Zeit gespeichert.
    - Erreicht ein Spieler einen neuen Rekord, wird das Leader-Schild automatisch aktualisiert.

6. **Punkte für Rekorde**
    - Ein neuer Weltrekord bringt dem Spieler Punkte (konfigurierbar in `config.yml`).
    - Die Punkte werden über die RankPointsAPI global gespeichert.

7. **World Locking**
    - Nur ein Spieler kann eine JumpAndRun-Welt gleichzeitig betreten.
    - Andere müssen warten, bis die Welt wieder frei ist.

---

## ⚙️ config.yml (Beispiel)

```yaml
schematicPath: "JumpAndRunIsland.schem"

debug: true

# JumpAndRun-DB
mysql:
  host: localhost
  port: 3306
  database: jnr
  user: root
  password: geheim

# Punkte-DB für RankPointsAPI
pointsdb:
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
```

---

## 🧑‍💻 Installation

1. Lade das Plugin herunter (`JumpAndRun-*.jar`).
2. Kopiere es in den `plugins/` Ordner deines Servers.
3. Starte den Server neu.
4. Passe die `config.yml` an (DB-Daten, Platten, Punkte).
5. Stelle sicher, dass die `RankPointsAPI`-JAR im `plugins/` Ordner liegt.

---

## 📜 Credits

- Ursprünglicher Code: [Mooselux/JumpAndRun](https://github.com/Mooselux/JumpAndRun) ❤️
- Erweiterungen, Refactoring & Features: **KSR Minecraft Tecs**
