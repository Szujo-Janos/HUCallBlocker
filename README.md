# HU Call Blocker v1.0.1

**Offline, reklámmentes, magyar nyelvű Android-hívásszűrő.** Kotlin + Android Views (nem Jetpack Compose), Android 10 vagy újabb.

## Mit csinál?

- A nemzetközi +36 / 0036 előhívószámú hívásokat átengedi.
- A más nemzetközi előhívóval megjelenő bejövő hívásokat a beállított rendszer-hívásszűrő szerepkörrel elutasítja.
- A bizonytalan, országkód nélküli vagy rejtett számokat **nem blokkolja automatikusan**.
- A mentett névjegyek alapértelmezés szerint kivételek. A mentettek tiltásához az Android READ_CONTACTS engedélye szükséges.
- Legfeljebb 100 elutasított hívásról helyi alkalmazásnaplót tárol.
- Nem kér internetjogosultságot, nem használ SMS-t, és nem igényel rootot.

**Fontos:** Az előhívó nem bizonyítja, hogy a hívó ténylegesen melyik országból telefonál. A számhamisítás sem szűrhető ezzel. A rendszer csak bizonyos bejövő hívásokat ad át a CallScreeningService-nek. Xiaomi/HyperOS és más gyártói rendszereken a viselkedést valódi hívásokkal ellenőrizni kell.

## Android Studio – első megnyitás

1. Klónozd a https://github.com/Szujo-Janos/HUCallBlocker repót (Android Studio → **Get from VCS**, vagy GitHub Desktop → Clone).
2. Android Studio → **File → Open**, válaszd a klónozott HUCallBlocker könyvtárat.
3. Várd meg a Gradle Sync-et. Szükség esetén telepítsd az **Android SDK 35**-öt. Gradle JDK: **17**.
4. USB hibakereséses telefonnal indíthatod a **Run** gombbal.
5. Az alkalmazásban engedélyezd az Android rendszerben a **Hívásszűrés** szerepkört.
6. Teszteld egy valós, kijelzett külföldi előhívószámú hívással.

A Gradle Wrapper (gradlew, gradlew.bat és a hivatalos 8.13-as gradle-wrapper.jar) a repó része; nem kell új projektet generálni.

## Ha korábban a Compose sablonnal kezdtél

A korábbi Android Studio-projekt `ui/theme/Color.kt`, `Theme.kt` és `Type.kt` fájljai Compose-függőségeket igényelnek, amelyek **nincsenek** ebben a projektben. Javasolt a GitHub-repót külön mappába klónozni és azt megnyitni, nem pedig a régi sablont rákeverni.

## Fejlesztői ellenőrzés

- Egységtesztek: `./gradlew testDebugUnitTest` (Windows: `gradlew.bat testDebugUnitTest`)
- APK: Android Studio → **Build → Build APK(s)**, majd `app/build/outputs/apk/debug/app-debug.apk`
- A számteszt nem indít valós hívást, és nem bizonyítja a gyártói rendszer hívásszűrésének működését.

## Adatvédelem

Az alkalmazás saját, privát beállítástárában tárolja a blokkolási naplót. Internetkapcsolatra nincs szükség. A napló törölhető az alkalmazásból.

## Jelenlegi állapot

A forráskód előkészített, és a Compose-függőségeket eltávolítottuk. **Android SDK-s build és valós készülékes hívásteszt még nem történt**, ezért a következő ellenőrzés Android Studióban szükséges.

Hivatalos Android-dokumentáció: https://developer.android.com/reference/android/telecom/CallScreeningService
