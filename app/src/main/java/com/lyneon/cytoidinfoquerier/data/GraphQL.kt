package com.lyneon.cytoidinfoquerier.data

object GraphQL {
    fun getQueryString(query: String): String = """{"operationName":null,"variables":{},"query":"${
        query.replace("\n", "\\n").replace("\"", "\\\"")
    }"}"""
}