package com.buymemaybe.dto

import com.fasterxml.jackson.annotation.JsonIgnore

class Recommendation(
    private val itemId: Int,
    val weights: Map<String, Double>
) {
    fun rank() = weights.values.sum()

    operator fun plus(other: Recommendation?): Recommendation {
        require(itemId == other?.itemId ?: itemId) { "Only the same items can be summed" }
        return other?.let { Recommendation(itemId, weights + other.weights) } ?: this
    }
}