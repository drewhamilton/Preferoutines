# Preferoutines
[![](https://github.com/drewhamilton/Preferoutines/workflows/CI/badge.svg?branch=master)](https://github.com/drewhamilton/Preferoutines/actions?query=workflow%3ACI+branch%3Amaster)

A collection of extension methods for accessing Android SharedPreferences in Kotlin coroutines.

Preferoutines is in pre-release. The API may undergo breaking changes before version 1.0.0 is released.

## Download
[ ![Download](https://api.bintray.com/packages/drewhamilton/Preferoutines/Preferoutines/images/download.svg) ](https://bintray.com/drewhamilton/Preferoutines)

Preferoutines is available in JCenter.

```groovy
// Preferoutines:
implementation "drewhamilton.preferoutines:preferoutines:$version"

// With extra extensions, e.g. for saving enum preferences:
implementation "drewhamilton.preferoutines:preferoutines-extras:$version"
```

## Usage

### Preferoutines
Listen to any preference for as long as you want:
```kotlin
exampleScope.launch {
    preferences.getStringFlow("Name", "<None>")
        .collect { name -> nameView.text = name }
}
```

### Extras
Smoothly handle some common cases with extra extensions for enums and non-nullable String and enum values.
```kotlin
exampleScope.launch {
    preferences.getNonNullEnumFlow("Theme", Theme.LIGHT)
        .collect { theme -> setTheme(theme.resource) }
}
```

## License
```
Copyright 2019 Drew Hamilton

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
