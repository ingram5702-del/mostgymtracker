# Firebase Firestore
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ML Kit Barcode
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Kotlin Coroutines
-dontwarn kotlinx.coroutines.**

# Keep data classes used with Firestore
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
}

# CameraX
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# Compose
-dontwarn androidx.compose.**

# Protobuf (used by Firebase/gRPC)
-dontwarn com.google.protobuf.**
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }
