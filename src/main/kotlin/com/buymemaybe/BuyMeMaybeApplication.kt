package com.buymemaybe

import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableTransactionManagement
class BuyMeMaybeApplication

fun main(args: Array<String>) {
	runApplication<BuyMeMaybeApplication>(*args) {
		setBannerMode(Banner.Mode.OFF)
	}
}
