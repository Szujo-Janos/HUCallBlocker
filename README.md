# HU Call Blocker 1.1.3 — Jetpack Compose

Android 10+ (API 29), magyar nyelvű offline hívásszűrő. 

## Működés
1. Telepítés után a **Hívásszűrés engedélyezése** gombbal kérd a rendszer szerepkörét.
2. A +36 vagy 0036 kezdetű megjelenő számok átjöhetnek; más külföldi nemzetközi számok tiltva vannak.
3. A **Mentett névjegyek átengedése** opció külön beállítható.
4. Ha az Android 13+ értesítési engedélyt megadod, a HU állapotsori jelzés is látható lehet.
5. Valós hívásokkal, a telefon újraindítása után is ellenőrizd a működést Xiaomi/HyperOS és más készülékeken.

### Rejtett / magán számok
Az Android a PRESENTATION_RESTRICTED, UNKNOWN, UNAVAILABLE és PAYPHONE hívásokat **nem adja át a CallScreeningService-nek**. Így ez az alkalmazás közvetlenül nem tudja őket blokkolni. A **Szűrés → Rejtett számok → Telefon beállításai** gomb a rendszer blokkolási képernyőjét (ha elérhető) vagy a telefon appot nyitja. Ott a gyártó telefonalkalmazásában lehet külön a „Rejtett / Ismeretlen” blokkolást bekapcsolni. A gomb **nem** állítja automatikusan ezt a rendszerszabályt.
A „rejtett” nem egyenlő a nem mentett, de látható számmal.

## Fordítás
Android Studio: JDK 17, Android SDK 36, Gradle Sync, **Build → Rebuild Project**.
CI: `./gradlew testDebugUnitTest assembleDebug`.

Dokumentáció: https://developer.android.com/reference/android/telecom/CallScreeningService
