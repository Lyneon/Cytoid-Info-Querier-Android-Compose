package com.lyneon.cytoidinfoquerier.data.constant

enum class RecordQuerySort(val displayName: String) {
    Score("分数"),
    Accuracy("精准度"),
    Date("游玩时间"),
    Rating("Rating"),
    RecentRating("Rating（60日内）")
}