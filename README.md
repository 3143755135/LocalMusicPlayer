# 音乐播放器（Android 本地播放器）

一个纯本地的安卓音乐播放器：不联网、不集成任何音源、没有插件系统，只读取设备上已有的音频文件并播放。

工程可以在 Android Studio 里直接打开运行，也可以用命令行构建。

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

**下载最新版本**：[Releases](https://github.com/3143755135/LocalMusicPlayer/releases/latest) ｜ **仓库**：https://github.com/3143755135/LocalMusicPlayer

## 已实现的功能

- **曲库**：自动扫描设备音频（MediaStore），首次启动引导授权
- **四个板块**：歌曲 / 专辑 / 艺术家 / 歌单
- **播放**：播放全部、随机播放、点击任意一首从该处开始播放
- **播放页**：大封面、进度拖动、上一首/下一首、随机、列表循环/单曲循环、0.75x–2x 倍速、睡眠定时、收藏
- **播放队列**：查看队列、跳转播放、上移/下移、移出队列
- **后台播放**：通知栏控制、锁屏控制、耳机线控；拔耳机/断蓝牙自动暂停；来电等场景自动处理音频焦点
- **歌单**：新建、重命名、删除、添加歌曲、从歌单移除、上下移动排序
- **其他**：收藏、最近播放、本地搜索（标题/艺术家/专辑）、歌曲排序（标题/艺术家/专辑/最近添加/时长）
- **外观**：Material 3，深浅色跟随系统

## 技术栈

| 项 | 版本 |
|---|---|
| 语言 / 构建 | Kotlin 2.0.21、Gradle 8.9、AGP 8.7.3 |
| 最低 / 目标版本 | minSdk 24（Android 7.0）、targetSdk 35 |
| UI | Jetpack Compose + Material 3（BOM 2024.12.01） |
| 播放内核 | AndroidX Media3 1.5.1（ExoPlayer + MediaSession） |
| 导航 | Navigation Compose 2.8.5 |
| 本地存储 | kotlinx-serialization（JSON 文件）；无 Room、无注解处理器 |

## 构建与运行

**方式一：Android Studio（推荐）**

用 Android Studio 打开本目录，等待 Gradle 同步完成后直接运行 `app`。要求 Android Studio 自带 JDK 17（Jellyfish 2024.1 及以后版本都满足），SDK 需要安装 API 35。

**方式二：命令行**

需要 JDK 17 与 Android SDK（platform 35 + build-tools 35.0.0）：

```bash
export JAVA_HOME=/path/to/jdk17
export ANDROID_HOME=/path/to/android-sdk
./gradlew assembleDebug          # 产物：app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease        # 未签名 release 包
```

Windows 用 `gradlew.bat`，并在系统环境变量里配置 `JAVA_HOME` 与 `ANDROID_HOME`。

**方式三：直接装现成 APK**

从 [Releases](https://github.com/3143755135/LocalMusicPlayer/releases/latest) 页面下载最新版本的 `MusicPlayer-<版本>-debug.apk`（带 debug 签名，可以直接安装），拷到手机后用文件管理器打开安装，或：

```bash
adb install -r MusicPlayer-1.0.0-debug.apk
```

Release 页面同时提供混淆压缩后的 release 包；正式分发前需要用你自己的 keystore 重新签名，见「开源与发布」一节。

## 在手机上安装与使用

### 1. 安装

**用现成的 APK（最快）**

到 [Releases](https://github.com/3143755135/LocalMusicPlayer/releases/latest) 页面下载最新版本 APK，拷到手机（数据线、微信文件传输、网盘都行），用手机上的文件管理器点开安装。系统如果提示“不允许安装未知来源应用”，在弹窗里选择允许当前文件管理器安装即可。

也可以用数据线：

```bash
adb install -r MusicPlayer-1.0.0-debug.apk
```

**用 Android Studio 直接跑到手机**

手机打开「开发者选项 → USB 调试」，用数据线连电脑，在 Android Studio 顶部选中你的设备，点运行按钮。这种方式修改代码后可以立刻重装，调试最方便。

> 说明：debug 包的包名是 `com.localmusic.player.debug`，和正式包 `com.localmusic.player` 是两个独立应用，可以同时装在一台手机上。

### 2. 首次启动

打开后会直接弹出权限申请，需要同意两项：

- **音乐和音频**：读不到就看不到任何歌，是必须的。
- **通知**：允许后才有通知栏播放控制。拒绝也不影响播放，只是没有通知栏。

如果误点了拒绝：到「系统设置 → 应用 → 音乐播放器 → 权限」里手动打开「音乐和音频」，然后回到应用即可（应用在回到前台时会自动重新检查权限）。

### 3. 把歌放进手机

这个播放器读的是系统的媒体库（MediaStore），所以：

- 把音频文件放在手机存储的常见位置，比如 `内部存储/Music/`、`Download/`，或者任意文件夹都行，只要被系统媒体库收录。
- 通过数据线拷贝或用音乐 App 下载到手机后，系统一般会自动收录。**如果新拷进去的歌没出现在列表里**，先重启一次手机（或让文件管理器触发一次媒体扫描），再回到应用点右上角的刷新按钮。
- `Android/data/` 目录下的音频不会被系统收录，放这里读不到。
- 铃声、通知音这类 `IS_MUSIC` 标记为 false 的文件会被过滤掉，不会出现在列表里。

### 4. 界面导览

| 位置 | 用法 |
|---|---|
| 底部「歌曲」 | 播放全部 / 随机播放；点任意一首从该处开始播；右上角可切换排序、重新扫描、进入搜索 |
| 底部「专辑」 | 网格浏览，点进去看整张专辑并按顺序播放 |
| 底部「艺术家」 | 按艺术家聚合，点进去看该艺术家所有歌曲 |
| 底部「歌单」 | 我的收藏、最近播放、自建歌单；右上角 `+` 新建歌单 |
| 每首歌右侧的 `⋮` | 下一首播放、加入播放队列、添加到歌单、收藏（在歌单里还多出上移/下移/从歌单移除） |
| 底部迷你播放条 | 点击展开完整播放页 |
| 播放页 | 拖动进度条跳转；左右两侧是随机、循环模式；下方是收藏、睡眠定时、播放队列；右上角切换倍速 |
| 播放队列 | 点任意一首直接跳过去播放，也可以上移、下移、移出队列 |

### 5. 后台播放

播放时下拉通知栏可以看到播放控制，锁屏界面同样可以控制上一首/下一首，插着的耳机线控和蓝牙耳机按键也有效。从最近任务里划掉应用**不会**停止正在播放的音乐，只有暂停状态下才会真正退出。

如果发现锁屏一段时间后音乐被系统杀掉，通常是国产系统的省电策略，需要手动放行：

- 小米 / Redmi：设置 → 应用设置 → 音乐播放器 → 省电策略改成「无限制」，并允许「自启动」
- 华为 / 荣耀：设置 → 应用 → 应用启动管理 → 改成「手动管理」，三个开关全开
- OPPO / vivo / 一加：设置在电池或耗电管理里，允许「后台运行」和「自启动」

### 6. 常见问题

- **一首歌都看不到**：先确认权限已给；再确认音频文件在被系统收录的位置；最后点列表右上角的刷新按钮。
- **播放没有声音**：检查是否被耳机或蓝牙设备占用，应用会自动处理音频焦点，接到来电时会暂停。
- **歌曲信息不对**：标题、艺术家、专辑都取自音频文件自身的标签，标签乱的话可以用电脑上的音乐标签工具改好再拷进来。
- **换了新手机怎么保留歌单**：歌单存在应用私有目录的 json 文件里，换机需要重新建歌单（后续可以加导出/导入功能）。

## 开源与发布

本项目以 **MIT** 许可证开源，欢迎提交 issue 与 PR。贡献前请先阅读 [CONTRIBUTING.md](CONTRIBUTING.md)。

每次推送到 `main` 或发起 PR，GitHub Actions 会自动执行 `assembleDebug` 与 `lintDebug`（见 `.github/workflows/android.yml`）。

**发布新版本**

```bash
git tag -a v1.0.1 -m "v1.0.1"
git push origin v1.0.1
```

推完 tag 后到 GitHub 的 Releases 页面创建 Release，上传签名后的 APK 并填写更新说明。

**正式签名**

```bash
keytool -genkey -v -keystore release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release
```

然后把签名配置写进 `app/build.gradle.kts`，密码通过环境变量或 `local.properties` 读取。`release.jks` 与密码**不要提交到仓库**，一旦泄露或丢失都会很麻烦。

## 验证情况

本工程在下面的环境里真实编译过：

- Microsoft OpenJDK 17.0.13 + Android SDK Platform 35 + Build-Tools 35.0.0 + Gradle 8.9
- `assembleDebug`：BUILD SUCCESSFUL（19.9 MB）
- `assembleRelease`：BUILD SUCCESSFUL，包含 R8 混淆、资源压缩与 lintVital（2.8 MB）
- `lintDebug`：**0 errors**，30 warnings（全部是“依赖有新版本可用”的提示）
- APK 元数据核对通过：包名 `com.localmusic.player`、targetSdk 35、权限与启动 Activity 正常

没有做运行时验证：本次环境没有真机也没有模拟器，界面效果与实际播放过程需要你在设备上确认。

## 项目结构

```
app/src/main/java/com/localmusic/player/
├── MusicApp.kt                     Application，持有全局依赖容器
├── MainActivity.kt                 Compose 入口，连接播放服务
├── di/AppContainer.kt              手写依赖容器（曲库 / 歌单 / 封面 / 播放连接）
├── data/
│   ├── model/Song.kt               Song、Album、Artist、排序枚举
│   ├── model/Playlist.kt           歌单与持久化快照
│   ├── LibraryRepository.kt        MediaStore 扫描与专辑/艺术家聚合
│   ├── MusicStoreRepository.kt     歌单/收藏/最近播放的 JSON 持久化
│   └── ArtworkLoader.kt            封面提取 + 内存缓存
├── playback/
│   ├── PlaybackService.kt          MediaSessionService + ExoPlayer
│   ├── PlayerConnection.kt         界面与服务的唯一桥梁（状态流 + 指令）
│   └── SongMediaMapper.kt          Song <-> MediaItem 映射
└── ui/
    ├── AppRoot.kt                  底部导航 + 导航图 + 迷你播放器
    ├── PermissionGate.kt           读取音频权限的引导页
    ├── theme/                      配色与主题
    ├── components/                 封面、歌曲行、迷你播放器、对话框、操作面板
    ├── screens/                    歌曲 / 专辑 / 艺术家 / 歌单 / 搜索 / 播放页
    └── viewmodel/                  LibraryViewModel、StoreViewModel
```

## 几个关键实现

**曲库扫描**：查询 `MediaStore.Audio`，只保留 `IS_MUSIC != 0` 且时长大于 0 的条目。没有申请 `MANAGE_EXTERNAL_STORAGE`，Android 13 及以上用 `READ_MEDIA_AUDIO`，12 及以下用 `READ_EXTERNAL_STORAGE`。权限没拿到时不创建曲库 ViewModel，避免出现一次空扫描。

**播放架构**：`PlaybackService` 里持有 ExoPlayer 和 MediaSession；界面通过 `PlayerConnection` 拿 `MediaController`，只读状态流、只调指令方法，不直接接触播放器实例。这样后台播放、通知栏、锁屏控制都由系统统一驱动。

**队列还原**：`Song` 的全部字段写进 `MediaItem` 的 `MediaMetadata.extras`。播放队列经过 MediaSession（甚至服务被系统重启）之后，界面依然能把 `MediaItem` 还原成完整的歌曲信息，而不是只剩一个 URI。

**封面提取**：先用 `ContentResolver.loadThumbnail`（API 29+），失败再回退到 `MediaMetadataRetriever` 读音频内嵌图片；按「URI + 目标尺寸」做 LRU 内存缓存，并把取不到的也记下来，避免列表滚动时反复解码。

**歌单持久化**：歌单、收藏、最近播放序列化到 `filesDir/music_store.json`，写入做了 200ms 防抖，连续点赞收藏不会每点一次就落盘。歌单里存的是歌曲 id，文件被删除的歌曲会自动跳过。

## 权限说明

| 权限 | 用途 |
|---|---|
| `READ_MEDIA_AUDIO` | Android 13+ 读取本机音频 |
| `READ_EXTERNAL_STORAGE`（maxSdk 32） | Android 12 及以下读取本机音频 |
| `POST_NOTIFICATIONS` | 显示播放通知 |
| `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | 后台播放的前台服务 |

清单里**没有 `INTERNET` 权限**，所以这个应用在系统层面就无法发起网络请求——它确实只是一个本地播放器。

## 已知限制与后续可以做的

- **通知栏封面**：Media3 默认的 `BitmapLoader` 无法解码音频内嵌图片，通知里可能只显示歌曲名和歌手。想要封面需要自定义 `BitmapLoader`（应用内的封面显示不受影响）。
- 界面文案目前直接写在 Kotlin 代码里，要国际化需要挪到 `strings.xml`。
- 歌单排序用的是上移/下移，还没有拖拽排序。
- 没有做：均衡器、桌面小组件、标签编辑、按文件夹浏览、多选批量操作。
- 依赖锁在 2024 年底的稳定版本上并已实测可编译；lint 会提示存在更新的 AGP / Compose / Media3 版本，升级后建议重新编译验证一遍。
- release 包未配置签名，发布前需要在 `app/build.gradle.kts` 里加 `signingConfigs`。

## 许可证

[MIT License](LICENSE) © 2026 LocalMusicPlayer contributors

可以自由使用、修改、分发和商用，只需保留原始版权声明。第三方依赖（AndroidX、Media3、Jetpack Compose 等）遵循各自的上游许可，主要为 Apache-2.0。
