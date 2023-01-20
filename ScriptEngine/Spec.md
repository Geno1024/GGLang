# GGLang Script 1 Simple Spec

GGLang 的所谓“脚本”形式，本质上是描述程序控制流的 JSON 文件。

<!-- TOC -->
* [GGLang Script 1 Simple Spec](#gglang-script-1-simple-spec)
  * [语法](#语法)
    * [基础结构](#基础结构)
    * [`variable` 对象结构](#variable-对象结构)
<!-- TOC -->

## 语法

### 基础结构

GGLang 1 脚本包含 `.version`，`.name`，`.id`，`.variables`，`.functions`，`.elements` 六个第一层对象。

| 键            | 类型                                      | 描述                       |
|--------------|-----------------------------------------|--------------------------|
| `.version`   | 数值                                      | GGLang 版本号               |
| `.name`      | 字符串                                     | GGLang 脚本名称，转译后生成的类名     |
| `.id`        | 字符串                                     | UUID，用于进行区分              |
| `.variables` | 包含若干 [`variable` 对象](#variable-对象结构)的数组 | 全局变量                     |
| `.functions` | 包含若干 [`function` 对象](#function-对象结构)的数组 | 函数列表                     |
| `elements`   | 包含若干 [`element` 对象](#element-对象结构)的数组   | 元件列表，每个元件转译后生成一个 Java 语句 |

### `variable` 对象结构

一个 `variable` 对象记录了一个全局变量。

| 键       | 类型  | 描述          |
|---------|-----|-------------|
| `.id`   | 字符串 | UUID，用于进行区分 |
| `.name` | 字符串 | 变量名         |
| `.type` | 字符串 | 变量类型        |

### `function` 对象结构

一个 `function` 对象记录了一个函数。

| 键                | 类型                                        | 描述                                         |
|------------------|-------------------------------------------|--------------------------------------------|
| `.id`            | 字符串                                       | UUID，用于进行区分                                |
| `.name`          | 字符串                                       | 函数名                                        |
| `.parameters`    | 包含若干 [`parameter` 对象](#parameter-对象结构)的数组 | 函数入参列表                                     |
| `.returns`       | 包含若干 [`return` 对象](#return-对象结构)的数组       | 函数返回类型列表，但版本 1 只支持第 0 个                    |
| `.next_elements` | 包含若干字符串的数组                                | 函数的起始 [`element` 对象](#element-对象结构)的 ID 列表 |

### `element` 对象结构

一个 `element` 对象记录了一个元件。

除非 `.type` 值为 `void`，否则一个 `element` 对象最终都会转译成一句 Java 运算赋值语句。由于 Java 中 `void` 为不能被赋值的 bottom type，所以 `void v = doSth();` 语句是无法通过编译的，因此如果 `.type` 是 `void` 的话，只能转译成一句 Java 运算语句。

| 键                | 类型                               | 描述                                      |
|------------------|----------------------------------|-----------------------------------------|
| `.id`            | 字符串                              | UUID，用于区分                               |
| `.name`          | 字符串                              | 元件名，转译出的 Java 语句的变量名                    |
| `.type`          | 字符串                              | 元件类型，转译出的 Java 赋值语句的类型，如果是 `void` 则没有赋值 |
| `.op`            | [`Op` 枚举类型](#Op-枚举类型)            | 元件的操作类型，转译出的 Java 语句的运算类型               |
| `.inputs`        | 包含若干[`input` 对象](#input-对象结构)的数组 | 元件的输入，根据运算类型确定“输入”的含义                   |
| `.next_elements` | 包含若干字符串的数组                       | 元件所连接的下一个元件的 ID 列表                      |

### `parameter` 对象结构

一个 `parameter` 对象