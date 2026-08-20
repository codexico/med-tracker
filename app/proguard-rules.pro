# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep class * { @androidx.room.Dao *; }
-keep class * { @androidx.room.Entity *; }
-keep class * { @androidx.room.Database *; }
-keep class * { @androidx.room.TypeConverter *; }

# Kotlin Serialization (if used)
-keepattributes *Annotation*, EnclosingMethod, Signature
-keepnames class kotlinx.serialization.internal.GeneratedSerializer { *; }
-keepclassmembers class * {
    *** Companion;
    *** $serializer;
}

# Jetpack Compose
-keep class androidx.compose.ui.platform.AndroidComposeView { *; }
-keep class androidx.compose.ui.platform.AbstractComposeView { *; }

# Keep your entities
-keep class com.franciscokahil.appMeusRemedinhos.data.local.EventEntity { *; }
-keep class com.franciscokahil.appMeusRemedinhos.data.local.MedicationTypeConverter { *; }
