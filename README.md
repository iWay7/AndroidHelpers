# AndroidHelpers
个人 Android 开发中积累的一些工具。通过文件名，即可大致推断出该文件的用途。

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
    api 'com.github.iWay7:AndroidHelpers:1.0.19'
    api 'com.github.iWay7:JavaHelpers:1.0.13'
    api 'com.android.support:support-v4:27.1.1'
    api 'com.google.zxing:core:3.2.1'
    api 'com.google.code.gson:gson:2.8.5'
}
```

#### 对于第三方库的依赖，如果没有用到相关组件，可以不使用，或者根据需要指定其他版本。
