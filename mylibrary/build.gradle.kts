plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.edu.mylibrary"
    compileSdk = 35

    defaultConfig {
        minSdk = 30

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.okhttp)
    // Retrofit 2 (保持 2.9.0，兼容 OKHttp 4.x)
    api(libs.retrofit)

    // Gson 转换器 (与 Retrofit 2.9.0 兼容)
    implementation(libs.converter.gson)

    // 日志拦截器 (OKHttp 4.12.0 版本兼容)
    implementation(libs.logging.interceptor)

    // Retrofit 和 RxJava 适配器
    implementation(libs.adapter.rxjava2)

    implementation("androidx.databinding:viewbinding:7.4.0")


    // RxJava
    api(libs.rxandroid)
    implementation(libs.rxjava)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

//dependencies {
//
////    implementation(libs.appcompat)
//    // AppCompat
//    implementation("androidx.appcompat:appcompat-resources:1.6.1")
//    implementation("androidx.appcompat:appcompat:1.6.1")
//    implementation(libs.material)
//    implementation(libs.okhttp)
//    // Retrofit 2 (保持 2.9.0，兼容 OKHttp 4.x)
//    api(libs.retrofit)
//
//    // Gson 转换器 (与 Retrofit 2.9.0 兼容)
//    implementation(libs.converter.gson)
//
//    // 日志拦截器 (OKHttp 4.12.0 版本兼容)
//    implementation(libs.logging.interceptor)
//
//    // Retrofit 和 RxJava 适配器
//    implementation(libs.adapter.rxjava2)
//
//    implementation("androidx.databinding:viewbinding:7.4.0")
//
//
//    // RxJava
//    api(libs.rxandroid)
//    implementation(libs.rxjava)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.ext.junit)
//    androidTestImplementation(libs.espresso.core)
//}