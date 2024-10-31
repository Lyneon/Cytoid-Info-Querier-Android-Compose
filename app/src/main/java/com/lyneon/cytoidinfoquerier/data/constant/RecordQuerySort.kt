package com.lyneon.cytoidinfoquerier.data.constant

import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R

enum class RecordQuerySort {
    Score,
    Accuracy,
    Date,
    Rating,
    RecentRating;

    companion object {
        val RecordQuerySort.displayName: String
            get() = when (this) {
                Score -> BaseApplication.context.getString(R.string.score)
                Accuracy -> BaseApplication.context.getString(R.string.accuracy)
                Date -> BaseApplication.context.getString(R.string.play_time)
                Rating -> BaseApplication.context.getString(R.string.rating)
                RecentRating -> BaseApplication.context.getString(R.string.recent_rating)
            }
    }
}