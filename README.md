# HU Call Blocker v1.0.3

Kotlin + Android Views Android-alkalmazás, minimum Android 10 (API 29), offline és reklámmentes.

## Mit tud?

- Magyar \`+36\` és \`0036\` számokat átengedi, nemzetközi külföldi előhívót elutasít, ha a hivatalos Android hívásszűrő szerepkör aktív.
- A mentett névjegyek alapértelmezés szerint átjöhetnek. A kivétel kikapcsolásához névjegyengedély kell.
- A bizonytalan vagy rejtett számokat nem tiltja automatikusan.
- A blokkolásokból helyi napló készül, legfeljebb 100 bejegyzéssel.
- Átdolgozott, sötét natív főképernyővel rendelkezik; nem használ Jetpack Compose-t.
- Aktív és engedélyezett állapotban fehér pajzsikon jelenhet meg az Android értesítési/állapotsorában; a védelmi jogosultság és a kapcsoló állapotát ellenőrzi.
- Android 13+ esetén külön \`POST_NOTIFICATIONS\` engedély szükséges; a hiányzó engedély **nem kapcsolja ki a hívásszűrést**.
- A \`BOOT_COMPLETED\` esemény után ismét szinkronizálja az értesítést. Nem futtat állandó háttérszolgáltatást.

### Állapotsori ikon – korlátok

Az ikon egy állapotértesítés, **nem** a védelem technikai feltétele és **nem** valós idejű, megmásíthatatlan rendszerállapot-mérő. Android 14+ rendszeren a felhasználó elhúzhatja az ilyen értesítést. A gyártó vagy a felhasználói értesítési beállítások el is rejthetik az ikont. Ha a rendszer a háttérben megvonja a szerepkört, az ikon a következő szinkronig tévesen is megmaradhat; megnyitáskor, hívás után és újraindítás után újra ellenőrzi a jogosultságot.

A \`BOOT_COMPLETED\` egyes eszközökön csak az első képernyőfeloldás után érkezik meg. Xiaomi/HyperOS alatt a viselkedést készüléken ellenőrizni kell.

Az Android \`CallScreeningService\` rendszere a tényleges hívásszűréshez nem igényel folyamatosan futó alkalmazást. A telefonszám országhívója nem jelenti a hívó tényleges földrajzi helyét; a hamisított \`+36\` szám nem azonosítható.

## Android Studio

1. Klónozd: https://github.com/Szujo-Janos/HUCallBlocker
2. Android Studio → File → Open (a klónozott projekt könyvtára).
3. Gradle Sync (JDK 17, Android SDK 35), Run.
4. A telefonon engedélyezd a **Hívásszűrés** szerepkört a HU Call Blockernek.
5. Android 13+ esetén fogadd el az értesítési jogosultságkérést, ha szeretnéd az ikont a felső sávban.
6. Teszteld egy valódi bejövő külföldi számú hívással, majd telefon-újraindítás után is.

## Fejlesztés és QA

- \`gradlew.bat testDebugUnitTest assembleDebug\` Windows alatt.
- GitHub Actions automatikusan fordít és a debug APK-t artifactként elérhetővé teszi.
- Nem használ internet-, SMS- vagy fölösleges telefonjogosultságot.

Android dokumentáció:
- https://developer.android.com/reference/android/telecom/CallScreeningService
- https://developer.android.com/develop/ui/views/notifications/notification-permission
