# MoneySav

# App code kecil; keep semua agar R8 tidak salah strip saat minify.
-keep class com.moneysav.app.** { *; }

# Room: database, entity, dan DAO hasil code-gen.
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Annotasi yang mungkin tidak terlihat R8.
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**
-dontwarn kotlin.jvm.internal.**
