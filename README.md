# openpgp-ktx ![Release nightly snapshots](https://github.com/android-password-store/openpgp-ktx/workflows/Release%20nightly%20snapshots/badge.svg) [![](https://jitpack.io/v/android-password-store/openpgp-ktx.svg)](https://jitpack.io/#android-password-store/openpgp-ktx)

Reimplementation of [OpenKeychain]'s integration library [openpgp-api]. Written entirely in Kotlin, it leverages Jetpack to be compatible with modern apps, unlike the original library.

## Using this with your projects

1. Add the JitPack repository to your root build.gradle

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

2. Add the dependency

```gradle
dependencies {
    implementation 'com.github.android-password-store:openpgp-ktx:<latest-version>'
}
```

A [sample](sample/) is provided in the repository for some basic utilities. Please refer to the migration PR for [Android Password Store](https://github.com/android-password-store/Android-Password-Store/pull/565) for an overview of what differences you need to address when migrating from [openpgp-api].

[OpenKeychain]: https://github.com/open-keychain/open-keychain
[openpgp-api]: https://github.com/open-keychain/openpgp-api
