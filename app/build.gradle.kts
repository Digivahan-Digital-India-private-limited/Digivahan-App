plugins {
    alias(libs.plugins.android.application)
    id("com.onesignal.androidsdk.onesignal-gradle-plugin") version "0.14.0"
    id("com.google.gms.google-services")
    alias(libs.plugins.kotlin.android)
//    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.digivahan"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.digivahan"
        minSdk = 26
        targetSdk = 36
        versionCode = 16
        versionName = "1.1.6"
//        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
//            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.runtime)
    implementation(libs.navigation.fragment)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.volley)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

//    implementation(libs.lifecycle.livedata.ktx)
//    implementation(libs.lifecycle.viewmodel.ktx)
//    implementation(libs.navigation.fragment)
//    implementation(libs.navigation.ui)

   /* // ⚡ Firebase Crashlytics & Analytics (runtime)
    dependencies {
        // Import the Firebase BoM
        implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

        // Add Firebase dependencies without versions
        implementation("com.google.firebase:firebase-crashlytics")
        implementation("com.google.firebase:firebase-analytics")
    }*/


    /*implementation(libs.volley)
    implementation(libs.okhttp3)
    implementation(libs.retrofit)
    implementation(libs.retrofitConverter)

    // to load images
    implementation(libs.picasso)
    implementation(libs.flexbox)
    implementation(libs.sdp)
    implementation(libs.ssp)
    implementation(libs.wdullaer)
    implementation(libs.easyupipayment)

    // Razorpay
    implementation(libs.checkout)

    // OneSignal SDK
    implementation(libs.onesignal)

    // Multidex
    implementation(libs.multidex)
    implementation(libs.play.services.location)
    implementation(libs.play.services.auth)

    // Facebook SDK
    implementation(libs.facebook.android.sdk)

    // Lottie
    implementation(libs.lottie)*/

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // SVG support
    implementation("io.coil-kt:coil:2.6.0")
    implementation("io.coil-kt:coil-svg:2.6.0")

    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.4")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.4")

    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    annotationProcessor("com.google.dagger:hilt-compiler:2.51.1")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    implementation("de.hdodenhof:circleimageview:3.1.0")

    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")


    implementation("com.tbuonomo:dotsindicator:4.3")
    implementation("com.google.android.flexbox:flexbox:3.0.0")


    implementation("com.google.android.gms:play-services-vision:20.1.3")

    // Core OneSignal SDK
    implementation("com.onesignal:OneSignal:[5.1.0, 5.1.99]")


    //razorpay
    implementation("com.razorpay:checkout:1.6.29")

    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))

    implementation("com.google.firebase:firebase-analytics")

//    implementation("com.github.ZEGOCLOUD:zego_uikit_prebuilt_call_android:3.9.10")
//    implementation("com.github.ZEGOCLOUD:zego_uikit_signaling_plugin_android:3.0.2")

//    implementation("im.zego:express-audio:3.23.0")

    /*implementation("com.github.ZEGOCLOUD:zego_uikit_prebuilt_call_android:3.9.11")
    implementation("com.github.ZEGOCLOUD:zego_uikit_signaling_plugin_android:3.0.2")

    implementation("im.zego:zpns-fcm:2.8.0")
    implementation("com.google.firebase:firebase-messaging:23.2.1")

    implementation("com.tencent:mmkv:1.3.14")~
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("androidx.activity:activity:1.8.1")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.22")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.22")*/

//    implementation("com.github.ZEGOCLOUD:zego_uikit_prebuilt_call_android:+")

    implementation("com.facebook.shimmer:shimmer:0.5.0")


    implementation("com.github.yalantis:ucrop:2.2.10")



//    implementation("com.github.SamuelGjk:RoundCornerProgressBar:1.0")


    implementation("com.github.Ashu-Hasan:AshOtp:1.0.8")
    implementation("com.github.Ashu-Hasan:Ashu_Image_Picker:v1.0.5")
    implementation("com.github.Ashu-Hasan:HelperUtils:1.0.2")
//    implementation("com.github.Ashu-Hasan:AshuXKit:1.2.3")
    implementation("com.github.Ashu-Hasan:QRCodeScanner:1.0.3")

    // to show current location on map.
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.29")

    implementation("io.socket:socket.io-client:2.1.0") {
        exclude(group = "org.json", module = "json")
    }

}
