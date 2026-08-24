object Version {

    private const val KUIKLY_VERSION = "2.7.0"
    private const val KOTLIN_VERSION = "2.1.21"
    private const val KOTLIN_OHOS_VERSION = "2.0.21-ohos"
    private const val KOTLINX_SERIALIZATION_VERSION = "1.7.1-KBA-003"
    private const val KOTLINX_COROUTINES_VERSION = "1.8.0-KBA-001"
    private const val KOTLINX_DATETIME_VERSION = "0.6.0-RC.2-KBA-003"
    private const val ANDROIDX_COLLECTION_VERSION = "1.4.0-KBA-002"
    private const val ANDROIDX_ANNOTATION_VERSION = "1.8.0-KBA-001"
    private const val ANDROIDX_LIFECYCLE_VERSION = "2.8.0-KBA-011"

    /**
     * 获取 Kuikly 版本号，版本号规则：${shortVersion}-${kotlinVersion}
     * 适用于 core、core-ksp、core-annotation、core-render-android
     */
    fun getKuiklyVersion(): String {
        return "$KUIKLY_VERSION-$KOTLIN_VERSION"
    }

    /**
     * 获取 Kuikly Ohos版本号
     */
    fun getKuiklyOhosVersion(): String {
        return "$KUIKLY_VERSION-$KOTLIN_OHOS_VERSION"
    }

    /**
     * 获取 kotlinx.serialization 版本号（KuiklyBase 平台补丁版，支持 OpenHarmony）
     */
    fun getKotlinxSerializationVersion(): String {
        return KOTLINX_SERIALIZATION_VERSION
    }

    /**
     * 获取 kotlinx.coroutines 版本号（KuiklyBase 平台补丁版，支持 OpenHarmony）
     */
    fun getKotlinxCoroutinesVersion(): String {
        return KOTLINX_COROUTINES_VERSION
    }

    /**
     * 获取 kotlinx.datetime 版本号（KuiklyBase 平台补丁版，支持 OpenHarmony）
     * 已确认 js/jvm/ios/ohosArm64 变体均已发布，可放 commonMain
     */
    fun getKotlinxDatetimeVersion(): String {
        return KOTLINX_DATETIME_VERSION
    }

    /**
     * 获取 androidx.collection 版本号（KuiklyBase 平台补丁版，支持 OpenHarmony）
     */
    fun getAndroidxCollectionVersion(): String {
        return ANDROIDX_COLLECTION_VERSION
    }

    /**
     * 获取 androidx.annotation 版本号（KuiklyBase 平台补丁版，支持 OpenHarmony）
     * 注意：README 里的 1.8.0-KBA-002 未发布，实际可用的是 androidx.annotation:annotation:1.8.0-KBA-001
     * （collection KBA-002 传递依赖的是同内容新坐标 com.tencent.kuiklybase:annotation:1.8.0-KBA-001）
     */
    fun getAndroidxAnnotationVersion(): String {
        return ANDROIDX_ANNOTATION_VERSION
    }

    /**
     * 获取 androidx.lifecycle 版本号（KuiklyBase 平台补丁版，支持 OpenHarmony）
     */
    fun getAndroidxLifecycleVersion(): String {
        return ANDROIDX_LIFECYCLE_VERSION
    }
}

object BuildPlugin {
    val kuikly by lazy {
        "com.tencent.kuikly-open:core-gradle-plugin:${Version.getKuiklyVersion()}"
    }
}