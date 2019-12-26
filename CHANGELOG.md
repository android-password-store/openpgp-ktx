# Changelog

### [Unreleased]

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

[Unreleased]: https://github.com/android-password-store/openpgp-ktx/compare/1.1.0...HEAD
[1.1.0]: https://github.com/android-password-store/openpgp-ktx/releases/1.1.0
[1.0.0]: https://github.com/android-password-store/openpgp-ktx/releases/1.0.0
[0.1.0]: https://github.com/android-password-store/openpgp-ktx/releases/0.1.0
