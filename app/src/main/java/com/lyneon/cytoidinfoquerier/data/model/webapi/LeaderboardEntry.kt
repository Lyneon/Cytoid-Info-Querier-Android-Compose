package com.lyneon.cytoidinfoquerier.data.model.webapi

import kotlinx.serialization.Serializable

/**
 * Json example:
 * [
 *   {
 *     "id": "7c16727c-39b1-447d-ac46-b623698852a2",
 *     "uid": "skisk",
 *     "name": null,
 *     "avatar": {
 *       "original": "https://assets.cytoid.io/avatar/auMYbnehA4GYyjyi16WNih0cl2LUqzgJZyZHwSNgxniWd72dCuNNKmUilUJtPJVFdA",
 *       "small": "https://images.cytoid.io/avatar/auMYbnehA4GYyjyi16WNih0cl2LUqzgJZyZHwSNgxniWd72dCuNNKmUilUJtPJVFdA?h=64&w=64",
 *       "medium": "https://images.cytoid.io/avatar/auMYbnehA4GYyjyi16WNih0cl2LUqzgJZyZHwSNgxniWd72dCuNNKmUilUJtPJVFdA?h=128&w=128",
 *       "large": "https://images.cytoid.io/avatar/auMYbnehA4GYyjyi16WNih0cl2LUqzgJZyZHwSNgxniWd72dCuNNKmUilUJtPJVFdA?h=256&w=256"
 *     },
 *     "rating": 15.8686648,
 *     "rank": 1
 *   }
 * ]
 */

@Serializable
data class LeaderboardEntry(
    val id: String,
    val uid: String,
    val avatar: Avatar,
    val rating: Double,
    val rank: Int
) {
    @Serializable
    data class Avatar(
        val original: String,
        val small: String,
        val medium: String,
        val large: String
    )
}
