import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.roborazzi)
}

val signingPropsFile = file("${System.getProperty("user.home")}/.config/pet-rap-sheet/signing.properties")
val signingProps = Properties().apply {
    if (signingPropsFile.exists()) {
        signingPropsFile.reader().use { load(it) }
    }
}

fun findSigningValue(propKeys: List<String>, envKeys: List<String>): String? {
    for (key in propKeys) {
        val v = signingProps.getProperty(key)?.takeIf { it.isNotBlank() }
        if (v != null) return v
    }
    for (key in envKeys) {
        val v = System.getenv(key)?.takeIf { it.isNotBlank() }
        if (v != null) return v
    }
    return null
}

val releaseStorePath = findSigningValue(
    listOf("storeFile", "keystore.path"),
    listOf("RELEASE_STORE_FILE", "KEYSTORE_PATH", "PET_RAP_SHEET_KEYSTORE_PATH"),
)
val releaseStorePass = findSigningValue(
    listOf("storePassword", "keystore.password"),
    listOf("RELEASE_STORE_PASSWORD", "KEYSTORE_PASSWORD", "PET_RAP_SHEET_KEYSTORE_PASSWORD"),
)
val releaseKeyAlias = findSigningValue(
    listOf("keyAlias", "key.alias"),
    listOf("RELEASE_KEY_ALIAS", "KEY_ALIAS", "PET_RAP_SHEET_KEY_ALIAS"),
)
val releaseKeyPass = findSigningValue(
    listOf("keyPassword", "key.password"),
    listOf("RELEASE_KEY_PASSWORD", "KEY_PASSWORD", "PET_RAP_SHEET_KEY_PASSWORD"),
)

val releaseStoreFile = releaseStorePath?.let { file(it) }
val hasReleaseSigning = releaseStoreFile != null &&
    releaseStoreFile.exists() &&
    releaseStorePass != null &&
    releaseKeyAlias != null &&
    releaseKeyPass != null

android {
    namespace = "app.petrapsheet"
    compileSdk = 37

    defaultConfig {
        applicationId = "app.petrapsheet"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = releaseStoreFile!!
                storePassword = releaseStorePass
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPass
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                // Robolectric on SDK 36+ reaches into FileDescriptor internals.
                it.jvmArgs(
                    "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED",
                    "--add-exports=java.base/jdk.internal.access=ALL-UNNAMED",
                    "--add-opens=java.base/java.io=ALL-UNNAMED",
                )
            }
        }
    }

    lint {
        abortOnError = true
        checkDependencies = true
    }
}

roborazzi {
    // Reference screenshots are committed; `verifyRoborazziDebug` compares against them.
    outputDir.set(file("src/test/screenshots"))
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    // Compose ui-test pulls Espresso 3.5.x, which breaks on API 37 (InputManager.getInstance).
    testImplementation(libs.androidx.test.espresso.core)
    testImplementation(libs.robolectric)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)
}
