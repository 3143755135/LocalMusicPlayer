# 参与贡献

感谢你考虑为这个项目做贡献。

## 开发环境

- JDK 17
- Android SDK Platform 35
- Android Studio（推荐）或命令行 Gradle 8.9

## 本地构建

```bash
./gradlew assembleDebug
./gradlew lintDebug
```

提交前请确认 `lintDebug` 为 0 error。CI 也会在每次推送和 PR 上执行这两项检查。

## 提交改动

1. Fork 本仓库，并从 `main` 切出功能分支
2. 保持改动聚焦，一个 PR 只做一件事
3. 在 PR 描述里说明要解决的问题与实现方案
4. 涉及界面的改动请附上截图

## 代码约定

- 使用 Kotlin 官方代码风格（工程已配置 `kotlin.code.style=official`）
- 界面文案目前为中文，新增文案请保持一致
- 不引入网络请求、第三方音源或插件系统：本项目定位为纯本地播放器

## 许可证

你提交的代码将以 [MIT License](LICENSE) 授权。
