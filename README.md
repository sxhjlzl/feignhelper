<div align="right">
  <strong>中文</strong> | <a href="./README_en.md">English</a>
</div>

<div align="center">
  <img src="./src/main/resources/META-INF/pluginIcon.svg" height="96">
  <h1>FeignHelper</h1>
  <p>面向 Spring Cloud / Spring Boot 3 / Spring 6 的现代化 Feign &amp; HTTP 接口导航助手</p>

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

## 项目简介

FeignHelper 是一个 IntelliJ IDEA 插件，为使用 OpenFeign、Spring MVC 以及 Spring 6 `@HttpExchange` 声明式 HTTP 客户端的开发者提供以下能力：

- `@FeignClient` 接口 ↔ `@RestController` 双向行内导航
- `@HttpExchange` 接口 ↔ `@RestController` 双向行内导航（Spring 6 新特性）
- 行内 gutter 图标：**左键跳转到对端方法**，**右键复制接口完整路径** 到剪贴板
- 自动解析 `application.yml` / `bootstrap.yml` / `application-{profile}.yml`，识别 `server.servlet.context-path`、`spring.mvc.servlet.path`、`${var:default}` 占位符
- 支持 Java 与 Kotlin 编写的接口（基于 UAST 语言无关抽象）
- 项目级 Service 托管缓存，PSI 变更后增量刷新
- 中英双语界面

## 兼容性

| 项 | 版本 |
| --- | --- |
| IntelliJ IDEA | `2024.3+`（build `243+`，以 Plugin Verifier 结果为准） |
| 依赖的 IDE 插件 | Java（bundled） + Kotlin（bundled） |
| 构建工具链 | JDK 21 + Gradle 9 + IntelliJ Platform Plugin 2.12 |

## 安装

### 方式一：JetBrains Marketplace（推荐）

`IDEA` → `Settings` → `Plugins` → `Marketplace` → 搜索 **FeignHelper** → `Install`

或在 [JetBrains Marketplace](https://plugins.jetbrains.com/search?search=FeignHelper) 搜索 **FeignHelper**。

### 方式二：本地 zip 安装

1. 到 [GitHub Releases](https://github.com/sxhjlzl/feignhelper/releases/latest) 下载最新的 `feignhelper-<version>.zip`
2. `IDEA` → `Settings` → `Plugins` → 齿轮图标 → `Install Plugin from Disk...` → 选择 zip

## 使用指南

在任意 Spring 项目里：

- 打开 `@FeignClient` / `@HttpExchange` 接口方法，编辑器左侧 gutter 会出现 **向右箭头** ➡
  - **左键** → 跳转到对应 `@RestController` 实现方法
  - **右键** → 弹出菜单，复制完整 URL 到剪贴板
- 打开 `@RestController` 方法，编辑器左侧 gutter 会出现 **向左箭头** ⬅
  - **左键** → 跳转到对应 Feign / HttpExchange 客户端方法
  - **右键** → 弹出菜单，复制完整 URL 到剪贴板
- 多个匹配目标时，左键会弹出选择列表
- **FeignHelper 工具窗口**（IDE 右侧边栏 `FeignHelper`）：
  - 两个 Tab 分别列出所有 **Controller 接口** 与 **Feign / HttpExchange 接口**
  - 按类分组、按 URL + HTTP 方法排序，显示接口数量统计
  - 支持按 URL、HTTP 方法或类名搜索过滤
  - 双击/回车跳转源码，右键可复制 URL 或跳转到对端接口

`Settings` → `Tools` → `FeignHelper`：

- 手动覆盖 Spring 激活的 profile（留空则自动从配置文件推断）

## 本地构建

```bash
./gradlew :compileKotlin       # 仅编译 Kotlin 源码
./gradlew :build               # 完整构建
./gradlew :buildPlugin         # 产出 build/distributions/feignhelper-<version>.zip
./gradlew :runIde              # 沙箱拉起 IDE 调试
./gradlew :verifyPlugin        # 与目标 SDK 兼容性校验
```

要求：JDK 21（推荐 SDKMAN 安装 `21.0.11-tem`）+ Gradle 9（仓库自带 wrapper）。

## 工程结构

```
feignhelper-plugin/
├── build.gradle.kts                # Gradle 9 + IntelliJ Platform Plugin 2.12
├── gradle.properties               # 平台版本与插件元信息
├── docs/
│   └── publish.md                  # 发布到 Marketplace 的流程文档
└── src/main/
    ├── kotlin/com/lizhuolun/feignhelper/
    │   ├── FeignHelperBundle.kt    # 国际化入口（DynamicBundle）
    │   ├── core/                   # 数据模型与算法（纯 PSI、无 UI）
    │   ├── config/                 # yml / properties 解析（占位符 + profile）
    │   ├── scanner/                # AnnotatedElementsSearch 高性能扫描
    │   ├── cache/                  # PROJECT Service 形式的双缓存
    │   ├── listener/               # ProjectActivity 预热 + PsiTreeChangeAdapter
    │   ├── provider/               # LineMarker（左键跳转 + 右键复制 URL）
    │   └── settings/               # 配置页 + PersistentStateComponent
    └── resources/
        ├── META-INF/plugin.xml     # 现代化扩展点声明
        ├── icons/                  # 方向箭头图标，含 _dark 暗黑变体
        └── messages/               # 中英双语 bundle
```

## 贡献

- [CHANGELOG.md](./CHANGELOG.md) — 版本变更日志
- [CONTRIBUTING.md](./CONTRIBUTING.md) — 贡献者指南
- [SECURITY.md](./SECURITY.md) — 安全策略
- [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md) — 行为准则
- [PRIVACY.md](./PRIVACY.md) — 隐私说明
- [THIRD_PARTY_NOTICES.md](./THIRD_PARTY_NOTICES.md) — 第三方依赖声明

欢迎提 Issue 。

## 许可证

[Apache License 2.0](./LICENSE) © lizhuolun
