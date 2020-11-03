# Changelog

### [Unreleased]

### [2.2.0] - 2020-11-04
- Update dependencies
- Enable tracking of public API changes

### [2.1.0] - 2020-08-16
- Target Android 11
- Update to Kotlin 1.4
- Update library dependencies

### [2.0.0] - 2020-04-29
- **BREAKING**: Remove `IOpenPgpCallback` and replace it with a lambda reference.

### [1.2.0] - 2020-01-25
- Fix unmarshalling of `OpenPgpError`

### [1.1.0] - 2019-12-26
- Update library dependencies
- Embed proguard rules in library

### [1.0.0] - 2019-11-29
- Update library dependencies
- Make logtags unique across classes to aid debugging
- **BREAKING**: Make parameters in OnBound interface non-null
- **BREAKING**: `OpenPgpApi#executeApiAsync` is now a `suspend` function and only works with a [coroutines](https://github.com/kotlin/kotlinx.coroutines) caller
- Don't generate `BuildConfig` in the library

### [0.1.0] - 2019-11-08
- Initial release

[Unreleased]: https://github.com/android-password-store/openpgp-ktx/compare/2.2.0...HEAD
[2.2.0]: https://github.com/android-password-store/openpgp-ktx/releases/2.2.0
[2.1.0]: https://github.com/android-password-store/openpgp-ktx/releases/2.1.0
[2.0.0]: https://github.com/android-password-store/openpgp-ktx/releases/2.0.0
[1.2.0]: https://github.com/android-password-store/openpgp-ktx/releases/1.2.0
[1.1.0]: https://github.com/android-password-store/openpgp-ktx/releases/1.1.0
[1.0.0]: https://github.com/android-password-store/openpgp-ktx/releases/1.0.0
[0.1.0]: https://github.com/android-password-store/openpgp-ktx/releases/0.1.0
