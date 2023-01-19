package com.geno1024.gglang.se

import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.annotation.JSONField
import java.io.File

typealias ID = String

data class ScriptDS(
    var name: String,
    var id: ID,
    var variables: List<Variable>,
    var functions: List<Function>,
    var elements: List<Element>
) {
    companion object
    {
        fun from(input: String): ScriptDS = JSONObject.parseObject(input, ScriptDS::class.java)

        fun from(file: File): ScriptDS = from(file.readText())
    }

    data class Variable(
        var id: ID,
        var name: String,
        var type: String,
        var initialValue: Any?
    ) {
        override fun toString(): String = "public static $type $name${if (initialValue != null) " = $initialValue" else ""};"
    }

    data class Function(
        var id: ID,
        var name: String,
        var parameters: List<Parameter>,
        var returns: List<Return>, // 暂时不接受多元组
        @JSONField(name = "first_element") var firstElement: ID
    ) {
        override fun toString(): String = """public static ${returns[0].type} $name {
            |}
        """.trimMargin()
    }

    data class Element(
        var id: ID,
        var name: String,
        var type: String,
        var op: Op,
        var inputs: List<Input>
    ) {
        enum class Op
        {
            ARRAY_INDEX, // name = input0[input1]
        }

        override fun toString(): String = """$type $name = ${when (op)
        {
            Op.ARRAY_INDEX -> "${inputs[0].value}[${inputs[1].value}]"
        }};"""
    }

    data class Input(
        var type: String,
        var value: String
    )

    data class Parameter(
        var type: String,
        var name: String
    )

    data class Return(
        var type: String
    )

    data class Output(
        var id: ID
    )

    override fun toString(): String =
        """public class $name {
            |${variables.joinToString(separator = "\n") { "\t$it" } }
            |${functions.joinToString(separator = "\n") { "$it" } }
            |}
        """.trimMargin()
}
