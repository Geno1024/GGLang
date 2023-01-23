package com.geno1024.gglang.se.v1

import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.annotation.JSONField
import java.io.File

typealias ID = String

data class ScriptDS(
    var version: Int,
    var name: String,
    var id: ID,
    var comment: String?,
    var variables: List<Variable>,
    var functions: List<Function>,
    var elements: List<Element>
) {
    companion object
    {
        fun from(input: String): ScriptDS = JSONObject.parseObject(input, ScriptDS::class.java).apply {
            functions.forEach { it.script = this }
            elements.forEach { it.script = this }
        }

        fun from(file: File): ScriptDS = from(file.readText())
    }

    data class Variable(
        var id: ID,
        var name: String,
        var type: String,
        var comment: String?,
        @JSONField(name = "initial_value") var initialValue: Any?
    ) {
        override fun toString(): String = "public static $type $name${if (initialValue != null) " = $initialValue" else ""};"
    }

    data class Function(
        var id: ID,
        var name: String,
        var comment: String?,
        var parameters: List<Parameter>,
        var returns: List<Return>, // 暂时不接受多元组
        @JSONField(name = "next_elements") var nextElements: List<ID>
    ) {
        var script: ScriptDS? = null

        override fun toString(): String = """${if (comment?.isNotEmpty() == true) "// $comment\n" else "" }public static ${returns[0].type} $name(${parameters.joinToString { "${if (it.comment?.isNotEmpty() == true) "/* ${it.comment} */ " else ""}${it.type} ${it.name}" }}) {
            |${nextElements.joinToString(separator = "\n") { "\t${script?.getElementById(it)?.iterToString()}" } }
            |}
        """.trimMargin()
    }

    data class Element(
        var id: ID,
        var name: String,
        var type: String,
        var comment: String?,
        var op: Op,
        var inputs: List<Input>,
        @JSONField(name = "next_elements") var nextElements: List<ID>
    ) {
        var script: ScriptDS? = null

        enum class Op
        {
            ARRAY_INDEX, // name = input0[input1];
            ASSIGN, // name = input0;
            BRANCH_CALL, // if (input0) { input1 } else if (input2) { input3 } else { input4 }
            FUNCTION_CALL, // name = input0(input1, ...);
            INFIX, // name = input0 input1 input2; for example "a + b"
            UNARY, // name = input0 input1; for example "-a"
        }

        override fun toString(): String = """${if (type != "void") "$type $name = " else ""}${when (op)
        {
            Op.ARRAY_INDEX -> "${inputs[0].value}[${inputs[1].value}];${if (comment?.isNotEmpty() == true) " // $comment\n" else "" }"
            Op.ASSIGN -> "${inputs[0].value};${if (comment?.isNotEmpty() == true) " // $comment\n" else "" }"
            Op.BRANCH_CALL -> "if (${inputs[0].value}) {${if (comment?.isNotEmpty() == true) " // $comment\n" else "" }\n\t${script?.getElementById(inputs[1].value)?.iterToString()} \n}" +
                inputs.drop(2).windowed(size = 2, step = 2, partialWindows = false).joinToString(separator = "\n") { " else if (${it[0].value}) {\n\t${script?.getElementById(it[1].value)?.iterToString()}}" } +
                if (inputs.size.mod(2) == 1) " else {\n\t${script?.getElementById(inputs.last().value)?.iterToString()}}" else ""
            Op.FUNCTION_CALL -> "${inputs[0].value}(${inputs.drop(1).joinToString(separator = ", ", transform = Input::value)});${if (comment?.isNotEmpty() == true) " // $comment\n" else "" }"
            Op.INFIX -> "${inputs[0].value} ${inputs[1].value} ${inputs[2].value};${if (comment?.isNotEmpty() == true) " // $comment\n" else "" }"
            Op.UNARY -> "${inputs[0].value} ${inputs[1].value};${if (comment?.isNotEmpty() == true) " // $comment\n" else "" }"
        }}"""

        fun iterToString(): String = toString() + "\n" + nextElements.joinToString(separator = "\n") { script?.getElementById(it)?.iterToString()?:"" }
    }

    data class Input(
        var type: String,
        var value: String,
        var comment: String?
    )

//    data class Output(
//        var id: ID
//    )

    data class Parameter(
        var type: String,
        var name: String,
        var comment: String?
    )

    data class Return(
        var type: String,
        var comment: String?
    )

    fun getElementById(id: ID) = elements.find { it.id == id }

    override fun toString(): String =
        """${if (comment?.isNotEmpty() == true) "/**\n$comment\n */\n" else "" }public class $name {
            |${variables.joinToString(separator = "\n") { "\t$it" } }
            |${functions.joinToString(separator = "\n") { "$it" } }
            |}
        """.trimMargin()
}
