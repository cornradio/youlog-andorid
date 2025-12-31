# YouLog - 图片日志应用

一个美观简洁的Android图片日志应用，用于管理和查看个人照片。

## 功能特性

### 核心功能

- **时间线（Timeline）**：按日期展示已保存的照片，点击进入图片详情
- **拍照导入（Camera）**：应用内拍照并保存到时间线（需相机权限）
- **相册导入（Photo Library）**：从系统相册导入图片到应用（可选导入后从相册删除）
- **分享导入（Share）**：支持系统分享将图片导入 youlog

### 图片查看与管理

- **图片详情 / 沉浸式查看（Image Detail）**：全屏查看图片、放大/滑动、进入或编辑笔记、分享与删除
- **迷你视图（Mini View）**：紧凑缩略图流用于快速浏览（已优化滑动性能）

### 笔记与标签

- **笔记（Notes）**：为图片添加/编辑文本笔记，支持在详情页查看与复制
- **标签管理（Tags）**：给图片打标签，支持多张图片的批量标签修改

### 高级功能

- **日期筛选与批量操作（Date Filter & Bulk Delete）**：按日期筛选照片，支持全预览、批量选择与删除
- **自动处理（Compression & Auto-delete）**：导入时可自动压缩图片；可选择导入后删除原相册图片

## 技术栈

- **语言**：Kotlin
- **架构**：MVVM (Model-View-ViewModel)
- **数据库**：Room
- **图片加载**：Glide
- **UI框架**：Material Design Components
- **异步处理**：Kotlin Coroutines

## 项目结构

```
app/
├── src/main/
│   ├── java/com/youlog/app/
│   │   ├── data/              # 数据层（Room数据库、实体类、DAO）
│   │   ├── repository/         # 数据仓库
│   │   ├── ui/                 # UI层（Activity、Fragment、Adapter）
│   │   ├── ui/viewmodel/       # ViewModel
│   │   └── utils/              # 工具类
│   ├── res/                    # 资源文件
│   └── AndroidManifest.xml
└── build.gradle
```

## 安装与运行

1. 克隆项目到本地
2. 使用 Android Studio 打开项目
3. 同步 Gradle 依赖
4. 连接 Android 设备或启动模拟器
5. 点击运行按钮

## 权限说明

应用需要以下权限：
- **相机权限**：用于拍照功能
- **存储权限**：用于从相册导入图片（Android 13+ 使用 READ_MEDIA_IMAGES）

## 注意事项

- 图片存储在应用内部存储，不会保存到系统相册
- 删除应用会同时删除所有保存的图片
- 建议定期备份重要图片

## 开发计划

- [ ] 实现批量标签编辑功能
- [ ] 添加图片搜索功能
- [ ] 支持导出图片到相册
- [ ] 添加云同步功能

## 许可证

MIT License

