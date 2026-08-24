plugins {
    //trick: for the same plugin versions in all sub-modules
    id("com.android.application").version("7.4.2").apply(false)
    id("com.android.library").version("7.4.2").apply(false)
    kotlin("android").version("2.1.21").apply(false)
    kotlin("multiplatform").version("2.1.21").apply(false)
    kotlin("plugin.serialization").version("2.1.21").apply(false)
    id("com.google.devtools.ksp").version("2.1.21-2.0.1").apply(false)

}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
        }
    }
    dependencies {
        classpath(BuildPlugin.kuikly)
        // AGP 7.4.2 自带的 R8/D8 太老，无法解析 Kotlin 2.1 metadata，dexing 阶段崩溃
        // （报错形如 com.android.tools.r8.kotlin.H / Error while dexing）。
        // 官方支持表：Kotlin 2.1 需 R8 >= 8.6.17（AGP 7.4.2-8.7.2 区间内可覆盖）。
        classpath("com.android.tools:r8:8.6.17")
    }
}

allprojects {
    configurations.matching { it.name.endsWith("Classpath") }.configureEach {
        resolutionStrategy.dependencySubstitution {
            // KBA 的 annotation 存在双坐标（androidx.annotation / com.tencent.kuiklybase），内容完全相同。
            // 双份共存时 Kotlin/Native 可正常编译链接，但 Android dexing 会报 Duplicate class，
            // 因此仅在 JVM/Android 的 *Classpath 配置上统一替换到 androidx 坐标。
            substitute(module("com.tencent.kuiklybase:annotation"))
                .using(module("androidx.annotation:annotation:${Version.getAndroidxAnnotationVersion()}"))
                .because("unify duplicate KBA annotation coordinates for Android dexing")
        }
    }
}