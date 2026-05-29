# Default ProGuard rules for the infinity app.

# Keep model classes intact so Firebase can deserialize them via reflection.
-keep class com.infinity.app.models.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class com.bumptech.glide.** { *; }

# Firebase
-keepattributes Signature
-keepattributes *Annotation*
