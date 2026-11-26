# HU Call Blocker 1.1.5

Magyar nyelvű, reklámmentes Android-hívásszűrő. Android 10-től használható, internetkapcsolat nélkül.

## Mit tud?

- Külföldi, nemzetközi előhívójú hívások blokkolása
- Magyar (+36) hívások átengedése
- Mentett névjegyek átengedése, ha megadod a névjegyengedélyt
- Országonkénti kivételek (például Németország és Ausztria)
- Saját engedélyezett és tiltott számok
- Helyi napló országgal, tiltási okkal és gyors engedélyezéssel

A saját tiltólista az engedélyezett számoknál és országoknál is erősebb. A saját engedélyezett számok és a mentett névjegyek megelőzik az országszűrést. A magyar számok csak akkor tiltódnak, ha kifejezetten a saját tiltólistára teszed őket. A szűrés kikapcsolásával minden saját szabály szünetel.

A számok országát az előhívó alapján azonosítjuk: ez nem a hívó földrajzi helye, és a hamisított hívószámokat sem ismeri fel biztosan. A rejtett hívószámok tiltását Androidon a telefon saját beállításaiban lehet kezelni, ha támogatott.

## Beüzemelés

Telepítés után engedélyezd a hívásszűrési szerepkört. A mentett külföldi ismerősök felismeréséhez külön névjegyengedély szükséges. Az állapotsori pajzshoz az értesítéseket is engedélyezheted.

A korábbi beállítások és naplóbejegyzések megmaradnak az alkalmazás frissítésekor.

## Fordítás

Android Studio, JDK 17, Android SDK 36.

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

A kiadási APK aláírásához ugyanazt a kulcsot kell használni, mint a korábbi verzióhoz: [aláírási útmutató](docs/alairas.md).
