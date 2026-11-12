# MoneySav

Aplikasi Android pencatat keuangan personal. Kotlin + Jetpack Compose, tema MIUI / HyperOS,
dengan **Floating Liquid Glass Navigation Bar** asli (blur + refraksi) yang struktur dan
resep pengambilannya mengikuti [chengxixian/liquid-miuix](https://github.com/chengxixian/liquid-miuix).

## Fitur

- **Beranda** — saldo total, pemasukan & pengeluaran bulan ini, sisa anggaran, grafik arus kas 6 bulan, transaksi terbaru.
- **Transaksi** — tambah / ubah / hapus, kategori, catatan, tanggal, metode pembayaran, pencarian + filter (jenis, kategori, bulan).
- **Statistik** — pemasukan vs pengeluaran (harian / mingguan / bulanan / tahunan), donat pengeluaran per kategori, persentase perubahan, kategori terbesar.
- **Anggaran** — anggaran bulanan per kategori, progress otomatis, target tabungan, peringatan (banner + notifikasi) saat anggaran mendekati batas.

Semua angka diturunkan dari database Room yang sama, jadi saldo, grafik, dan progress
selalu konsisten dan otomatis diperbarui.

## Stack

| Komponen | Versi |
|---|---|
| AGP | 9.2.1 |
| Gradle | 9.4.1 |
| Kotlin | 2.4.20 |
| compileSdk / minSdk | 37 / 33 |
| miuix-ui | 0.9.4 |
| backdrop (Liquid Glass) | 2.0.1 |
| Material3 | 1.5.0-alpha22 |
| Room | 2.8.5 (KSP 2.3.13) |

## Struktur

```
app/src/main/java/com/moneysav/app/
├── data/db/          Entitas, DAO, AppDatabase, kategori bawaan
├── data/repository/  FinanceRepository (satu sumber data reaktif)
├── domain/           Model + FinanceCalculations (logika murni, teruji)
├── glass/            Liquid Glass: resep, modifier, nav bar (adaptasi liquid-miuix)
├── ui/components/    Kartu, list item, chip, grafik (bar + donat)
├── ui/navigation/    AppShell: sampling layer + nav bar kaca
├── ui/features/      home, transactions, statistics, budget (MVVM)
└── util/             Format Rupiah/tanggal, notifikasi anggaran
```

## Build

Build lewat GitHub Actions (`.github/workflows/build.yml`):

- `./gradlew :app:assembleDebug`
- `./gradlew :app:testDebugUnitTest`
- APK debug diunggah sebagai artifact `moneysav-debug-apk`.

Local: `./gradlew :app:assembleDebug` (butuh JDK 17 + Android SDK 37).

## Lisensi

Apache-2.0. Liquid Glass berutang pada `miuix-blur` dan `Kyant0/AndroidLiquidGlass`.
