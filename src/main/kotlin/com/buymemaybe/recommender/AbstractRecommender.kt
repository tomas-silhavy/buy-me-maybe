package com.buymemaybe.recommender

import com.buymemaybe.db.Tables.RECOMMENDER
import com.buymemaybe.db.tables.pojos.Recommender
import org.jooq.DSLContext

abstract class AbstractRecommender(internal val dsl: DSLContext) {

    fun getConfig(): Recommender {
        return dsl.selectFrom(RECOMMENDER)
            .where(RECOMMENDER.CLAZZ.eq(this::class.qualifiedName))
            .fetchSingleInto(Recommender::class.java)
    }

    abstract fun getRecommendedItems(customerId: Int): Map<Int, Double>
}