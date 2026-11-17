# HU Call Blocker v1.0.5

Material 3 világos UI, HU pajzs ikon és állapotértesítés.

## Fordítás javítása
A régi projektben maradt stílusok `@color/teal` és `@color/background`
hivatkozásai újra elérhetők. A régi `AppTheme` név az új Material 3 témához kötődik.

## Telepítés
A ZIP-et az Android Studio projekt **gyökérmappájába** csomagold ki,
**a fájlokat felülírva**, majd Sync Project with Gradle Files → Build / Rebuild Project.

A pajzsikon a rendszer értesítési jogosultságától függ, a valódi blokkolás
pedig az Android hívásszűrő szerepkörtől. A Xiaomi készülékes ellenőrzés szükséges.
