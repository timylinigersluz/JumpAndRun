# 📋 Changelog - JumpAndRun Plugin (KSR Edition)

## Neueste Änderungen

### 🔥 Neue Features
- **SQLite-Support**: Falls `jumpandrun.enabled=false`, wird automatisch eine lokale SQLite-Datenbank (`jumpandrun.db`) im Plugin-Ordner verwendet.
- **Aufgeben-Item**: Spieler erhalten beim Betreten einer JNR-Welt ein Barrier-Item. Rechtsklick → Welt verlassen, Inventar clear, Sound- und Partikeleffekte.
- **Alias-Workflow erweitert**: Nach erfolgreicher Eingabe des Aliases beim Ready-Flow erhält der Spieler automatisch ein OAK-Schild zum Platzieren von `[JNR]` und `[JNR-LEADER]` Schildern.
- **Automatisches Inventar-Clearing**: Beim Betreten oder Verlassen einer JNR-Welt wird das Inventar geleert (mit Ausnahme des Spezialfalls „Alias abgeschlossen“, dort erhält der Spieler ein Schild).
- **Feuerwerk bei Rekorden**: Neue Rekorde lösen eine Feuerwerks-Animation beim Spieler aus.
- **Optionale Punktevergabe**: Punkte (RankPointsAPI) werden nur vergeben, wenn `pointsdb.enabled=true` in der Config aktiviert ist.

### ⚙️ Verbesserungen
- Stabile Speicherung von Checkpoints, Runs und Zeiten in MySQL oder SQLite.
- Fehlerbehandlung verbessert: Startzeit bleibt auch nach Void-Fällen oder Tod gültig (wird nur beim Verlassen der Welt gelöscht).
- Welt-Locking überarbeitet: Nur ein Spieler pro Welt gleichzeitig möglich.

### 🐛 Fixes
- Fehler bei Rekordvergabe ohne vorhandene Startzeit behoben.
- Alias-Vergabe im Chat blockiert die Nachricht korrekt für andere Spieler.
- Bei Void-Fall wird kein aktiver Run mehr gelöscht, sondern korrekt zurückgesetzt.
