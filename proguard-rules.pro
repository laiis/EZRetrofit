# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/laiis/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:

-optimizationpasses 10
-mergeinterfacesaggressively
-dontusemixedcaseclassnames
#-overloadaggressively # don't use this
-obfuscationdictionary proguard_java_keyword.txt
-classobfuscationdictionary proguard_java_keyword.txt
-packageobfuscationdictionary proguard_java_keyword.txt
-flattenpackagehierarchy ccc
#-repackageclasses ccc

-keepattributes SourceFile,LineNumberTable

# keep annotation
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# simplexml
-keep public class org.simpleframework.**{ *; }
-keep class org.simpleframework.xml.**{ *; }
-keep class org.simpleframework.xml.core.**{ *; }
-keep class org.simpleframework.xml.util.**{ *; }
-keepattributes *Annotation*
-keepattributes SourceFile
-keepattributes LineNumberTable
-dontwarn javax.xml.stream.XMLEventReader
-dontwarn javax.xml.stream.XMLInputFactory
-dontwarn javax.xml.stream.events.Attribute
-dontwarn javax.xml.stream.events.XMLEvent
-dontwarn javax.xml.stream.events.StartElement
-dontwarn javax.xml.stream.Location
-dontwarn javax.xml.stream.events.Characters

# okio
-keep class okio.**{
    *;
}
-dontwarn java.nio.file.Files
-dontwarn java.nio.file.Path
-dontwarn java.nio.file.OpenOption
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# okhttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *;}

-dontwarn tw.idv.laiis.ezretrofit.**
-keep class tw.idv.laiis.ezretrofit.EZCallback { *; }
-keep class tw.idv.laiis.ezretrofit.EZRetrofit { *; }
-keep class tw.idv.laiis.ezretrofit.EZRetrofit$* { *; }
-keep class tw.idv.laiis.ezretrofit.RetrofitConf { *; }
-keep class tw.idv.laiis.ezretrofit.RetrofitConf$* { *; }
-keep class tw.idv.laiis.ezretrofit.managers.EZRetrofitTrustManager { *; }
-keep class tw.idv.laiis.ezretrofit.cookies.CookieStoreRepo { *; }
-keep class tw.idv.laiis.ezretrofit.cookies.PersistentCookieStore { *; }
-keep class tw.idv.laiis.ezretrofit.cookies.SerializableHttpCookie { *; }