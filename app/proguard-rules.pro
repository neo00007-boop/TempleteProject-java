# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\bigSdk\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
#算法规则
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-dontwarn
-dontskipnonpubliclibraryclassmembers
-allowaccessmodification
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
# 不要删除无用代码
-dontshrink
# 不混淆本地方法
-keepclasseswithmembernames class * {
    native <methods>;
}
# 不混淆 Activity 在 XML 布局所设置的 onClick 属性值
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
# 不混淆 Parcelable 子类
-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

# 不混淆 Serializable 子类
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
# 不混淆 R 文件中的字段
-keepclassmembers class **.R$* {
    public static <fields>;
}

# 不混淆 WebView 设置的 JS 接口的方法名
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
#基本组件
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep class **.R$* { *;
}
-keep class **.R$* { *;
    public static <fields>;
}
# Keep native methods
-dontwarn com.laiy.lyapp.**
-keepclassmembers class * {
    native <methods>;
}
#反射
-keepattributes Signature
-keepattributes *Annotation*
-keep public class com.google.gson.**{*;}
-keep class sun.misc.Unsafe.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }
-dontwarn com.google.gson.**
-dontwarn sun.misc.Unsafe
-dontwarn com.google.gson.examples.android.model.**
#apache
-keep class org.apache.http.** {*;}
-keep class android.net.** { *; }
-keep class com.android.internal.http.multipart.** { *; }
-keep class org.apache.** { *; }
-keep class org.apache.commons.codec.binary.** { *; }
-dontwarn org.apache.http.**
-dontwarn android.net.**
-dontwarn com.android.internal.http.multipart.**
-dontwarn org.apache.**
-dontwarn org.apache.commons.codec.binary.**
-ignorewarnings
#annotations
-keep class android.support.annotation.** {*;}
-dontwarn com.qiniu.pili.droid.streaming.**
#七牛
-keep class com.qiniu**{*;}
-keep class com.qiniu**{public <init>();}
-ignorewarnings
#拼音工具混淆
-dontwarn demo.**
-keep class demo.**{*;}
-dontwarn net.sourceforge.pinyin4j.**
-keep class net.sourceforge.pinyin4j.**{*;}
-keep class net.sourceforge.pinyin4j.format.**{*;}
-keep class net.sourceforge.pinyin4j.format.exception.**{*;}
#QQ混淆
-keep class com.tencent.** { *; }
-dontwarn com.tencent.**
#其他jar
-dontwarn android.support.**
-keep class android.support.** {*;}
-dontwarn com.squareup.picasso.**
-keep class com.squareup.picasso.** {*;}
#短信验证
-keep class cn.sharesdk.**{*;}
-keep class cn.smssdk.**{*;}
-keep class com.mob.**{*;}
-dontwarn com.mob.**
-dontwarn cn.smssdk.**
#bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

#支付混淆
-keep class cn.trinea.android.common.** {*;}
-dontwarn cn.trinea.android.common.**

#友盟混淆
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
#xutil混淆
-keep class com.lidroid.xutils.**{*;}
-dontwarn com.lidroid.xutils.**
#百度地图
-keep class com.baidu.** {*;}
-keep class vi.com.** {*;}
-dontwarn com.baidu.**
#------------------------------------------------------------------------------------------------------------glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule
#------------------------------------------------------------------------------------------------------------rxjava rxbinding
-dontwarn java.util.concurrent.Flow*
#------------------------------------------------------------------------------------------------------------okio
# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*
#------------------------------------------------------------------------------------------------------------okhttp
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt and other security providers are available.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
#------------------------------------------------------------------------------------------------------------retrofit
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
#-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
#------------------------------------------------------------------------------------------------------------gson
##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

##---------------End: proguard configuration for Gson  ----------
#------------------------------------------------------------------------------------------------------------aroute
-keep public class com.alibaba.android.arouter.routes.**{*;}
-keep public class com.alibaba.android.arouter.facade.**{*;}
-keep class * implements com.alibaba.android.arouter.facade.template.ISyringe{*;}

# If you use the byType method to obtain Service, add the following rules to protect the interface:
-keep interface * implements com.alibaba.android.arouter.facade.template.IProvider

# If single-type injection is used, that is, no interface is defined to implement IProvider, the following rules need to be added to protect the implementation
 -keep class * implements com.alibaba.android.arouter.facade.template.IProvider
#oaid 1.0.25版本(1.0.26和以后版本需要申请移动安全联盟的证书认证,之前不需要)
-keep class XI.CA.XI.**{*;}
-keep class XI.K0.XI.**{*;}
-keep class XI.XI.K0.**{*;}
-keep class XI.xo.XI.XI.**{*;}
-keep class com.asus.msa.SupplementaryDID.**{*;}
-keep class com.asus.msa.sdid.**{*;}
-keep class com.bun.lib.**{*;}
-keep class com.bun.miitmdid.**{*;}
-keep class com.huawei.hms.ads.identifier.**{*;}
-keep class com.samsung.android.deviceidservice.**{*;}
-keep class com.zui.opendeviceidlibrary.**{*;}
-keep class org.json.**{*;}
-keep public class com.netease.nis.sdkwrapper.Utils {public <methods>;}
#lib_permission
-keep class com.hjq.permissions.** {*;}
# -------------------lottie-------------------------
-keep class com.airbnb.lottie.** { *; }
#PictureSelector 3.0
-keep class com.luck.picture.lib.** { *; }
-keep class com.luck.lib.camerax.** { *; }

# 如果引入了Ucrop库请添加混淆
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

#Okio
-dontwarn org.codehaus.mojo.animal_sniffer.*
#greendao 3
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
    public static java.lang.String TABLENAME;
}
-keep class **$Properties
#吐司
-keep class com.hjq.toast.** {*;}
-keep class com.hjq.permissions.** {*;}