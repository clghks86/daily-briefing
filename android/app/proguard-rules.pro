# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.clghks86.dailybriefing.** {
    *** Companion;
}
-keepclasseswithmembers class com.clghks86.dailybriefing.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.clghks86.dailybriefing.data.remote.dto.**$$serializer { *; }

# Glance / WorkManager
-keep class androidx.glance.** { *; }
-keep class * extends androidx.work.ListenableWorker {
    <init>(...);
}
