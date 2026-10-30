# R8 / ProGuard rules for PulseCharge

# Keep Jetpack Compose & DataStore models
-keepclassmembers class * extends java.lang.Enum { *; }

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Keep DataStore Preferences classes
-keep class androidx.datastore.preferences.protobuf.** { *; }

# Optimize aggressively
-repackageclasses ''
-allowaccessmodification
