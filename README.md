# AndroidHelpers
个人 Android 开发中积累的一些工具。

### 集成方式

#### 第一步：在你的项目 build.gradle 添加 maven 库：
```
allprojects {
    repositories {
        ...
        maven { url 'https://www.jitpack.io' }
    }
}
```

#### 第二步：在你的模块 build.gradle 添加依赖库：
```
dependencies {
    ...
    api 'com.github.iWay7:AndroidHelpers:1.0.5'
    api 'com.github.iWay7:JavaHelpers:1.0.5'
    api 'com.android.support:support-v4:27.1.1'
    api 'com.google.zxing:core:3.2.1'
    api 'com.google.code.gson:gson:2.8.2'
}
```