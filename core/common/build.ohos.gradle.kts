plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("maven-publish")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
        publishLibraryVariants("release")
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // 库模块只产出 ohosArm64 klib，由宿主 shared 的 libshared.so（binaries.sharedLib）统一链接；
    // ohosApp/entry 的 CMake 只链接 libshared.so，core 模块不需要单独产 .so。
    ohosArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("com.tencent.kuikly-open:core:${Version.getKuiklyOhosVersion()}")
                api("com.tencent.kuikly-open:core-annotations:${Version.getKuiklyOhosVersion()}")
                // kotlinx.coroutines（KuiklyBase 平台补丁版，来自 maven-tencent 仓库）
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.getKotlinxCoroutinesVersion()}")
                // kotlinx.datetime（KuiklyBase 平台补丁版，来自 maven-tencent 仓库）
                api("org.jetbrains.kotlinx:kotlinx-datetime:${Version.getKotlinxDatetimeVersion()}")
            }
        }
        // 中间源集：与常规构建保持同构（android/ios/ohosArm64 共享）。
        // KBA 版 androidx.collection 未发布 js 变体，不能进 commonMain，否则 js 编译失败。
        // 依赖 collection 的公共代码请放在 src/nonJsMain/kotlin。
        val nonJsMain by creating {
            dependsOn(commonMain)
            dependencies {
                // androidx.annotation（KuiklyBase 平台补丁版，来自 maven-tencent 仓库）
                api("androidx.annotation:annotation:${Version.getAndroidxAnnotationVersion()}")
                // androidx.collection（KuiklyBase 平台补丁版，来自 maven-tencent 仓库）
                api("androidx.collection:collection:${Version.getAndroidxCollectionVersion()}")
                // androidx.lifecycle（KuiklyBase 平台补丁版，来自 maven-tencent 仓库）
                api("androidx.lifecycle:lifecycle-common:${Version.getAndroidxLifecycleVersion()}")
                api("androidx.lifecycle:lifecycle-runtime:${Version.getAndroidxLifecycleVersion()}")
                api("androidx.lifecycle:lifecycle-viewmodel:${Version.getAndroidxLifecycleVersion()}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        // 与 shared 同构保留 androidMain 源集声明（挂在 nonJsMain 之下）；当前无 Android 独立实现。
        // core-render-android（渲染引擎，ohos 版本号）由宿主 shared 的 androidMain 对外提供，core 不需要。
        val androidMain by getting {
            dependsOn(nonJsMain)
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(nonJsMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
        val ohosArm64Main by getting {
            dependsOn(nonJsMain)
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

group = "com.imcys.sairen"
version = System.getenv("kuiklyBizVersion") ?: "1.0.0"

publishing {
    repositories {
        maven {
            credentials {
                username = System.getenv("mavenUserName") ?: ""
                password = System.getenv("mavenPassword") ?: ""
            }
            rootProject.properties["mavenUr?"]?.toString()?.let { url = uri(it) }
        }
    }
}

android {
    namespace = "com.imcys.sairen.core.common"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
        targetSdk = 30
    }
}
