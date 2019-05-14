package com.brightercode.discoursefromtwitter.twitter

import com.danielasfregola.twitter4s.entities.{Tweet, UrlDetails}

object TweetUrlHelper {
  def tweetUrl(tweet: Tweet): Option[String] =
    tweet.user.map(_.screen_name).map { screenName =>
      s"https://twitter.com/$screenName/status/${tweet.id}"
    }

  def allUrlsFrom(tweet: Tweet): Set[UrlDetails] =
    (tweet.entities.toIterable.flatMap(_.urls) ++
      tweet.quoted_status.toIterable.flatMap(_.entities).flatMap(_.urls) ++
      tweet.retweeted_status.toIterable.flatMap(_.entities).flatMap(_.urls)).toSet
}
