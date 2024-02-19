package com.lyneon.cytoidinfoquerier.model.graphql

object GraphQL {
    fun getQueryString(query: String): String = """{"operationName":null,"variables":{},"query":"${query.replace("\n", "\\n").replace("\"", "\\\"")}"}"""
}