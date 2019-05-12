package com.brightercode.discoursefromtwitter.twitter

import com.danielasfregola.twitter4s.entities.Tweet

object TweetUrlHelper {
  def tweetUrl(tweet: Tweet): Option[String] =
    tweet.user.map(_.screen_name).map { screenName =>
      s"https://twitter.com/$screenName/status/${tweet.id}"
    }
}
