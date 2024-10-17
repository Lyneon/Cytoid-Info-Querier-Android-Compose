package com.lyneon.cytoidinfoquerier.data.enums

enum class AvatarSize(val value: String) {
    /**
     * Original size
     */
    Original("original"),

    /**
     * Large size, 256 * 256 px
     */
    Large("large"),

    /**
     * Medium size, 128 * 128 px
     */
    Medium("medium"),

    /**
     * Small size, 64 * 64 px
     */
    Small("small")
}

enum class ImageSize(val value: String) {
    /**
     * Thumbnail size, 360 * 576 px
     */
    Thumbnail("thumbnail"),

    /**
     * Original size
     */
    Original("original"),

    /**
     * Cover size, 800 * 1280 px
     */
    Cover("cover"),

    /**
     * Stripe size, 180 * 768 px
     */
    Stripe("stripe")
}