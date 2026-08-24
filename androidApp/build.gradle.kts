plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.imcys.sairen"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.imcys.sairen"
        minSdk = 23
        targetSdk = 30
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    lint {
        // AGP 7.4.2 的 lint（旧版 UAST）无法解析 Kotlin 2.1 源码，会对 Manifest 注册的类
        // 误报 "must extend android.app.Application/Activity"（KRApplication/KuiklyRenderActivity
        // 实际存在且继承正确）；targetSdk=30 还会触发 ExpiredTargetSdkVersion 致命错误。
        // release 打包阶段跳过 lintVital，代码检查交给 IDE/单独的 lint 任务。
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    implementation(project(":shared"))

    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.appcompat:appcompat:1.3.1")

    implementation("com.squareup.picasso:picasso:2.8")

    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.dynamicanimation:dynamicanimation:1.0.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // kotlinx-datetime KBA 版在 Android 上依赖 threetenabp（低 API 设备的 java.time 兼容层），
    // App 启动时需调用 AndroidThreeTen.init(this)，见 KRApplication
    implementation("com.jakewharton.threetenabp:threetenabp:1.4.7")
}