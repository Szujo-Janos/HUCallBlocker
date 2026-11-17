# HU Call Blocker 1.1.3 — Jetpack Compose

Android 10+ (API 29), magyar nyelvű offline hívásszűrő. 
A korábbi XML-es felületet Kotlin Jetpack Compose / Material 3 váltja. 
A hívásszűrő és az előző verziókban mentett kapcsolók változatlanok.

## Kijelzőhöz igazodó felület (1.1.3)
- A fejléc az Android státuszsávja és a kijelzőkivágás alá kerül a Compose safeDrawing inset kezelésével.
- Kompaktabb főoldal kisebb képernyőn; nagy betűméretnél megmarad a görgethetőség.
- A teljes szélességű frissítésgomb helyett fejlécben lévő frissítésikon.
- Az alsó navigációt nem módosítja; az értesítési és hívásszűrési működés változatlan.

## Rendszer-splash finomítása (1.1.2)
- Külön, 288×288 dp-es vektorból készülő, kis méretű HU-védjegy az Android natív splashhez.
- Az eredeti alkalmazásikon és a második, forgó fénygyűrűs Compose-intro változatlan.
- A natív splash elhalványulása 160-ról 85 ms-ra csökkent; a rendszer saját indítási ideje készülékfüggő.
- Nem hoz létre új splash Activityt, nincs dupla várakozás.

## Új animált indítás (1.1.1)
- Egységes sötét Android-rendszer splash és teljes képernyős Compose animáció
- Lélegző HU-pajzs, forgó kettős fénygyűrű, lágyan belépő felirat és indítási fénycsík
- A főképernyő először az animáció befejezésekor jelenik meg; a háttérben a hívásszűrés működése változatlan
- Nincs hálózati betöltés vagy kötelező várakozás a hívásszűrésnek; az animáció csak az app újraindításakor jelenik meg

## Megújult kezelőfelület
- natív AndroidX SplashScreen HU logóval, rövid kilépési animációval
- animált státuszkártya, finom Compose átmenetek
- alsó navigáció: Főoldal / Szűrés / Napló / Beállítások
- HU pajzsikon az alkalmazásban és az állapotsori értesítésben

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
