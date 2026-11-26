# Kiadási aláírás

A HU Call Blocker 1.2.0 kiadás csomagazonosítója `hu.szujo.hucallblocker`, verziókódja `17`.

A kiadói kulcsot és a jelszavát **ne töltsd fel GitHubra vagy más nyilvános helyre**. Minden későbbi, közvetlenül terjesztett frissítést ugyanazzal a kulccsal kell aláírni. A kulcs elvesztése esetén a meglévő telepítések nem frissíthetők ezzel az aláírással.

- Kulcsformátum: JKS, RSA 4096
- Kulcsazonosító: `hucallblocker-release`
- Tanúsítvány SHA-256: `5C:63:82:59:14:63:D5:C0:02:73:5E:94:5D:EB:A3:79:43:7A:AD:04:09:A6:2D:F0:8D:B4:13:D7:29:85:83:F7`

A GitHubon csak az **aláíratlan** kiadás fordítható. Helyi aláíráshoz az Android SDK `zipalign` és `apksigner` eszközeit használd:

```powershell
$eszkozok = "E:\Android\Sdk\build-tools\36.0.0"
& "$eszkozok\zipalign.exe" -P 16 -f 4 "app-release-unsigned.apk" "app-release-aligned.apk"
& "$eszkozok\apksigner.bat" sign --ks "E:\Android\Signing\HUCallBlocker_release_signing_key.jks" --ks-key-alias hucallblocker-release --out "HUCallBlocker_release.apk" "app-release-aligned.apk"
& "$eszkozok\apksigner.bat" verify --verbose --print-certs "HUCallBlocker_release.apk"
```

A jelszót az aláíróprogram kérésére add meg, ne írd parancsba vagy forrásfájlba.

A fejlesztői és a kiadói APK eltérő kulccsal van aláírva, így a kettő nem telepíthető egymásra közvetlenül. Az alkalmazás eltávolítása törölheti a helyi beállításokat és naplót.
