package com.buymemaybe.recommender

import com.buymemaybe.db.Tables.CUSTOMER_FAV_ITEM
import com.buymemaybe.db.Tables.ITEM
import org.jooq.impl.DSL.count
import org.jooq.impl.DefaultDSLContext
import org.springframework.stereotype.Component

@Component
class NeighbouringRecommender(dsl: DefaultDSLContext): AbstractRecommender(dsl) {

    override fun getRecommendedItems(customerId: Int): Map<Int, Double> {
        val (weight, limit, active) = with(getConfig()) { Triple(weight, limit, active) }
        if (!active) {
            return emptyMap()
        }

        val userFavsIems = dsl.select(CUSTOMER_FAV_ITEM.ITEM_ID)
            .from(CUSTOMER_FAV_ITEM)
            .where(CUSTOMER_FAV_ITEM.CUSTOMER_ID.eq(customerId))
            .fetchInto(Int::class.java)

        val relatedUserItemsQuery = dsl.selectFrom(CUSTOMER_FAV_ITEM)
            .where(CUSTOMER_FAV_ITEM.CUSTOMER_ID.ne(customerId).and(CUSTOMER_FAV_ITEM.ITEM_ID.`in`(userFavsIems)))
            .asTable()

        val otherFavItems = dsl.select(CUSTOMER_FAV_ITEM.ITEM_ID, count(CUSTOMER_FAV_ITEM.ITEM_ID))
            .from(CUSTOMER_FAV_ITEM)
            .join(relatedUserItemsQuery).on(relatedUserItemsQuery.field(CUSTOMER_FAV_ITEM.CUSTOMER_ID).eq(CUSTOMER_FAV_ITEM.CUSTOMER_ID))
            .join(ITEM).on(ITEM.ID.eq(CUSTOMER_FAV_ITEM.ITEM_ID))
            .where(ITEM.QUANTITY.greaterThan(0).and(CUSTOMER_FAV_ITEM.ITEM_ID.notIn(userFavsIems)))
            .groupBy(CUSTOMER_FAV_ITEM.ITEM_ID)
            .fetch { it.value1() to it.value2() }
            .sortedByDescending { it.second }
            .take(limit)

        return if (otherFavItems.isNotEmpty()) {
            //normalize to 0..1
            val max = otherFavItems.first().second.toDouble()
            otherFavItems.associateBy({ it.first }, { weight * it.second / max })
        } else emptyMap()
    }
}