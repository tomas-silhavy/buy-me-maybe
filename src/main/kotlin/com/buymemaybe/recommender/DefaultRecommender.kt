package com.buymemaybe.recommender

import com.buymemaybe.db.Tables.CUSTOMER_FAV_ITEM
import com.buymemaybe.db.Tables.ITEM
import com.buymemaybe.db.Tables.ORDER
import com.buymemaybe.db.Tables.ORDER_ITEM
import org.jooq.impl.DSL.count
import org.jooq.impl.DefaultDSLContext
import org.springframework.stereotype.Component

@Component
class DefaultRecommender(dsl: DefaultDSLContext) : AbstractRecommender(dsl) {

    companion object {
        //should be in DB
        const val FAVOURITES_BOOST = 0.6
    }

    override fun getRecommendedItems(customerId: Int): Map<Int, Double> {
        val (weight, limit, active) = with(getConfig()) { Triple(weight, limit, active) }
        if (!active) {
            return emptyMap()
        }
        val prevBuys = dsl.select(ORDER_ITEM.ITEM_ID, count(ORDER_ITEM.ITEM_ID))
            .from(ORDER_ITEM)
            .join(ORDER).on(ORDER.ID.eq(ORDER_ITEM.ORDER_ID))
            .join(ITEM).on(ITEM.ID.eq(ORDER_ITEM.ITEM_ID))
            .where(ITEM.QUANTITY.greaterThan(0).and(ORDER.CUSTOMER_ID.eq(customerId)))
            .groupBy(ORDER_ITEM.ITEM_ID)
            .fetch { it.value1() to it.value2() }
            .sortedByDescending { it.second }

        val max = prevBuys.firstOrNull()?.second?.toDouble() ?: 0.0
        val prevBuysMap = prevBuys.associateBy({ it.first }, { it.second / max }).toMutableMap()

        dsl.select(ITEM.ID)
            .from(ITEM)
            .join(CUSTOMER_FAV_ITEM).on(ITEM.ID.eq(CUSTOMER_FAV_ITEM.ITEM_ID))
            .where(ITEM.QUANTITY.greaterThan(0).and(CUSTOMER_FAV_ITEM.CUSTOMER_ID.eq(customerId)))
            .fetch { (id) ->
                //we boost the previously bought if they are also saved as favourites
                prevBuysMap[id] = prevBuysMap[id]?.let { maxOf(1.0, FAVOURITES_BOOST + it) } ?: FAVOURITES_BOOST
            }
        return prevBuysMap.toList()
            .sortedByDescending { it.second }
            .take(limit)
            .associateBy({ it.first }, { it.second * weight })
    }
}