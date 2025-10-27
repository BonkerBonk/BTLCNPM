plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.btlcnpm.androidapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.btlcnpm.androidapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true // Cho phép dùng icon vector (XML)
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures{
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10" // Phiên bản trình biên dịch Compose
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}" // Loại trừ các file license trùng
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    val composeBom = platform("androidx.compose:compose-bom:2024.05.00") // Phiên bản BOM mới nhất (ổn định)
    implementation(composeBom) // Áp dụng BOM
    androidTestImplementation(composeBom) // Cho testing

    // Các thư viện Compose cơ bản (phiên bản được quản lý bởi BOM)
    implementation("androidx.compose.material3:material3") // Thành phần Material Design 3
    implementation("androidx.compose.ui:ui")             // Lõi của Compose UI
    implementation("androidx.compose.ui:ui-tooling-preview") // Hỗ trợ xem trước (@Preview)
    debugImplementation("androidx.compose.ui:ui-tooling")    // Công cụ debug cho Compose

    // Activity Compose (BẮT BUỘC để dùng setContent trong Activity)
    implementation("androidx.activity:activity-compose:1.9.0") // Phiên bản ổn định

    // Navigation Compose (Để di chuyển giữa các màn hình Compose)
    implementation("androidx.navigation:navigation-compose:2.7.7") // Phiên bản ổn định

    // ViewModel Compose (Để kết nối ViewModel với Composable)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0") // Phiên bản ổn định

    // Retrofit (Để gọi API) & Gson (Để xử lý JSON)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")        // Phiên bản Retrofit 2 ổn định
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Bộ chuyển đổi Gson

    // Kotlin Coroutines (Để xử lý bất đồng bộ khi gọi API)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3") // Phiên bản ổn định
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0") // Cung cấp CoroutineScope cho Lifecycle

    // Coil (Để tải ảnh từ URL trong Compose)
    implementation("io.coil-kt:coil-compose:2.6.0") // Phiên bản ổn định mới nhất

    // Test (Các thư viện testing cơ bản)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4") // Testing cho Compose
    debugImplementation("androidx.compose.ui:ui-test-manifest")    // Hỗ trợ testing
}