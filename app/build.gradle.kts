import java.util.Properties

// As chaves ficam em local.properties, que está fora do git.
val locais = Properties().apply {
    val arquivo = rootProject.file("local.properties")
    if (arquivo.exists()) arquivo.inputStream().use { load(it) }
}

// Senha e caminho do keystore ficam fora do git. Sem esse arquivo o release não é assinado.
val assinatura = Properties().apply {
    val arquivo = rootProject.file("keystore.properties")
    if (arquivo.exists()) arquivo.inputStream().use { load(it) }
}
val arquivoDaLoja = assinatura.getProperty("storeFile")?.let { rootProject.file(it) }
val temAssinatura = arquivoDaLoja?.isFile == true &&
    listOf("storePassword", "keyAlias", "keyPassword").all { !assinatura.getProperty(it).isNullOrBlank() }

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.culturama"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "br.com.culturama.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0-beta"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "CULTURAMA_APP_CHAVE", "\"${locais.getProperty("CULTURAMA_APP_CHAVE", "")}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"145186922170-kf7vfa8oqoopo93h5tpp0vlbqbeibg1v.apps.googleusercontent.com\"")
        buildConfigField("String", "SUPABASE_URL", "\"https://qvwejrfmjvsuwfrqvgdb.supabase.co\"")
        buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"${locais.getProperty("SUPABASE_PUBLISHABLE_KEY", "")}\"")
    }

    signingConfigs {
        if (temAssinatura) {
            create("release") {
                storeFile = arquivoDaLoja
                storePassword = assinatura.getProperty("storePassword")
                keyAlias = assinatura.getProperty("keyAlias")
                keyPassword = assinatura.getProperty("keyPassword")
            }
        }
    }
    buildTypes {
        release {
            if (temAssinatura) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.coil.compose)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.play.services.location)
    implementation(libs.osmdroid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}