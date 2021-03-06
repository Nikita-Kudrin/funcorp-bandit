package com.funcorp.bandit.integrationtests

import com.funcorp.bandit.content.model.ContentEvent
import com.funcorp.bandit.content.model.EventType
import com.funcorp.bandit.content.model.FAKE_VIEW_DATE
import com.funcorp.bandit.content.service.BanditContentService
import com.funcorp.bandit.extensions.toDate
import com.funcorp.bandit.generators.ContentGenerator
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import java.util.*

@SpringBootTest
class BanditContentServiceTest @Autowired constructor(private val contentService: BanditContentService) {
    companion object {
        private val log = LoggerFactory.getLogger(BanditContentServiceTest::class.java)
    }

    // TODO: Just examples of tests. Not complete coverage though.

    @Test
    fun insertContentTest() {
        val expectedContent = ContentGenerator.generateValidContent()
        contentService.insert(expectedContent).shouldBeTrue()
    }

    @Test
    fun retrieveContentTest() {
        val expectedContent = ContentGenerator.generateValidContent()
        contentService.insert(expectedContent)
        val storedContent = contentService.getById(expectedContent.id)
        storedContent.get().shouldBe(expectedContent)
    }

    @Test
    fun addViewToContentTest() {
        val expectedContent = ContentGenerator.generateValidContent()
        val userId = UUID.randomUUID().toString()
        val view = ContentEvent(userId, EventType.VIEW, Instant.now().epochSecond.toDate())

        contentService.let {
            it.insert(expectedContent)
            it.addView(expectedContent.id, userId, watchedOn = view.eventTime.toInstant().epochSecond.toString())
        }
            .get()
            .shouldBe(expectedContent.apply {
                views.putIfAbsent(userId, view)
                attempts++
            })
    }

    @Test
    fun addFakeViewToContentTest() {
        val expectedContent = ContentGenerator.generateValidContent()
        val userId = UUID.randomUUID().toString()
        val view = ContentEvent(userId, EventType.VIEW, FAKE_VIEW_DATE)

        val content = contentService.let {
            it.insert(expectedContent)
            it.addView(expectedContent.id, userId, watchedOn = "")
        }.get()

        log.info(content.toString())

        assertSoftly {
            content
                .shouldBe(expectedContent.apply {
                    views.putIfAbsent(userId, view)
                    attempts++
                })
        }
    }

    @Test
    fun addLikeToContentTest() {
        val expectedContent = ContentGenerator.generateValidContent()
        val userId = UUID.randomUUID().toString()
        val like = ContentEvent(userId, EventType.LIKE, Instant.now().epochSecond.toDate())

        contentService.let {
            it.insert(expectedContent)
            it.addLike(expectedContent.id, userId, likedOn = like.eventTime.toInstant().epochSecond.toString())
        }
            .get()
            .shouldBe(expectedContent.apply { likes.putIfAbsent(userId, like) })
    }
}