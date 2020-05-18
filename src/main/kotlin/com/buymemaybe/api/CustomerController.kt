package com.buymemaybe.api

import com.buymemaybe.db.tables.pojos.Item
import com.buymemaybe.dto.RecommendedItem
import com.buymemaybe.service.CustomerService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("customer", produces = [MediaType.APPLICATION_JSON_VALUE])
@RestController
class CustomerController(
    private val customerService: CustomerService
) {

    @GetMapping("recommend/{customer-id}", "/{customer-id}/{limit}")
    fun suggestItems(
        @PathVariable("customer-id") customerId: Int,
        @PathVariable("limit", required = false) limit: Int?
    ): List<RecommendedItem> {
        return customerService.getRecommendedItems(customerId).take(limit ?: 10)
    }

    @GetMapping("fav/{customer-id}")
    fun listFavourite(
        @PathVariable("customer-id") customerId: Int
    ): List<Item> {
        return customerService.getFavouriteItems(customerId)
    }

    @PostMapping("fav/{customer-id}/{item-id}")
    fun addFavourite(
        @PathVariable("customer-id") customerId: Int,
        @PathVariable("item-id") itemId: Int
    ) {
        customerService.addFavouriteItem(customerId, itemId)
    }

    @DeleteMapping("fav/{customer-id}/{item-id}")
    fun removeFavourite(
        @PathVariable("customer-id") customerId: Int,
        @PathVariable("item-id") itemId: Int
    ) {
        customerService.removeFavouriteItem(customerId, itemId)
    }

}