# 5000choyen-mirai

这是一个使用 Kotlin 实现的生成 [5000choyen](https://github.com/yurafuca/5000choyen) 风格文字的 [mirai](https://github.com/mamoe/mirai) 插件。无需外挂 nodejs 服务器，开箱即用。

## 使用方法

默认调用方法如下：
```
生成5k 第一行文本
第二行文本
```
该命令所有别名如下：
* 生成5k
* /生成5k
* gocho
* /gocho
* choyen
* /choyen
* /5k

本插件支持自定义命令别名，只需关闭 bot 后修改 `config/xyz.xszq.fivethousand-choyen/config.yml` 中的 `commands` 项即可。

## 更换字体

您可以修改 `config/xyz.xszq.fivethousand-choyen/config.yml` 中的 `topFont` 和 `bottomFont` 来自定义字体，此项建议使用字体的英文名，否则可能无法识别到。