<div align="right">
  <a href="./README.md">中文</a> | <strong>English</strong>
</div>

<div align="center">
  <img src="./src/main/resources/icons/jumpAction_feign.svg" height="64">
  <img src="./src/main/resources/icons/jumpAction_controller.svg" height="64">
  <h1>FeignHelper</h1>
  <p>Modern navigation assistant for Spring Cloud OpenFeign, Spring MVC and Spring 6 <code>@HttpExchange</code> in IntelliJ IDEA.</p>

  <p>
    <a href="https://plugins.jetbrains.com/plugin/32207-feignhelper">
      <img alt="JetBrains Plugin Version" src="https://img.shields.io/jetbrains/plugin/v/32207-feignhelper?style=flat&label=Marketplace&color=2DA44E">
    </a>
    <a href="https://plugins.jetbrains.com/plugin/32207-feignhelper">
      <img alt="JetBrains Plugin Downloads" src="https://img.shields.io/jetbrains/plugin/d/32207-feignhelper?style=flat&color=3DA639">
    </a>
    <a href="https://plugins.jetbrains.com/plugin/32207-feignhelper/reviews">
      <img alt="JetBrains Plugin Rating" src="https://img.shields.io/jetbrains/plugin/r/rating/32207-feignhelper?style=flat&color=FFB400">
    </a>
    <a href="https://github.com/sxhjlzl/feignhelper/releases/latest">
      <img alt="GitHub Release" src="https://img.shields.io/github/v/release/sxhjlzl/feignhelper?style=flat&logo=github&color=24292E">
    </a>
    <a href="https://github.com/sxhjlzl/feignhelper/stargazers">
      <img alt="GitHub Stars" src="https://img.shields.io/github/stars/sxhjlzl/feignhelper?style=flat&logo=github&color=181717">
    </a>
    <img alt="IntelliJ Platform" src="https://img.shields.io/badge/IntelliJ%20IDEA-2024.3%2B-000?style=flat&logo=intellijidea">
    <a href="./LICENSE">
      <img alt="License" src="https://img.shields.io/github/license/sxhjlzl/feignhelper?style=flat">
    </a>
  </p>
</div>

## Overview

FeignHelper is an IntelliJ IDEA plugin that brings the following capabilities to Spring developers using OpenFeign, Spring MVC and Spring 6 `@HttpExchange`:

- Bidirectional gutter navigation between `@FeignClient` interfaces and `@RestController` methods
- Bidirectional gutter navigation between `@HttpExchange` interfaces and `@RestController` methods (Spring 6)
- A single gutter arrow per method: **left-click to jump**, **right-click to copy the full endpoint path**
- Automatic resolution of `application.yml` / `bootstrap.yml` / `application-{profile}.yml`, including `server.servlet.context-path`, `spring.mvc.servlet.path` and `${var:default}` placeholders
- Java and Kotlin sources via the UAST language-agnostic abstraction
- Project-level caching with incremental refresh on PSI changes
- English & Simplified Chinese bundle

## Compatibility

| Item | Version |
| --- | --- |
| IntelliJ IDEA | `2024.3+` (build `243+`, subject to Plugin Verifier results) |
| Required IDE plugins | Java (bundled) + Kotlin (bundled) |
| Build toolchain | JDK 21 + Gradle 9 + IntelliJ Platform Plugin 2.12 |

## Installation

### Option 1: JetBrains Marketplace (recommended)

`IDEA` → `Settings` → `Plugins` → `Marketplace` → search **FeignHelper** → `Install`

Or search for **FeignHelper** on [JetBrains Marketplace](https://plugins.jetbrains.com/search?search=FeignHelper).

### Option 2: Local zip

1. Download the latest `feignhelper-<version>.zip` from [GitHub Releases](https://github.com/sxhjlzl/feignhelper/releases/latest)
2. `IDEA` → `Settings` → `Plugins` → gear icon → `Install Plugin from Disk...` → select the zip

## Usage

In any Spring project:

- Open a `@FeignClient` / `@HttpExchange` method — a **right-pointing arrow** ➡ appears in the gutter
  - **Left-click** → jump to the matching `@RestController` method
  - **Right-click** → popup menu to copy the full URL
- Open a `@RestController` method — a **left-pointing arrow** ⬅ appears in the gutter
  - **Left-click** → jump to the matching Feign / HttpExchange method
  - **Right-click** → popup menu to copy the full URL
- When more than one target matches, left-click opens a chooser popup

`Settings` → `Tools` → `FeignHelper`:

- Manually override the active Spring profile (leave empty to auto-detect from configuration files)

## Local build

```bash
./gradlew :compileKotlin       # compile Kotlin sources only
./gradlew :build               # full build
./gradlew :buildPlugin         # produces build/distributions/feignhelper-<version>.zip
./gradlew :runIde              # launch a sandbox IDE for debugging
./gradlew :verifyPlugin        # validate compatibility against target SDKs
```

Requirements: JDK 21 (Temurin 21.0.11 recommended) + Gradle 9 (wrapper included).

## Project structure

```
feignhelper-plugin/
├── build.gradle.kts                # Gradle 9 + IntelliJ Platform Plugin 2.12
├── gradle.properties               # platform version & plugin metadata
├── docs/
│   └── publish.md                  # how to publish to Marketplace
└── src/main/
    ├── kotlin/com/lizhuolun/feignhelper/
    │   ├── FeignHelperBundle.kt    # i18n entry point (DynamicBundle)
    │   ├── core/                   # data model & algorithms (pure PSI, no UI)
    │   ├── config/                 # yml / properties parsing (placeholders + profiles)
    │   ├── scanner/                # AnnotatedElementsSearch-powered scanner
    │   ├── cache/                  # PROJECT Service caches (bilateral mapping + PsiClass)
    │   ├── listener/               # ProjectActivity warmer + PsiTreeChangeAdapter
    │   ├── provider/               # LineMarker (left-click jump + right-click copy URL)
    │   └── settings/               # configurable + PersistentStateComponent
    └── resources/
        ├── META-INF/plugin.xml     # modern extension points
        ├── icons/                  # directional arrow icons, including _dark variants
        └── messages/               # en + zh_CN bundles
```

## Contributing

- [CHANGELOG_en.md](./CHANGELOG_en.md) — release notes
- [CONTRIBUTING_en.md](./CONTRIBUTING_en.md) — contributor guide
- [SECURITY.md](./SECURITY.md) — security policy
- [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md) — Contributor Covenant
- [PRIVACY.md](./PRIVACY.md) — privacy policy
- [THIRD_PARTY_NOTICES.md](./THIRD_PARTY_NOTICES.md) — bundled dependency notices

Issues requests are welcome.

## License

[Apache License 2.0](./LICENSE) © lizhuolun
