# ViperSDK
Innocomm Viper Sample code

# Project configuration:

## In root build.gradle:

    allprojects {
        repositories {
            ...
            maven {
                url 'https://dl.bintray.com/icommori/vipersdk'
            }
        }
    }
    
## In your app build.gradle:

    implementation 'com.innocomm:vipersdk:1.0.4'
    implementation 'com.squareup.okhttp3:okhttp:4.2.2'

## Usage:

    BCRManager mBCRManager;
    mBCRManager = new BCRManager(CONTEXT);
    mBCRManager.scan(true);

