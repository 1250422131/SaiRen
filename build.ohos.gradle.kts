buildscript {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
        }
    }
    dependencies {
        // AGP 7.4.2 自带的 R8/D8 太老，无法解析 Kotlin 2.0/2.1 metadata，dexing 阶段崩溃
        // （报错形如 com.android.tools.r8.kotlin.H / Error while dexing）。
        // 官方支持表：Kotlin 2.1 需 R8 >= 8.6.17，此处统一覆盖（与 build.gradle.kts 保持一致）。
        classpath("com.android.tools:r8:8.6.17")
    }
}

plugins {
    //trick: for the same plugin versions in all sub-modules
    id("com.android.application").version("7.4.2").apply(false)
    id("com.android.library").version("7.4.2").apply(false)
    kotlin("android").version("2.0.21-KBA-010").apply(false)
    kotlin("multiplatform").version("2.0.21-KBA-010").apply(false)
    kotlin("plugin.serialization").version("2.0.21-KBA-010").apply(false)
    id("com.google.devtools.ksp").version("2.0.21-1.0.27").apply(false)
    id("com.tencent.kuiklybase.knoi.plugin").version("0.0.4").apply(false)

}

// 注意：KBA 的 annotation 存在双坐标（androidx.annotation / com.tencent.kuiklybase），klib 内容完全相同。
// collection 的 klib 元数据（dependsOn）要求 com.tencent.kuiklybase:annotation，而 lifecycle 的 klib 元数据
// 要求 androidx.annotation:annotation。KLIB resolver 按 klib 内的 unique_name 匹配依赖，任何方向的
// dependencySubstitution 替换都会让其中一方找不到 klib 而编译失败（已实测双向均失败）。
// 因此不做替换，让两个坐标的 klib 同时上 classpath，实测可正常编译。
