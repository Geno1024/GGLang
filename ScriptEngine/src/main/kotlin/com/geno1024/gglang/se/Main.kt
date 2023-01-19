package com.geno1024.gglang.se

import java.io.File

object Main
{
    @JvmStatic
    fun main(args: Array<String>)
    {
        println(ScriptDS.from(File("ScriptEngine/src/test/ggscript/simple.json")))

    }
}
