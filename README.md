# HU Call Blocker 1.1.9

Magyar nyelvű, reklámmentes Android-hívásszűrő. Android 10-től használható, internetkapcsolat nélkül.

## 1.1.9 – animált hívásvédelem

- Aktív védelemkor nagyméretű, áttetsző fehér pipa a védelmi kártya hátterében.
- Maszkolt nemzetközi mintaszámok egyenként közelednek a pajzshoz, majd elpattannak és eltűnnek, rövid blokkolási hullámmal.
- A szemléltető animáció nem használ valós hívásadatokat, és kikapcsolt védelemkor nem fut.

## 1.1.8 – pontosított embléma és világosabb színvilág

- Az elfogadott eredeti kép kontúrjaiból vektorizált embléma minden ikonváltozathoz.
- Világos mentazöld főoldali védelmi kártya, kontrasztos sötét szövegek és állapotjelzések.
- A sötét téma többi felülete és a hívásszűrő működése változatlan.

## 1.1.7 – új embléma

- Új, vektoros HU-pajzs telefonkagylóval és tiltásjelzéssel.
- Egységes logó a fejlécen, főoldalon, mindkét indítóképernyőn és az alkalmazásikonon.
- Adaptív, monokróm és egyszerűsített állapotsori változat; a hívásszűrés változatlan.
- SVG embléma: [hu-call-blocker.svg](docs/branding/hu-call-blocker.svg).

## 1.1.6 – új megjelenés

- Egységes világos és sötét felület, rendszer szerinti témaválasztás.
- Új védelmi főoldal, letisztultabb kártyák és alsó navigáció.
- Kereshető, teljes képernyős ország- és számlisták.
- Dátum szerint csoportosított napló, részletező panel és gyors műveletek.
- Rövidebb indítóanimáció, adaptív alkalmazásikon.
- Kijelzőmérethez igazodó navigáció; széles kijelzőn oldalsó menü.

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
