package com.geno1024.gglang.se

import com.geno1024.gglang.se.v1.ScriptDS
import java.io.File

object Main
{
    @JvmStatic
    fun main(args: Array<String>)
    {
        println(ScriptDS.from(File("ScriptEngine/src/test/ggscript/branch.json")))

    }
}
