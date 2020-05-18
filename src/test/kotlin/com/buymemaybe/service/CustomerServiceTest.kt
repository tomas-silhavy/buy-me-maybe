package com.buymemaybe.service

import com.buymemaybe.db.tables.pojos.Item
import com.buymemaybe.recommender.AbstractRecommender
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.random.Random

@ExtendWith(SpringExtension::class)
internal class CustomerServiceTest {

    @SpyBean
    lateinit var customerService: CustomerService

    @MockBean
    lateinit var dsl: DSLContext

    lateinit var recommendations1: Map<Int, Double>
    lateinit var recommendations2: Map<Int, Map<Int, Double>>
    private val w1 = 0.7
    private val w2 = 0.3


    @BeforeEach
    fun setUp() {
        recommendations1 = mapOf(
            1 to 1.0,
            2 to 0.7,
            3 to 0.6,
            4 to 0.5
        )
        recommendations2 = mapOf(
            1 to mapOf(
                3 to 1.0,
                5 to 0.5
            ),
            2 to mapOf(3 to -1.0)
        )

        whenever(customerService.getRecommenders()).thenReturn(mapOf(
            "recommender1" to TestRecommender1(w1),
            "recommender2" to TestRecommender2(w2)
        ))
        doAnswer {
            it.getArgument(0, Collection::class.java)
                .map { id -> Item(id as Int, "item_$id", Random.nextDouble(), Random.nextInt()) }
        }.whenever(customerService).getItems(any())
    }

    @Test
    fun `test item gets promoted`() {
        val items = customerService.getRecommendedItems(1)

        assertThat(items).allSatisfy { it.rank <= 1 }
        assertThat(items.map { it.id }).containsExactly(3, 1, 2, 4, 5)
        assertThat(items.map { it.rank }).containsExactly(
            w1 * 0.6 + w2 * 1.0,
            w1 * 1.0,
            w1 * 0.7,
            w1 * 0.5,
            w2 * 0.5
        )
    }

    @Test
    fun `test item gets demoted`() {
        val items = customerService.getRecommendedItems(2)

        assertThat(items).allSatisfy { it.rank <= 1 }
        assertThat(items.map { it.id }).containsExactly(1, 2, 4, 3)
        assertThat(items.map { it.rank }).containsExactly(
            w1 * 1.0,
            w1 * 0.7,
            w1 * 0.5,
            w1 * 0.6 - w2 * 1.0
        )
    }

    inner class TestRecommender1(val weight: Double) : AbstractRecommender(dsl) {
        val name = this::class.java.simpleName.decapitalize()
        override fun getRecommendedItems(customerId: Int) = recommendations1.mapValues { it.value * weight }
    }

    inner class TestRecommender2(val weight: Double) : AbstractRecommender(dsl) {
        override fun getRecommendedItems(customerId: Int): Map<Int, Double> {
            return recommendations2.getValue(customerId).mapValues { it.value * weight }
        }
    }
}