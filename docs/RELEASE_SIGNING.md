# Release signing

HU Call Blocker **1.1.3**, applicationId `hu.szujo.hucallblocker`, versionCode `10`.

## A kiadói kulcs

A telepíthető, közvetlenül terjesztett release APK önálló **RSA 4096** kiadói kulccsal lett aláírva (APK Signature Scheme v2/v3). A privát kulcs és jelszava **soha nem kerülhet a repóba**.

Certificate SHA-256:

```
5C:63:82:59:14:63:D5:C0:02:73:5E:94:5D:EB:A3:79:43:7A:AD:04:09:A6:2D:F0:8D:B4:13:D7:29:85:83:F7
```

Alias: `hucallblocker-release`. Keystore: JKS.

**Fontos:** Az aláírókulcsot és a hozzá tartozó jelszót a tulajdonosnak külön, biztonságos offline helyen meg kell őriznie. Ha elveszik, a korábban telepített, közvetlenül terjesztett release APK-ra ugyanazzal az aláírással többé nem lehet frissítést kiadni.

## Fordítás (aláírás nélkül)

GitHub Actions: [Release unsigned build](../.github/workflows/release-unsigned.yml).

Helyben:

```powershell
.\gradlew.bat testDebugUnitTest assembleRelease
```

A kapott `app-release-unsigned.apk` **nem telepíthető**, amíg nincs aláírva.

## Aláírás helyben Windows alatt

Az Android SDK Build Tools (pl. `E:\Android\Sdk\build-tools\36.0.0`) eszközeivel:

```powershell
$tools = "E:\Android\Sdk\build-tools\36.0.0"
& "$tools\zipalign.exe" -P 16 -f 4 "app-release-unsigned.apk" "app-release-aligned.apk"
& "$tools\apksigner.bat" sign --ks "E:\Android\Signing\HUCallBlocker_release_signing_key.jks" --ks-key-alias hucallblocker-release --out "HUCallBlocker_release.apk" "app-release-aligned.apk"
& "$tools\apksigner.bat" verify --verbose --print-certs "HUCallBlocker_release.apk"
```

Jelszót **csak az apksigner kérésére**, ne parancssorban vagy forrásfájlban adj meg.

A debug APK és a release APK eltérő aláírással készül: a debug verzióra közvetlenül nem telepíthető rá a release. Az eltávolítás alkalmazásadat-vesztést okozhat.

## Tesztállapot

A hívásszűrés alapfunkcióit (nem mentett külföldi szám, magyar szám, mentett külföldi névjegy, újraindítás utáni blokkolás) Pixel 10 emulátoron ellenőriztük. A **Xiaomi/HyperOS éles rendszerintegrációja még külön tesztelendő**.
