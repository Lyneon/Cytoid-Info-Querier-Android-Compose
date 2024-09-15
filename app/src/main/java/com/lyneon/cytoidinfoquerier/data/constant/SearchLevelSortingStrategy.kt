package com.lyneon.cytoidinfoquerier.data.constant

enum class SearchLevelSortingStrategy(val value: String, val displayName: String) {
    CreationDate("creation_date", "创建日期"),
    ModificationDate("modification_date", "修改日期"),
    Duration("duration", "曲目长度"),
    Downloads("downloads", "下载次数"),
    Plays("plays", "游玩次数"),
    Rating("rating", "评分"),
    Difficulty("difficulty", "难度")
}