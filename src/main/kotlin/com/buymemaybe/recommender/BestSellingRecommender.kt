package com.buymemaybe.recommender

import com.buymemaybe.db.Tables.ITEM
import com.buymemaybe.db.Tables.ORDER
import com.buymemaybe.db.Tables.ORDER_ITEM
import org.jooq.impl.DSL.count
import org.jooq.impl.DefaultDSLContext
import org.springframework.stereotype.Component

@Component
class BestSellingRecommender(dsl: DefaultDSLContext) : AbstractRecommender(dsl) {

    override fun getRecommendedItems(customerId: Int): Map<Int, Double> {
        val (weight, limit, active) = with(getConfig()) { Triple(weight, limit, active) }
        if (!active) {
            return emptyMap()
        }

        val totalOrders = dsl.fetchCount(ORDER)
        val list = dsl.select(ORDER_ITEM.ITEM_ID, count(ORDER_ITEM.ITEM_ID))
            .from(ORDER_ITEM)
            .join(ITEM).on(ITEM.ID.eq(ORDER_ITEM.ITEM_ID))
            .where(ITEM.QUANTITY.greaterThan(0))
            .groupBy(ORDER_ITEM.ITEM_ID)
            .fetch { row ->
                row.value1() to row.value2().toDouble() / totalOrders
            }.sortedByDescending { it.second }
            .take(limit)

        return if (list.isNotEmpty()) {
            val max = list.first().second
            list.associateBy({ it.first }, { weight * it.second / max })
        } else emptyMap()
    }
}