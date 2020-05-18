package com.buymemaybe.dto

import com.buymemaybe.db.tables.pojos.Item

class RecommendedItem(
    val recommendation: Recommendation,
    item: Item
) : Item(
    item.id,
    item.name,
    item.price,
    item.quantity
) {
    val rank = recommendation.rank()
}