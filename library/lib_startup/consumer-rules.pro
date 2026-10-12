# ServiceLoader 按 META-INF/services 里的类名反射创建，实现类和无参构造不能被删或改名
-keep class * implements com.lib.startup.ModuleInitializer {
    <init>();
}
