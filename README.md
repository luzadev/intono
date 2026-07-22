# NoteMusicali

App multipiattaforma per l'apprendimento musicale, costruita con **Kotlin Multiplatform** e **Compose Multiplatform**.

## Funzionalita

- **Controllo Intonazione** — rileva in tempo reale la nota suonata tramite microfono (algoritmo YIN)
- **Metronomo** — BPM regolabile, time signature, tap tempo e tuner integrato per suonare a tempo verificando l'intonazione
- **Nota di Riferimento** — genera qualsiasi nota cromatica con frequenza esatta per accordatura
- **Pratica guidata** — segui sequenze di note con feedback visivo e sonoro, metronomo integrato, modalita "Nascondi" per lettura dal pentagramma
- **Sfida** — suona il maggior numero di note nel tempo con punteggio, combo e pentagramma
- **Ear Training** — riconoscimento di note e intervalli con difficolta progressiva
- **Esercizi predefiniti** — scale, arpeggi e sequenze ordinate per difficolta
- **Inserimento manuale** — componi sequenze personalizzate con notazione italiana e durate musicali
- **Scansione spartiti** — riconosci note da foto di spartiti tramite AI (OpenAI / Gemini)
- **Pentagramma interattivo** — visualizzazione grafica delle note sul rigo musicale
- **Obiettivi giornalieri** — traccia il tempo di pratica da tutte le attivita con serie consecutiva e calendario
- **Cronologia unificata** — tutte le sessioni (pratica, ear training, sfide) in un'unica lista

## Piattaforme supportate

| Piattaforma | Stato | Note |
|-------------|-------|------|
| Android     | Funzionante | Richiede API 26+ (Android 8.0) |
| iOS         | Funzionante | Richiede iOS 16+, audio via AVAudioEngine |
| macOS       | Funzionante | Desktop app via Compose Desktop |

## Struttura progetto

```
NoteMusicali/
  app/              # Android app module
  shared/           # Kotlin Multiplatform shared code
    commonMain/     #   Logica condivisa (UI, audio, pitch detection)
    androidMain/    #   Implementazioni Android (AudioRecord)
    iosMain/        #   Implementazioni iOS (AVAudioEngine)
    desktopMain/    #   Implementazioni Desktop (javax.sound)
  iosApp/           # Xcode project (SwiftUI entry point)
  desktopApp/       # Desktop app module (JVM)
```

## Prerequisiti

- **JDK 17+**
- **Android Studio** (per Android)
- **Xcode 15+** (per iOS)
- **XcodeGen** (per generare il progetto iOS): `brew install xcodegen`

## Build

### Android

```bash
./gradlew :app:assembleDebug
```

L'APK si trova in `app/build/outputs/apk/debug/`.

### iOS

```bash
# Genera il progetto Xcode
cd iosApp && xcodegen generate

# Compila il framework Kotlin
./gradlew :shared:compileKotlinIosArm64

# Apri in Xcode, seleziona il device e Build & Run
open iosApp/iosApp.xcodeproj
```

Il pre-build script in Xcode esegue automaticamente `embedAndSignAppleFrameworkForXcode`.

### macOS (Desktop)

```bash
./gradlew :desktopApp:run                    # Esegui
./gradlew :desktopApp:packageDmg             # Genera DMG
```

## Strumenti presets

L'app supporta preset ottimizzati per diversi strumenti:

| Strumento   | Range       | Buffer |
|-------------|-------------|--------|
| Violino     | G3 - E7     | 2048   |
| Viola       | C3 - A6     | 2048   |
| Violoncello | C2 - C6     | 4096   |
| Pianoforte  | A0 - C8     | 4096   |
| Chitarra    | E2 - E6     | 2048   |
| Voce        | D2 - D6     | 2048   |

## Stack tecnologico

- **Kotlin 2.1** + **Compose Multiplatform 1.7**
- **Ktor** per le chiamate API (scansione spartiti)
- **multiplatform-settings** per preferenze persistenti
- **YIN algorithm** per pitch detection in tempo reale
- **AVAudioEngine** (iOS) / **AudioRecord** (Android) / **javax.sound** (Desktop)
