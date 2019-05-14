package com.brightercode.discoursefromtwitter.discourse

import com.brightercode.discoursefromtwitter.discourse.TopicUrlParsing.TopicReference
import org.scalatest.{Matchers, WordSpec}

class TopicUrlParsingTest extends WordSpec with Matchers {
  "Parser" should {
    "identify topic id and slug" in {
      TopicUrlParsing.topicReferencesIn(Set("https://se23.life/t/open-gardens/12084?u=chrisbeach")) should be(
        Set(TopicReference(12084, slug = Some("open-gardens")))
      )
    }

    "identify topic id, slug and post id" in {
      TopicUrlParsing.topicReferencesIn(Set("https://se23.life/t/open-gardens/12084/2?u=chrisbeach")) should be(
        Set(TopicReference(12084, slug = Some("open-gardens"), postId = Some(2)))
      )
    }

    "identify topic id and post id" in {
      TopicUrlParsing.topicReferencesIn(Set("https://se23.life/t/12084/2?u=chrisbeach")) should be(
        Set(TopicReference(12084, slug = None, postId = Some(2)))
      )
    }
  }
}
