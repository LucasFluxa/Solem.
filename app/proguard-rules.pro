# Add project specific ProGuard rules here.
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Moshi
-dontwarn com.squareup.moshi.**
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}
-keep class com.example.data.dto.** { *; }
-keep class com.example.data.local.entity.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# DataStore & Coroutines
-dontwarn androidx.datastore.**
-dontwarn kotlinx.coroutines.**

# Compose
-keepclassmembers class androidx.compose.ui.platform.AndroidComposeView { *; }
