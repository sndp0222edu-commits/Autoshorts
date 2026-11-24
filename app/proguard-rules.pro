# Keep WebAppInterface and methods annotated with @JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
