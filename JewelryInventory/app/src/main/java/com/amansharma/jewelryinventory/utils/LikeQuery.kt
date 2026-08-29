package com.amansharma.jewelryinventory.utils

object LikeQuery {
    fun escape(raw: String): String {
        return raw.trim()
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
    }
}
