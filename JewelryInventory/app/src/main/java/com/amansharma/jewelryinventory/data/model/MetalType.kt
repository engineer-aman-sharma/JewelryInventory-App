package com.amansharma.jewelryinventory.data.model

enum class MetalType(val displayName: String) {
    YELLOW_GOLD("Yellow Gold"),
    WHITE_GOLD("White Gold"),
    ROSE_GOLD("Rose Gold"),
    SILVER("Silver"),
    PLATINUM("Platinum"),
    NOT_APPLICABLE("N/A");

    companion object {
        fun fromDisplayName(value: String): MetalType =
            entries.firstOrNull { it.displayName.equals(value, ignoreCase = true) }
                ?: entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: NOT_APPLICABLE
    }
}
