# Shadow Run 2D

Game Android 2D action-platformer sederhana, orientasi landscape, kontrol sentuh.

## Fitur
- Gerak kiri/kanan dan lompat
- Platform bertingkat
- Musuh patroli dan bisa dikalahkan dengan lompatan
- Koin, skor, 3 HP, kondisi menang/kalah, restart
- Build APK otomatis dengan GitHub Actions

## Struktur
- `app/src/main/java/.../MainActivity.java` — activity Android
- `app/src/main/java/.../GameView.java` — seluruh logika dan gambar game
- `app/src/main/AndroidManifest.xml` — konfigurasi aplikasi
- `app/build.gradle` — konfigurasi modul Android
- `.github/workflows/android.yml` — workflow build APK

## Build lewat HP dengan GitHub
1. Buat repository GitHub baru bernama `ShadowRun2D`.
2. Ekstrak ZIP ini, lalu upload **isi folder** ke root repository (jangan upload folder ZIP sebagai satu file).
3. Pastikan `.github/workflows/android.yml` ikut ter-upload.
4. Buka tab **Actions** dan pilih workflow **Build Android APK**.
5. Jika belum berjalan, tekan **Run workflow**.
6. Setelah status hijau, buka hasil run dan unduh artifact `ShadowRun2D-debug-apk`.
7. Ekstrak artifact ZIP, lalu instal `app-debug.apk` di Android.

## Catatan
Ini adalah starter game 2D yang bisa dikembangkan. Build pertama membutuhkan koneksi internet di GitHub Actions.
