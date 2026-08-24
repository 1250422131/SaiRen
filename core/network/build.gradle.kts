plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
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

    // 库模块的 js 目标：以 klib 形式被 shared 的 js 产物（nativevue2.js）引用。
    // webpack 打包配置与 binaries.executable() 是宿主专属，core 模块不产出 js bundle。
    js(IR) {
        browser()
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core:common"))
                // kotlinx.serialization（KuiklyBase 平台补丁版，来自 maven-tencent 仓库）
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${Version.getKotlinxSerializationVersion()}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Version.getKotlinxSerializationVersion()}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        // 与 shared 同构保留 androidMain 源集声明；当前无 Android 独立实现。
        // core-render-android（渲染引擎）由宿主 shared 的 androidMain 对外提供，core 不需要。
        val androidMain by getting {
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
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
    namespace = "com.imcys.sairen.core.network"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
        targetSdk = 30
    }
}
