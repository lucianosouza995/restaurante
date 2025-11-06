// No arquivo build.gradle.kts (Module: app)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    id("com.google.gms.google-services")
}

// V-- ADICIONE ESTE BLOCO INTEIRO DE VOLTA --V
android {
    namespace = "com.example.encontro06" // Use o seu namespace correto
    compileSdk = 36 // <-- ESTA É A LINHA EXATA QUE O ERRO ESTÁ PEDINDO

    defaultConfig {
        applicationId = "com.example.encontro06" // Use o seu ID correto
        minSdk = 24
        targetSdk = 36 // É bom que seja igual ao compileSdk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    // --- CORREÇÕES DO FIREBASE ---

    // 2. MANTENHA a versão -ktx
    implementation("com.google.firebase:firebase-auth-ktx")

    // 3. (Recomendado) Use a versão -ktx para Analytics também
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Add sincronização com Firestore
    implementation("com.google.firebase:firebase-firestore-ktx")

    // 4. A versão duplicada "firebase-auth" foi removida.
    // 5. A versão "firebase-common-ktx" não é mais necessária com o BoM.

    // --- Room ---
    // (O seu código do Room estava misturando `libs.versions.toml` e versões manuais)
    // (Vamos usar a versão manual que você definiu)
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    // --- Lifecycle ---
    // Esta versão estava errada antes, vamos usar uma correta:
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // --- CameraX ---
    val camerax_version = "1.3.0"
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")

    // --- Glide ---
    implementation("com.github.bumptech.glide:glide:4.12.0")
    kapt("com.github.bumptech.glide:compiler:4.12.0")

    // --- Dependências do Android (Limpas) ---
    // Você estava importando "core-ktx" e "material" duas vezes.
    // Deixei apenas a versão do libs.versions.toml
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.gridlayout)

    // --- Testes ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}