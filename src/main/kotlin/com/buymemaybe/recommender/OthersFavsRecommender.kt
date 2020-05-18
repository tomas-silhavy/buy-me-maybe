package com.buymemaybe.recommender

import com.buymemaybe.db.Tables.CUSTOMER_FAV_ITEM
import com.buymemaybe.db.Tables.ITEM
import org.jooq.impl.DSL.count
import org.jooq.impl.DSL.countDistinct
import org.jooq.impl.DefaultDSLContext
import org.springframework.stereotype.Component

@Component
class OthersFavsRecommender(dsl: DefaultDSLContext): AbstractRecommender(dsl) {

    override fun getRecommendedItems(customerId: Int): Map<Int, Double> {
        val (weight, limit, active) = with(getConfig()) { Triple(weight, limit, active) }
        if (!active) {
            return emptyMap()
        }
        val totalFavs = dsl.select(countDistinct(CUSTOMER_FAV_ITEM.CUSTOMER_ID))
            .from(CUSTOMER_FAV_ITEM)
            .fetchOneInto(Int::class.java) ?: 0

        return if (totalFavs > 0) {
            val favs = dsl.select(CUSTOMER_FAV_ITEM.ITEM_ID, count(CUSTOMER_FAV_ITEM.ITEM_ID))
                .from(CUSTOMER_FAV_ITEM)
                .join(ITEM).on(ITEM.ID.eq(CUSTOMER_FAV_ITEM.ITEM_ID))
                .where(ITEM.QUANTITY.greaterThan(0))
                .groupBy(CUSTOMER_FAV_ITEM.ITEM_ID)
                .fetch { row ->
                    row.value1() to row.value2().toDouble() / totalFavs
                }.sortedByDescending { it.second }
                .take(limit)

            //normalize to 0..1
            val max = favs.first().second
            favs.associateBy({ it.first }, { weight * it.second / max })
        } else emptyMap()
    }
}