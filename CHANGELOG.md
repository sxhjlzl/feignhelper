# 更新日志

> [English](./CHANGELOG_en.md) | **中文**

本文件记录 FeignHelper 插件的版本变更，格式参考 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，版本号遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

## [Unreleased]

## [1.0.2] - 2026-06-12

### Changed 改进

- gutter 图标仅在存在对端匹配时显示：`@FeignClient` / `@HttpExchange` 方法只有找到对应 `@RestController` 方法时才出现图标，反之亦然
- 对端存在性检查基于缓存 + 当前方法即时解析，避免 LineMarker 渲染期间触发全工程扫描导致 EDT 卡顿
- 工程打开异步预热完成后通知 `DaemonCodeAnalyzer` 重新渲染，使匹配图标在缓存就绪后立即出现

## [1.0.1] - 2026-06-10

### Fixed 修复

- 修复 `LineMarkerInfo` 强持有 `PsiMethod` 导致的 PSI 失效引用与潜在内存泄漏，改为在点击时基于最新锚点重新解析方法
- 修复点击 gutter 图标可能在 EDT 上触发全工程扫描而卡顿的问题，兜底扫描现统一在可取消的后台进度中执行
- 修复 `BilateralMappingCacheService.removeByMethod` / `resolveMapping` 对已失效 `PsiMethod` 调用 `qualifierOf` 时抛出 `PsiInvalidElementAccessException` 的问题
- 修复 `computeFreshClientMapping` 拆分为两段 read action 带来的 PSI 读取不一致风险，合并为单段原子操作

### Changed 改进

- 行内图标即使缓存未命中也常显，确保编辑后立刻可点击触发兜底扫描
- 未找到匹配对端接口时弹出气泡通知，避免点击图标后无任何反馈

## [1.0.0] - 2026-06-09

### Added 新增

- `@FeignClient` 接口 ↔ `@RestController` 双向行内导航 gutter
- `@HttpExchange` 接口 ↔ `@RestController` 双向行内导航 gutter（Spring 6 新特性支持）
- gutter 行内方向箭头图标：**左键跳转到对端方法**，**右键复制完整 URL** 到剪贴板，含气泡通知
- `application.yml` / `bootstrap.yml` / `application-{profile}.yml` / `bootstrap-{profile}.yml` 多文件合并解析
- `server.servlet.context-path` 与 `spring.mvc.servlet.path` 路径前缀解析
- `${var:default}` 占位符与多 profile 优先级处理
- Java + Kotlin 双语言支持（基于 UAST 抽象）
- 设置页：手动 profile 覆盖
- 中文 + 英文双语界面

### Technical 技术细节

- IntelliJ Platform Gradle Plugin 2.12.0
- Kotlin 2.3.20 + JDK 21（关闭依赖新 runtime 的协程优化，以兼容 IDEA 2024.3）
- Gradle 9.5.0
- 以最低支持版本 IDEA 2024.3 编译，并使用 Plugin Verifier 校验后续版本
- 缓存采用 `@Service(Service.Level.PROJECT)`，由 IDEA 自动管理生命周期
- 异步预热通过 `ProjectActivity` 协程，避免阻塞工程打开
- PSI 监听覆盖增删改移动事件，Spring 配置或 profile 改动后自动刷新缓存
- 声明 Kotlin K1 / K2 插件模式兼容
- 为路径、占位符、profile 与配置文件识别补充单元测试

[Unreleased]: https://github.com/sxhjlzl/feignhelper/compare/v1.0.2...HEAD
[1.0.2]: https://github.com/sxhjlzl/feignhelper/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/sxhjlzl/feignhelper/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/sxhjlzl/feignhelper/releases/tag/v1.0.0
