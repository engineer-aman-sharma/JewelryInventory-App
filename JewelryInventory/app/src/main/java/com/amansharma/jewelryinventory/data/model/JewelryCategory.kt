package com.amansharma.jewelryinventory.data.model

enum class JewelryCategory(val displayName: String) {
    RING("Ring"),
    NECKLACE("Necklace"),
    BRACELET("Bracelet"),
    EARRING("Earring"),
    LOOSE_STONE("Loose Stone"),
    COIN("Coin"),
    WATCH("Watch"),
    OTHER("Other");

    companion object {
        fun fromDisplayName(value: String): JewelryCategory =
            entries.firstOrNull { it.displayName.equals(value, ignoreCase = true) }
                ?: entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: OTHER
    }
}
