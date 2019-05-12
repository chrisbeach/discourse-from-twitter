package com.brightercode.discoursefromtwitter.discourse

import com.brightercode.discourse.model.TopicTemplate
import com.brightercode.discoursefromtwitter.twitter.TweetUrlHelper.tweetUrl
import com.danielasfregola.twitter4s.entities.Tweet

object TopicFactory {
  def topicFrom(tweet: Tweet, categoryId: Int): Option[TopicTemplate] =
    tweetUrl(tweet).map { url =>
      TopicTemplate(
        title = tweet.text,
        raw = url,
        categoryId = categoryId
      )
    }
}
