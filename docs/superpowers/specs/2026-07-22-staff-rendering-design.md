# Design: rendering avanzato del pentagramma (InTono)

Data: 2026-07-22 — Approccio approvato: **disegno raffinato su Canvas** (nessun font musicale embedded).

## Obiettivo

Migliorare la resa delle note sul pentagramma nelle viste Compose condivise con quattro upgrade approvati dall'utente:

1. **Glifi raffinati** — teste "a pera" (`Path`), code curve riempite, ♯/♭ ben proporzionati, chiave a piena opacità e dimensione maggiore.
2. **Travature (beam)** — crome/semicrome consecutive nello stesso beat unite da parallelogrammi inclinati.
3. **Battute** — stanghette e cifre del tempo (es. 4/4) dopo la chiave.
4. **Spaziatura proporzionale** — larghezza orizzontale ∝ √(durata in quarti).

Non-obiettivi: font SMuFL/Bravura (rimandabile senza rework: la geometria resta valida), pause, legature di portamento, chiavi miste nello stesso rigo, gambi condivisi tra righi.

## Architettura

### 1. `StaffLayout` (esteso) — geometria pura, `commonMain`, testabile

- `quarterUnits(duration: NoteDuration): Double` — WHOLE=4, HALF=2, QUARTER=1, EIGHTH=0.5, SIXTEENTH=0.25, THIRTY_SECOND=0.125.
- `beamGroups(notes: List<MusicalNote>, beats: Int, beatType: Int): List<IntRange>` — gruppi di indici di note beamabili (durata ≤ EIGHTH) consecutive che iniziano nello stesso beat; gruppi di 1 elemento esclusi (tengono la coda). Il confine di beat spezza il gruppo.
- `measurePositions(notes: List<MusicalNote>, beats: Int, beatType: Int): List<Int>` — indici *dopo* i quali cade una stanghetta (accumulo di quarti ≥ capacità battuta; best effort quando le durate non riempiono esattamente la battuta: la stanghetta cade al superamento, mai eccezioni).
- `spacingWeights(notes: List<MusicalNote>): List<Float>` — pesi orizzontali `sqrt(quarterUnits)`, normalizzati dal chiamante sull'ampiezza disponibile.
- `stemUp(relPos: Int): Boolean` (regola esistente `relPos < 4`); per un beam group la direzione è quella della maggioranza delle note del gruppo.

### 2. `StaffRenderer` (nuovo file, `ui/components`) — estensioni `DrawScope`

Primitive di disegno usate da tutte le viste:

- `drawNoteHeadPath` — testa a pera via `Path` (quadratiche, come il mockup approvato), piena per ≤ QUARTER, contorno per WHOLE/HALF; rotazione implicita nella forma.
- `drawCurvedFlag` — coda a goccia riempita (`Path` cubica), 1–3 code per croma/semicroma/biscroma, specchiata per gambo in giù.
- `drawBeam` — parallelogramma tra due estremità di gambo, spessore ~0.5·lineSpacing, pendenza limitata (±0.5·lineSpacing per nota); doppia barra per semicrome.
- `drawSharpGlyph` / `drawFlatGlyph` — esistenti, raffinati (proporzioni del mockup).
- `drawBarline`, `drawTimeSignature(beats, beatType)` — stanghetta sottile a tutta altezza rigo; cifre con `TextMeasurer` (bold, serif di sistema).
- `drawClef` — glifo Unicode esistente ma a piena opacità/colore nota e dimensione maggiore.

Le funzioni esistenti in `NoteRenderer.kt` (drawNoteHead/drawNoteStem/drawNoteFlags) vengono sostituite dalle nuove primitive; il file viene aggiornato o assorbito da StaffRenderer.

### 3. Modello e parser

- `NoteSequence` guadagna `beats: Int = 4`, `beatType: Int = 4`.
- `MusicXmlParser` estrae la prima occorrenza di `<time><beats>N</beats><beat-type>M</beat-type></time>` nella prima parte; assente → 4/4.
- Inserimento manuale, scale, esercizi, scansione AI: default 4/4.

### 4. Applicazione per vista

| Vista | Glifi | Travature | Battute+Tempo | Spaziatura |
|---|---|---|---|---|
| StaffFullView | ✅ | ✅ | ✅ | ✅ (per rigo) |
| StaffContextView | ✅ | ✅ | ✅ (se nella finestra) | ✅ |
| StaffPreview | ✅ | ✅ | — | ✅ |
| StaffView (nota singola) | ✅ | — | — | — |

L'evidenziazione (nota corrente oro, suonate sbiadite) resta identica. In StaffFullView il wrapping per riga passa da "N note fisse" a "capienza per pesi", con le battute che preferibilmente non si spezzano a fine riga quando possibile (best effort: si spezza sul confine di battuta più vicino che sta nella riga; se una battuta è più larga della riga, si spezza comunque).

## Gestione errori

- Sequenze senza time signature → 4/4.
- Durate che non riempiono la battuta (anacrusi, parser lossy) → stanghetta al superamento della soglia; nessuna eccezione.
- Beam group con note su registri lontani → pendenza della travatura clampata.
- Sequenze vuote → nessun disegno (comportamento attuale).

## Test

- **TDD (commonTest)**: `quarterUnits` per tutte le durate; `beamGroups` (coppie di crome, croma singola, confine di beat in 4/4 e 6/8, misto crome/semicrome, interruzione su nota lunga); `measurePositions` (4/4 esatto, 3/4, durate eccedenti, sequenza vuota); `spacingWeights` (monotonia, rapporto √); parser `<time>` (presente, assente, 6/8).
- **Verifica visiva**: lancio dell'app desktop e ispezione di Esercizi/Spartiti/Pratica (screenshot).

## Rischi

- La resa estetica di path disegnati a mano richiede iterazione visiva: prevista una passata di tuning dopo la prima implementazione, con l'app desktop come banco di prova.
- StaffFullView cambia il layout di wrapping: verificare l'auto-scroll alla riga corrente (già esistente) con le nuove larghezze.
