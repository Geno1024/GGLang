# GGLang Script 1 Simple Spec

GGLang 的所谓“脚本”形式，本质上是描述程序控制流的 JSON 文件。

<!-- TOC -->
* [GGLang Script 1 Simple Spec](#gglang-script-1-simple-spec)
  * [语法](#语法)
    * [基础结构](#基础结构)
    * [`variable` 对象结构](#variable-对象结构)
    * [`function` 对象结构](#function-对象结构)
    * [`element` 对象结构](#element-对象结构)
    * [`parameter` 对象结构](#parameter-对象结构)
    * [`return` 对象结构](#return-对象结构)
    * [`Op` 枚举类型](#op-枚举类型)
    * [`input` 对象结构](#input-对象结构)
  * [有效性](#有效性)
    * [GGLang 1 宽式脚本](#gglang-1-宽式脚本)
    * [GGLang 1 严式脚本](#gglang-1-严式脚本)
  * [转译](#转译)
    * [版本 1 转译方式](#版本-1-转译方式)
      * [`.name`](#name)
      * [`.id`](#id)
      * [`.comment`](#comment)
      * [`.variables`](#variables)
      * [`.functions`](#functions)
      * [`.elements`](#elements)
<!-- TOC -->

## 语法

### 基础结构

GGLang 1 脚本包含 `.version`，`.name`，`.id`，`.variables`，`.functions`，`.elements` 六个第一层对象。

另外，任何 GGLang 1 脚本中的对象均接受 `.comment` 键以描述其注释。

| 键            | 类型                                      | 描述                       |
|--------------|-----------------------------------------|--------------------------|
| `.version`   | 数值                                      | GGLang 版本号               |
| `.name`      | 字符串                                     | GGLang 脚本名称，转译后生成的类名     |
| `.id`        | 字符串                                     | UUID，用于进行区分              |
| `.variables` | 包含若干 [`variable` 对象](#variable-对象结构)的数组 | 全局变量                     |
| `.functions` | 包含若干 [`function` 对象](#function-对象结构)的数组 | 函数列表                     |
| `.elements`  | 包含若干 [`element` 对象](#element-对象结构)的数组   | 元件列表，每个元件转译后生成一个 Java 语句 |

### `variable` 对象结构

一个 `variable` 对象记录了一个全局变量。

| 键                | 类型  | 描述          |
|------------------|-----|-------------|
| `.id`            | 字符串 | UUID，用于进行区分 |
| `.name`          | 字符串 | 变量名         |
| `.type`          | 字符串 | 变量类型        |
| `.initial_value` | 字符串 | 变量初始值       |

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

除非 `.type` 值为 `void`，否则一个 `element` 对象最终都会转译成一句简单 Java 运算赋值语句。由于 Java 中 `void` 为不能被赋值的 bottom type，所以 `void v = doSth();` 语句是无法通过编译的，因此如果 `.type` 是 `void` 的话，只能转译成一句简单 Java 运算语句。

这里的“简单”指的是，一个语句除了赋值之外**只有一个**操作。例如，`a = b + c + d;` 和 `d = doSth(e, f + g);` 是不允许的，因为它们都包含了多个操作。

| 键                | 类型                               | 描述                                      |
|------------------|----------------------------------|-----------------------------------------|
| `.id`            | 字符串                              | UUID，用于区分                               |
| `.name`          | 字符串                              | 元件名，转译出的 Java 语句的变量名                    |
| `.type`          | 字符串                              | 元件类型，转译出的 Java 赋值语句的类型，如果是 `void` 则没有赋值 |
| `.op`            | [`Op` 枚举类型](#Op-枚举类型)            | 元件的操作类型，转译出的 Java 语句的运算类型               |
| `.inputs`        | 包含若干[`input` 对象](#input-对象结构)的数组 | 元件的输入，根据运算类型确定“输入”的含义                   |
| `.next_elements` | 包含若干字符串的数组                       | 元件所连接的下一个元件的 ID 列表                      |

### `parameter` 对象结构

一个 `parameter` 对象记录了一个函数的入参。

| 键       | 类型  | 描述    |
|---------|-----|-------|
| `.type` | 字符串 | 入参的类型 |
| `.name` | 字符串 | 入参的名称 |

### `return` 对象结构

一个 `return` 对象记录了一个函数的出参。

| 键       | 类型  | 描述    |
|---------|-----|-------|
| `.type` | 字符串 | 出参的类型 |

### `Op` 枚举类型

`Op` 枚举类型描述了该元件的操作。

为表述方便，此表格使用 `$n` 代表 `${.inputs[n].value}`。

| 枚举值             | 含义      | 拼接方式                                                                     |
|-----------------|---------|--------------------------------------------------------------------------|
| `ARRAY_INDEX`   | 数组取下标操作 | `$0[$1]`                                                                 |
| `ASSIGN`        | 赋值操作    | `$0`                                                                     |
| `BRANCH_CALL`   | 分支操作    | `if ($0) { $1 }`<br/> `else if ($2) { $3 }`<br/> ...<br/> `else { $n } ` |
| `FUNCTION_CALL` | 函数调用操作  | `$0($1, ...)`                                                            |
| `INFIX`         | 中缀运算符操作 | `$0 $1 $2`                                                               |
| `UNARY`         | 单目运算符操作 | `$0$1`                                                                   |

### `input` 对象结构

一个 `input` 对象记录了一个元件转译成 Java 之后的语句的右手式中的一个 AST 节点。具体节点的含义及转译方式由 `op` 枚举值确定。

| 键        | 类型  | 描述  |
|----------|-----|-----|
| `.type`  | 字符串 | 类型  |
| `.value` | 字符串 | 值   |

## 有效性

根据对 GGLang 1 脚本的有效性的校验要求，可以将其分为“宽式”和“严式”两种。

### GGLang 1 宽式脚本
### GGLang 1 严式脚本

一个 GGLang 1 严式脚本必然符合 GGLang 1 宽式脚本的要求，并在其之上增添如下规则：

## 转译

一个 GGLang 1 脚本将转译为一个 Java 文件。

`.version` 用于判断 GGLang 脚本的版本号，若版本号为 1 则按照下文所叙述的过程进行转译。

### 版本 1 转译方式

#### `.name`
将转译为 Java 文件的类名：

```json
{
  "name": "Clazz"
}
```
将转译为：
```java
public class Clazz {
    
}
```

#### `.id`
暂不用于转译。

#### `.comment`
转译为类上的注释：
```json
{
  "name": "Clazz",
  "comment": "comment of class"
}
```
将转译为：
```java
/**
comment of class
 */
public class Clazz {
    
}
```

#### `.variables`
转译为类中的静态全局变量：
```json
{
  "name": "Clazz",
  "variables": [
    {
      "name": "field",
      "type": "String",
      "initial_value": "\"a\""
    }
  ]
}
```
将转译为：
```java
public class Clazz {
    public static String field = "a";
}
```

#### `.functions`
转译为类中的静态函数：
```json
{
  "name": "Clazz",
  "functions": [
    {
      "name": "main",
      "parameters": [
        {
          "type": "String[]",
          "name": "args"
        }
      ],
      "returns": [
        "void"
      ]
    }
  ]
}
```
将转译为：
```java
public class Clazz {
    public static void main(String[] args) {

    }
}
```

#### `.elements`
转译为每一句 Java 语句。根据 `.op` 的操作以确定转译的方式与结果。

- `array_index` 将转译为数组下标操作：
  ```json
  {
    "name": "Clazz",
    "functions": [
      {
        "name": "main",
        "parameters": [
          {
            "type": "String[]",
            "name": "args"
          }
        ],
        "returns": [
          "void"
        ],
        "next_elements": [
          "e00c2192-da7a-4e19-b51e-8bfa99687608"
        ]
      }
    ],
    "elements": [
      {
        "id": "e00c2192-da7a-4e19-b51e-8bfa99687608",
        "type": "String",
        "name": "arg",
        "op": "array_index",
        "inputs": [
          {
            "type": "String[]",
            "value": "args"
          },
          {
            "type": "int",
            "value": "0"
          }
        ]
      }
    ]
  }
  ```
  将转译为：
  ```java
  public class Clazz {
      public static void main(String[] args){
          String arg = args[0];
      }
  }
  ```

- `assign` 将转译为赋值操作：
  ```json
  {
    "name": "Clazz",
    "functions": [
      {
        "name": "main",
        "parameters": [
          {
            "type": "String[]",
            "name": "args"
          }
        ],
        "returns": [
          "void"
        ],
        "next_elements": [
          "c82a53b5-53bd-4f63-9a56-ed046a4a8cf1"
        ]
      }
    ],
    "elements": [
      {
        "id": "c82a53b5-53bd-4f63-9a56-ed046a4a8cf1",
        "type": "String",
        "name": "arg",
        "op": "assign",
        "inputs": [
          {
            "type": "String",
            "value": "\"foo\""
          }
        ]
      }
    ]
  }
  ```
  将转译为：
  ```java
  public class Clazz {
      public static void main(String[] args){
          String arg = "foo";
      }
  }
  ```

- `branch_call` 将转译为分支操作：
  ```json
  {
    "name": "Clazz",
    "functions": [
      {
        "name": "main",
        "parameters": [
          {
            "type": "String[]",
            "name": "args"
          }
        ],
        "returns": [
          "void"
        ],
        "next_elements": [
          "8b95eebd-2754-4f42-b00d-3c70d77096a5"
        ]
      }
    ],
    "elements": [
      {
        "id": "8b95eebd-2754-4f42-b00d-3c70d77096a5",
        "type": "String",
        "name": "arg",
        "op": "array_index",
        "inputs": [
          {
            "type": "String[]",
            "value": "args"
          },
          {
            "type": "int",
            "value": "0"
          }
        ],
        "next_elements": [
          "c9084be0-3826-4dea-a705-d02c0cd59f75"
        ]
      },
      {
        "id": "c9084be0-3826-4dea-a705-d02c0cd59f75",
        "type": "void",
        "name": "",
        "op": "branch_call",
        "inputs": [
          {
            "type": "boolean",
            "value": "arg.equals(\"pass\")"
          },
          {
            "type": "",
            "value": "276b3af0-d259-4933-a1fa-d23ec573f777"
          },
          {
            "type": "",
            "value": "97ce7182-3d4d-420d-9885-3f73ea1dc238"
          }
        ]
      },
      {
        "id": "276b3af0-d259-4933-a1fa-d23ec573f777",
        "type": "void",
        "name": "",
        "op": "function_call",
        "inputs": [
          {
            "type": "",
            "value": "System.out.println"
          },
          {
            "type": "char",
            "value": "'Y'"
          }
        ]
      },
      {
        "id": "97ce7182-3d4d-420d-9885-3f73ea1dc238",
        "type": "void",
        "name": "",
        "op": "function_call",
        "inputs": [
          {
            "type": "",
            "value": "System.out.println"
          },
          {
            "type": "char",
            "value": "'N'"
          }
        ]
      }
    ]
  }
  ```
  将转译为：
  ```java
  public class Clazz {
      public static void main(String[] args){
          String arg = args[0];
          if (arg.equals("pass")) {
            System.out.println('Y');
          } else {
            System.out.println('N');
          }
      }
  }
  ```
  若 `branch_call` 有偶数个 `input`，则每两个为一组，每一组的第一个为判断条件，第二个为判断条件成立时的 `next_element`；若有奇数个，则除了最后一个之外的均与偶数个的情况相同，最后一个成为 `if` 中的 `else` 或者 `switch` 中的 `default` 分支的 `next_element`。 

- `function_call` 将转译为函数调用操作：
  ```json
  {
    "name": "Clazz",
    "functions": [
      {
        "name": "main",
        "parameters": [
          {
            "type": "String[]",
            "name": "args"
          }
        ],
        "returns": [
          "void"
        ],
        "next_elements": [
          "b1d4a121-d17c-46e5-84e4-19b4f0006458"
        ]
      }
    ],
    "elements": [
      {
        "id": "b1d4a121-d17c-46e5-84e4-19b4f0006458",
        "type": "void",
        "name": "",
        "op": "function_call",
        "inputs": [
          {
            "type": "",
            "value": "System.out.println"
          },
          {
            "type": "String",
            "value": "\"bar\""
          }
        ]
      }
    ]
  }
  ```
  将转译为：
  ```java
  public class Clazz {
      public static void main(String[] args){
          System.out.println("bar");
      }
  }
  ```
  
- `infix` 将转译为中缀操作：
  ```json
  {
    "name": "Clazz",
    "functions": [
      {
        "name": "main",
        "parameters": [
          {
            "type": "String[]",
            "name": "args"
          }
        ],
        "returns": [
          "void"
        ],
        "next_elements": [
          "b1d4a121-d17c-46e5-84e4-19b4f0006458"
        ]
      }
    ],
    "elements": [
      {
        "id": "b1d4a121-d17c-46e5-84e4-19b4f0006458",
        "type": "int",
        "name": "a",
        "op": "infix",
        "inputs": [
          {
            "type": "int",
            "value": "1"
          },
          {
            "type": "",
            "value": "+"
          },
          {
            "type": "int",
            "value": "2"
          }
        ]
      }
    ]
  }
  ```
  将转译为：
  ```java
  public class Clazz {
      public static void main(String[] args){
          int a = 1 + 2;
      }
  }
  ```

- `unary` 将转译为单目操作：
  ```json
  {
    "name": "Clazz",
    "functions": [
      {
        "name": "main",
        "parameters": [
          {
            "type": "String[]",
            "name": "args"
          }
        ],
        "returns": [
          "void"
        ],
        "next_elements": [
          "b1d4a121-d17c-46e5-84e4-19b4f0006458"
        ]
      }
    ],
    "elements": [
      {
        "id": "b1d4a121-d17c-46e5-84e4-19b4f0006458",
        "type": "int",
        "name": "a",
        "op": "unary",
        "inputs": [
          {
            "type": "",
            "value": "-"
          },
          {
            "type": "int",
            "value": "2"
          }
        ]
      }
    ]
  }
  ```
  将转译为：
  ```java
  public class Clazz {
      public static void main(String[] args){
          int a = -2;
      }
  }
  ```
