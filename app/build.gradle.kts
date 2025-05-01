import com.android.build.api.variant.BuildConfigField
import com.android.builder.compiling.BuildConfigType


plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.edu.journal"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.edu.journal"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

//        resValue("string","VERSION_NAME",versionName.toString())
//        resValue("string", "VERSION_CODE", versionCode.toString())
//        buildConfigField("boolean","IS_DEBUG","Debug")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
//        debug {
////            buildConfigField("boolean","IS_DEBUG","true")
//        }
        release {
//            buildConfigField("boolean","IS_DEBUG","false")
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
    sourceSets {
        getByName("main") {
            jniLibs.setSrcDirs(listOf("libs"))
        }
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
//    implementation ("com.squareup.okhttp3:okhttp:3.14.9")
    implementation(libs.okhttp)

    implementation(files("libs/AMap3DMap_10.1.201_AMapSearch_9.7.4_AMapLocation_6.4.9_20250317.jar"))
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.easypermissions)

    //Room 数据库
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.room.compiler)
    // Room 支持 RxJava2（现在已经被包含在 room-runtime 中）
    implementation(libs.room.rxjava2)
    // 腾讯 MMKV
    implementation(libs.mmkv)
    // Gson
    implementation(libs.gson)
    implementation(libs.androidx.swiperefreshlayout)

    implementation(project(":mylibrary"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

//dependencies {
//    // Navigation
//    implementation("androidx.navigation:navigation-common:2.7.0")
//    implementation("androidx.navigation:navigation-runtime:2.7.0")
//    implementation("androidx.navigation:navigation-fragment:2.7.0")
//    implementation("androidx.navigation:navigation-ui:2.7.0")
//
//    implementation(libs.material)
//
//    // ConstraintLayout
//    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
//
//    // AppCompat
//    implementation("androidx.appcompat:appcompat-resources:1.6.1")
//    implementation("androidx.appcompat:appcompat:1.6.1")
//
//    // Activity (兼容 API 33)
//    implementation("androidx.activity:activity-ktx:1.9.0")
//    implementation("androidx.activity:activity:1.9.0")
//
//    // Transition (兼容 API 33)
//    implementation("androidx.transition:transition:1.4.0")
//
//    // Lifecycle (兼容 API 33)
//    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
//    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
//    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
//    implementation("androidx.lifecycle:lifecycle-runtime-android:2.6.1")
//    implementation("androidx.lifecycle:lifecycle-livedata-core-ktx:2.6.1")
//    implementation("androidx.lifecycle:lifecycle-viewmodel-android:2.6.1")
//    implementation("androidx.lifecycle:lifecycle-livedata-core:2.6.1")
//    implementation("androidx.lifecycle:lifecycle-process:2.6.1")
//    // Lifecycle ViewModel SavedState (兼容 API 33)
//    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.7.1")
//
//    // Core KTX (兼容 API 33)
//    implementation("androidx.core:core-ktx:1.12.0")
//
//    // Core (兼容 API 33)
//    implementation("androidx.core:core:1.12.0")
//
//    // ProfileInstaller (兼容 API 33)
//    implementation("androidx.profileinstaller:profileinstaller:1.3.0")
//
//    // Core ViewTree (兼容 API 33)
//    implementation("androidx.core:core-viewtree:0.2.0")
//
//    // Annotation Experimental (兼容 API 33)
//    implementation("androidx.annotation:annotation-experimental:1.3.0")
//
//    implementation(files("libs/AMap3DMap_10.1.201_AMapSearch_9.7.4_AMapLocation_6.4.9_20250317.jar"))
//
//    implementation(project(":mylibrary"))
//
//    implementation(libs.easypermissions)
//    //Room 数据库
//    implementation(libs.androidx.room.runtime)
//    annotationProcessor(libs.room.compiler)
//    // Room 支持 RxJava2（现在已经被包含在 room-runtime 中）
//    implementation(libs.room.rxjava2)
//    // 腾讯 MMKV
//    implementation(libs.mmkv)
//    // Gson
//    implementation(libs.gson)
//    implementation(libs.androidx.swiperefreshlayout)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.ext.junit)
//    androidTestImplementation(libs.espresso.core)
//}