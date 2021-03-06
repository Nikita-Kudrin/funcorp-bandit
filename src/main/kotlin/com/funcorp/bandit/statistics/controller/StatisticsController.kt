package com.funcorp.bandit.statistics.controller

import com.funcorp.bandit.algorithm.Ucb1Algorithm
import com.funcorp.bandit.content.service.BanditContentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/play")
class StatisticsController @Autowired constructor(private val contentService: BanditContentService) {

    companion object {
        private val ucb1Algorithm = Ucb1Algorithm()
    }

    @GetMapping(value = ["/{userId}"], produces = ["application/json"])
    fun play(@PathVariable("userId") userId: String): List<String> {
        // TODO: MongoTemplate and DynamicQuery to filter unnecessary content on DB side
        val contentItems = contentService.getAll().filter { !it.views.containsKey(userId) }

        val promisingItems = ucb1Algorithm.selectMostPromisingItems(contentItems, itemsCount = 10)

        // During N minutes after content was given to user it counts as viewed
        // After N minutes those "fake" views will be deleted
        promisingItems.forEach {
            contentService.apply {
                addView(contentId = it, userId = userId, watchedOn = "")
            }
        }

        return promisingItems
    }
}