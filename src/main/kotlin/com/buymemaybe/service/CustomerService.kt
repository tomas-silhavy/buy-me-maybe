package com.buymemaybe.service

import com.buymemaybe.db.Tables.CUSTOMER_FAV_ITEM
import com.buymemaybe.db.Tables.ITEM
import com.buymemaybe.db.tables.pojos.Item
import com.buymemaybe.dto.Recommendation
import com.buymemaybe.dto.RecommendedItem
import com.buymemaybe.recommender.AbstractRecommender
import org.jooq.DSLContext
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class CustomerService(
    private val context: ApplicationContext,
    private val dsl: DSLContext
) {

    fun getRecommendedItems(customerId: Int): List<RecommendedItem> {

        val recommendations = mutableMapOf<Int, Recommendation>()

        getRecommenders().map { (name, recommender) ->
            recommender.getRecommendedItems(customerId)
                .forEach { (itemId, weight) ->
                    recommendations.compute(itemId) { _, oldRec ->
                        Recommendation(itemId, mapOf(name to weight)) + oldRec
                    }
                }
        }
        return getItems(recommendations.keys)
            .map { RecommendedItem(recommendations.getValue(it.id), it) }
            .sortedByDescending { it.rank }
    }

    internal fun getRecommenders(): Map<String, AbstractRecommender> {
        return context.getBeansOfType(AbstractRecommender::class.java)
    }

    /*
     * V ramci demo aplikace jsem vynechal explicitni DAO vrstvu, ktera by pri vetsim projektu byla zadouci
     * To se tyka hlavne metod nize
     */

    internal fun getItems(itemIds: Collection<Int>): List<Item> {
        return dsl.selectFrom(ITEM)
            .where(ITEM.ID.`in`(itemIds))
            .fetchInto(Item::class.java)
    }

    fun getFavouriteItems(customerId: Int): List<Item> {
        return dsl.select(*ITEM.fields())
            .from(ITEM)
            .join(CUSTOMER_FAV_ITEM).on(ITEM.ID.eq(CUSTOMER_FAV_ITEM.ITEM_ID))
            .where(CUSTOMER_FAV_ITEM.CUSTOMER_ID.eq(customerId))
            .fetchInto(Item::class.java)
    }

    fun addFavouriteItem(customerId: Int, itemId: Int) {
        dsl.insertInto(CUSTOMER_FAV_ITEM)
            .set(CUSTOMER_FAV_ITEM.CUSTOMER_ID, customerId)
            .set(CUSTOMER_FAV_ITEM.ITEM_ID, itemId)
            .onDuplicateKeyIgnore()
            .execute()
    }

    fun removeFavouriteItem(customerId: Int, itemId: Int) {
        dsl.deleteFrom(CUSTOMER_FAV_ITEM)
            .where(CUSTOMER_FAV_ITEM.CUSTOMER_ID.eq(customerId).and(CUSTOMER_FAV_ITEM.ITEM_ID.eq(itemId)))
            .execute()
    }
}