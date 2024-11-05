package com.lyneon.cytoidinfoquerier.data.constant

import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R

enum class SearchLevelSortingStrategy(val value: String) {
    CreationDate("creation_date"),
    ModificationDate("modification_date"),
    Duration("duration"),
    Downloads("downloads"),
    Plays("plays"),
    Rating("rating"),
    Difficulty("difficulty");

    companion object {
        val SearchLevelSortingStrategy.displayName: String
            get() = when (this) {
                CreationDate -> BaseApplication.context.getString(R.string.creation_date)
                ModificationDate -> BaseApplication.context.getString(R.string.modification_date)
                Duration -> BaseApplication.context.getString(R.string.song_duration)
                Downloads -> BaseApplication.context.getString(R.string.downloads_count)
                Plays -> BaseApplication.context.getString(R.string.plays_count)
                Rating -> BaseApplication.context.getString(R.string.level_rating)
                Difficulty -> BaseApplication.context.getString(R.string.difficulty)
            }
    }
}