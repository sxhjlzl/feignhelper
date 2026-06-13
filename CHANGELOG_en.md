# Changelog

> **English** | [中文](./CHANGELOG.md)

All notable changes to the FeignHelper plugin are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [1.0.4] - 2026-06-13

### Fixed

- Replaced strong `PsiMethod` references in the endpoint mapping cache with `SmartPsiElementPointer`, preventing stale PSI retention after source edits or deletion
- Preserved `ProcessCanceledException` in configuration parsing, indexed scans, and tool-window refreshes so IDE cancellation propagates correctly
- Corrected duplicate light-service registrations, the PSI listener extension point name, and localization metadata for the settings page in `plugin.xml`
- Disabled Kotlin compatibility bridges that generated calls to deprecated and experimental `ToolWindowFactory` APIs on IntelliJ IDEA 2026.2 EAP

### Changed

- Delayed refresh tasks now use the project service's injected `CoroutineScope` and are cancelled with the project lifecycle
- The message-bus connection is explicitly bound to the project lifecycle
- Verified compatibility with IntelliJ IDEA 2024.3, 2025.1, 2025.2, 2025.3, 2026.1, and 2026.2 EAP

## [1.0.3] - 2026-06-12

### Added

- New **FeignHelper tool window** docked on the right side of the IDE
- Two tabs in the tool window:
  - **Controller Endpoints**: lists all `@RestController` / `@Controller` endpoints
  - **Feign / HttpExchange Endpoints**: lists all `@FeignClient` / `@HttpExchange` endpoints
- Endpoints are grouped by fully-qualified class name and sorted by URL + HTTP method
- Filter box for URL, HTTP method, or class name
- Double-click or Enter to jump to source; right-click to copy URL or navigate to the counterpart
- List refreshes automatically on PSI changes, Spring configuration changes, and cache warm-up

## [1.0.2] - 2026-06-12

### Changed

- Gutter icons are now rendered only when a counterpart exists: a `@FeignClient` / `@HttpExchange` method shows the icon only when a matching `@RestController` method is found, and vice versa
- The counterpart existence check uses the cache plus on-demand resolution of the current method, avoiding project-wide scans during LineMarker rendering and keeping the EDT responsive
- After asynchronous cache warm-up on project open, the daemon is notified so matched icons appear immediately

## [1.0.1] - 2026-06-10

### Fixed

- Fixed a PSI leak / stale-element issue caused by `LineMarkerInfo` strongly holding `PsiMethod`; the source method is now re-resolved from the latest anchor on click
- Fixed UI freezes caused by running the fallback project-wide scan on the EDT; the lookup now runs in a cancelable background task
- Fixed `PsiInvalidElementAccessException` when `BilateralMappingCacheService.removeByMethod` / `resolveMapping` was called with an invalid `PsiMethod`
- Fixed a non-atomic PSI read in `computeFreshClientMapping` by merging the two read actions into a single atomic block

### Changed

- Gutter icons are now always rendered when a method is applicable, so a freshly edited method can immediately trigger the fallback scan on click
- Added a balloon notification when no matching endpoint is found, so clicks no longer fail silently

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

[Unreleased]: https://github.com/sxhjlzl/feignhelper/compare/v1.0.4...HEAD
[1.0.4]: https://github.com/sxhjlzl/feignhelper/compare/v1.0.3...v1.0.4
[1.0.3]: https://github.com/sxhjlzl/feignhelper/compare/v1.0.2...v1.0.3
[1.0.2]: https://github.com/sxhjlzl/feignhelper/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/sxhjlzl/feignhelper/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/sxhjlzl/feignhelper/releases/tag/v1.0.0
