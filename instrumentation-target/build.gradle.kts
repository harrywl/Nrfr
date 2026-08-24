// Fork 变更说明：本文件为 Ackites/Nrfr fork 新增，定义 Android 16 instrumentation helper APK。
import java.util.Properties

plugins {
    id("com.android.application")
}

val localPropertiesFile = rootProject.file("local.properties")
val releaseSigningProperties = localPropertiesFile
    .takeIf { it.isFile }
    ?.let { file ->
        Properties().apply {
            file.inputStream().use { input -> load(input) }
        }
    }
    ?.takeIf { properties ->
        listOf(
            "signing.storeFile",
            "signing.storePassword",
            "signing.keyAlias",
            "signing.keyPassword",
        ).all { key -> !properties.getProperty(key).isNullOrBlank() }
    }

android {
    namespace = "com.github.nrfr.instrumentationtarget"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.github.nrfr.instrumentationtarget"
        minSdk = 26
        targetSdk = 34
        versionCode = providers.gradleProperty("nrfr.versionCode").get().toInt()
        versionName = providers.gradleProperty("nrfr.versionName").get()
    }

    signingConfigs {
        releaseSigningProperties?.let { properties ->
            create("release") {
                storeFile = rootProject.file(properties.getProperty("signing.storeFile"))
                storePassword = properties.getProperty("signing.storePassword")
                keyAlias = properties.getProperty("signing.keyAlias")
                keyPassword = properties.getProperty("signing.keyPassword")
            }
        }
    }

    buildTypes {
        release {
            if (releaseSigningProperties != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
}
