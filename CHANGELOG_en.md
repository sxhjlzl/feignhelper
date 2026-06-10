# Changelog

> **English** | [中文](./CHANGELOG.md)

All notable changes to the FeignHelper plugin are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [1.0.0] - 2026-06-09

### Added

- Bidirectional gutter navigation between `@FeignClient` and `@RestController`
- Bidirectional gutter navigation between `@HttpExchange` and `@RestController` (Spring 6 support)
- Single directional arrow gutter icon: **left-click to jump**, **right-click to copy full URL** with balloon notification
- Multi-file merge parsing for `application.yml` / `bootstrap.yml` / `application-{profile}.yml` / `bootstrap-{profile}.yml`
- `server.servlet.context-path` and `spring.mvc.servlet.path` prefix resolution
- `${var:default}` placeholder resolution with multi-profile precedence
- Java + Kotlin source support via UAST abstraction
- Settings page: manual profile override
- English + Simplified Chinese UI bundle

### Technical

- IntelliJ Platform Gradle Plugin 2.12.0
- Kotlin 2.3.20 + JDK 21 (coroutine optimizations requiring a newer runtime are disabled for IDEA 2024.3 compatibility)
- Gradle 9.5.0
- Compiled against the minimum supported IDEA 2024.3, with Plugin Verifier used to validate later releases
- Caches use `@Service(Service.Level.PROJECT)` with automatic lifecycle management
- Asynchronous cache warm-up via `ProjectActivity` coroutine, never blocking project open
- PSI listeners cover add, remove, replace, move, and property events; Spring configuration and profile changes refresh caches
- Declares compatibility with Kotlin K1 and K2 plugin modes
- Adds unit tests for paths, placeholders, profiles, and configuration file recognition

[Unreleased]: https://github.com/sxhjlzl/feignhelper/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/sxhjlzl/feignhelper/releases/tag/v1.0.0
