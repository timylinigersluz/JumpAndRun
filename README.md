# 🎮 JumpAndRun Plugin für Minecraft (KSR Edition)

Ein **komplettes JumpAndRun-System** für Minecraft-Server (Paper/Spigot), entwickelt auf Basis des grossartigen Codes von [Mooselux/JumpAndRun](https://github.com/Mooselux/JumpAndRun).  
Mooselux ist ein talentierter Coder, dessen Projekt wir erweitert und für unser Server-Netzwerk angepasst haben.

Dieses Plugin erlaubt es Spielern, **eigene JumpAndRun-Welten** zu erstellen, im Draft-Modus zu testen und anschliessend für alle freizugeben.  
Es enthält ein **Checkpoint-System**, **Schilder für Teleport & Leaderboards**, ein **Inventar-Management mit Aufgeben-Item**, sowie eine **Integration mit RankPointsAPI** für ein serverweites Punktesystem.

---

## 🚀 Features

- **Eigenes JumpAndRun erstellen**
    - Spieler mit `jumpandrun.create` können neue JumpAndRuns anlegen.
    - Automatische Erstellung von Start- und Zielplattformen.
    - Draft-Modus: nur der Ersteller kann spielen, bis es getestet und veröffentlicht ist.

- **Draft → Ready → Publish Flow**
    - `/jnr ready`: Setzt den Ersteller in den Testmodus (Survival, Timer aktiv).
    - Erfolgreicher Abschluss erfordert Alias-Eingabe → danach Veröffentlichung.
    - Nach Veröffentlichung kann der Ersteller Start- und Leaderboardschilder setzen.

- **Checkpoints**
    - Zusätzliche Checkpoints möglich.
    - Respawn beim letzten Checkpoint oder Startpunkt.

- **Schilder**
    - `[JNR] <alias>` → Teleport-Schild: Spieler klicken zum Start.
    - `[JNR-LEADER] <alias>` → Leader-Schild: zeigt aktuellen Rekordhalter & Bestzeit.
    - Automatische Aktualisierung bei neuen Rekorden.
    - **Nur Spieler mit `jumpandrun.sign.interact`** dürfen Schilder erstellen, bearbeiten oder löschen.  
      • Bearbeiten / Löschen → nur im Creative-Modus  
      • Erstellen → auch im Survival erlaubt  
      • Klicken auf [JNR] → für alle Spieler erlaubt

- **Aufgeben-Item**
    - Beim Betreten einer JNR-Welt: Barrier-Item im letzten Hotbar-Slot.
    - Rechtsklick → Abbruch + Teleport zurück + Effekte.

- **Punktesystem (RankPointsAPI, optional)**
    - Neue Weltrekorde geben Punkte (Wert aus `config.yml`).
    - Integration mit [RankPointsAPI](https://github.com/timylinigersluz/RankPointsAPI).
    - Staff-Mitglieder können von der Punktevergabe ausgeschlossen werden.

- **World Locking**
    - Nur ein Spieler pro Welt gleichzeitig.
    - Sperrt Welt bis zum Verlassen.

- **Advancement-Blocker**
    - In allen JumpAndRun-Welten (Draft & Published) sind **Advancements deaktiviert**.
    - Blockiert still – keine Chatnachricht, aber Debug-Log bei aktiviertem Debug-Modus.

- **Flexible Konfiguration**
    - `config.yml` enthält:
        - Debug-Modus
        - Materialien für Platten
        - MySQL/SQLite-Einstellungen
        - RankPointsAPI-Optionen
        - Punktewerte
        - Fallback-Welt

---

## 🕹️ Commands

### Hauptbefehl `/jnr`

| Befehl | Beschreibung |
|:--|:--|
| `/jnr create <länge>` | Erstellt ein neues JumpAndRun. |
| `/jnr delete <welt>` | Löscht eine JumpAndRun-Welt. |
| `/jnr teleport <welt>` | Teleportiert in eine JNR-Welt (Staff). |
| `/jnr list` | Listet alle JumpAndRuns. |
| `/jnr ready` | Startet den Testmodus (Survival). |
| `/jnr continue <welt>` | Draft-Welt erneut betreten. |
| `/jnr abort <keepworld | deleteworld>` | Bricht einen Testlauf ab – **nur in Draft-Welten ausführbar!** |
| `/jnr name <alias>` | Alias nachträglich setzen. |
| `/jnr unpublish <welt>` | Welt wieder auf Draft setzen. |

---

## 🔐 Permissions

| Permission | Beschreibung |
|:--|:--|
| `jumpandrun.use` | Grundrecht für / jnr. |
| `jumpandrun.create` | JumpAndRuns erstellen. |
| `jumpandrun.delete` | JumpAndRuns löschen. |
| `jumpandrun.teleport` | Teleport in andere Welten (Staff). |
| `jumpandrun.list` | Alle JumpAndRuns auflisten. |
| `jumpandrun.ready` | Testmodus starten. |
| `jumpandrun.continue` | Drafts fortsetzen. |
| `jumpandrun.abort` | Testläufe abbrechen (basisrecht). |
| `jumpandrun.abort.keepworld` | Abbruch → Welt behalten. |
| `jumpandrun.abort.deleteworld` | Abbruch → Welt löschen. |
| `jumpandrun.unpublish` | Veröffentlichte Welt zurücksetzen. |
| `jumpandrun.sign.interact` | [JNR] / [JNR-LEADER] Schilder setzen, bearbeiten (Creative) und abbauen. |
| *(kein Recht nötig)* | [JNR] Schild klicken (Teleport). |

---

## 📚 Typische Abläufe

1. **Erstellen**
    - `/jnr create <länge>` → neue Welt mit Start/Ziel.

2. **Testen (Draft)**
    - `/jnr ready` → Testmodus startet.
    - Abschluss → Alias setzen, Teleport zurück, Schilder setzen.

3. **Spiel**
    - `[JNR]` Schild anklicken → Teleport zum Start.
    - Mit Barrier-Item vorzeitig abbrechen.

4. **Leaderboards**
    - Neue Rekorde → automatische Aktualisierung + Feuerwerk + Punkte.

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
```

---

## 🧩 Installation

1. Plugin-JAR in `plugins/` legen.
2. `config.yml` anpassen (DB, Punkte usw.).
3. Optional: [RankPointsAPI](https://github.com/timylinigersluz/RankPointsAPI) installieren.
4. Server neustarten.

---

## 📜 Credits

- Ursprünglicher Code: [Mooselux/JumpAndRun](https://github.com/Mooselux/JumpAndRun) ❤️
- Erweiterungen & Anpassungen: **KSR Minecraft Tecs**

---

### 🧠 Changelog (aktuelle Version)

- Neu: `jumpandrun.sign.interact` ersetzt alte sign-Permissions
- Neu: `/jnr abort keepworld|deleteworld` nur in Draft-Welten
- Neu: Advancements in JNR-Welten werden automatisch blockiert
- Verbesserte DB-Logik, Error-Handling & Debug-Ausgaben
