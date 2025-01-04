# Android Common Utils
> 安卓通用工具，仅在Kotlin环境通过测试

## Global Application Context Provider
> Application Context全局提供器

### Usage
> 使用方式
```kotlin

//init
class MyApplication:ApplicationExt()

//getter
ApplicationModule.application.context

```


## Extension Function to Prevent Rapid Clicks on View
> 防止View快速点击的拓展方法

### Usage
> 使用方式

```kotlin

//single tap
view.setOnSingleClickListener(delayMillis = 500L){
  //todo
}

//long tap
view.setOnLongSingleClickListener(delayMillis = 500L){
  //todo
}

```

## Custom Logging Utility
> 自定义的Log工具

fork from this project [LogCat](https://github.com/liangjingkanji/LogCat)


## MMKV Wrapper Based on Delegation Pattern
> 基于委托模式的MMKV封装

reference from this article [Kotlin 委托属性（Delegated properties）实战之 MMKV 封装](https://juejin.cn/post/7251876857704038458)

### Usage
> 使用方式

```kotlin

//init 
MMKVUtils.INSTANCE.init(context)

//create
var item by MMKVItem("key", 0)

//setter
item = 1

//getter
item

```

## waiting ...
