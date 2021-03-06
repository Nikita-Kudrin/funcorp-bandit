package com.funcorp.bandit.content.controller

import com.funcorp.bandit.content.model.Content
import com.funcorp.bandit.content.service.BanditContentService
import com.funcorp.bandit.extensions.toDate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/content")
class ContentController @Autowired constructor(private val contentService: BanditContentService) {
    companion object {
        private val log = LoggerFactory.getLogger(ContentController::class.java)
    }

    @PostMapping(value = ["/add"])
    fun add(
        @RequestParam("id") id: String,
        @RequestParam("timestamp") timestamp: String
    ): ResponseEntity<String> {
        val content = Content(id, timestamp.toDate())

        if (!contentService.insert(content))
            return ResponseEntity("Content with id '$id' already exist", HttpStatus.CONFLICT)

        log.debug("Content has been added: $content")
        return ResponseEntity(HttpStatus.CREATED)
    }

    @PostMapping(value = ["/{id}/views/add"])
    fun addView(
        @PathVariable("id") id: String,
        @RequestParam("userId") userId: String,
        @RequestParam("watchedOn") watchedOn: String
    ): ResponseEntity<String> {
        val optional = contentService.addView(id, userId, watchedOn)

        if (!optional.isPresent)
            return ResponseEntity("Content with id $id doesn't exist", HttpStatus.NO_CONTENT)

        log.debug("View has been added to content: $id for user $userId")
        return ResponseEntity(HttpStatus.OK)
    }

    @PostMapping(value = ["/{id}/likes/add"])
    fun addLike(
        @PathVariable("id") id: String,
        @RequestParam("userId") userId: String,
        @RequestParam("likedOn") likedOn: String
    ): ResponseEntity<String> {
        val optional = contentService.addLike(id, userId, likedOn)

        if (!optional.isPresent)
            return ResponseEntity("Content with id $id doesn't exit", HttpStatus.NO_CONTENT)

        log.debug("Like has been added to content: $id for user $userId")
        return ResponseEntity(HttpStatus.OK)
    }

    @GetMapping(value = ["/{id}"])
    fun get(@PathVariable("id") id: String): ResponseEntity<Content> {
        val content = contentService.getById(id)

        return when (content.isPresent) {
            true -> ResponseEntity(content.get(), HttpStatus.OK)
            else -> ResponseEntity(HttpStatus.NO_CONTENT)
        }
    }

    // TODO: add paging
    @GetMapping(value = ["/all"], produces = ["application/json"])
    fun getAllContent(): List<Content> {
        return contentService.getAll().sortedByDescending { it.statisticalScore }
    }
}