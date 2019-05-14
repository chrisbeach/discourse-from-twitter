package com.brightercode.discoursefromtwitter

import com.brightercode.discoursefromtwitter.config.AppConfig
import com.brightercode.discoursefromtwitter.config.AppConfig._
import com.brightercode.discoursefromtwitter.discourse.TopicUrlParsing.{TopicReference, topicReferencesIn}
import com.brightercode.discoursefromtwitter.discourse.{AsyncForum, TopicFactory}
import com.brightercode.discoursefromtwitter.twitter.TweetUrlHelper.allUrlsFrom
import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.danielasfregola.twitter4s.http.clients.streaming.TwitterStream
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.concurrent.duration._


// TODO: If tweet contains link to topic, post share info on topic
object Runner extends App with LazyLogging {

  val twitterStream = TwitterStreamingClient()
  val forum = new AsyncForum(discourseEndpointConfig)

  implicit lazy val errorHandler: PartialFunction[Throwable, Unit] = {
    case e: Exception =>
      logger.error(e.getMessage, e)
      System.exit(1)
  }

  val stream = Await.result(tweetStream(), 60 seconds)
  logger.info(s"Established Twitter stream")

  //{"title"=>"", "raw"=>"", "category"=>49, "api_key"=>"[FILTERED]", "api_username"=>"se23_tweets"}
//  forum.enqueue(TopicTemplate(
//    title = "RT @love_se4: One of those hidden gems in the local community, you just have to try. Over 170 different gins. Proper mixologists who know h…",
//    raw = "https://twitter.com/se23_tweets/status/1127663613412630529",
//    49
//  ))

  private def tweetStream(): Future[TwitterStream] = {
    logger.info(s"Establishing tweet stream, " +
      s"tracks=[${twitterConfig.tracks.mkString(", ")}], " +
      s"follow=[${twitterConfig.follow.mkString(", ")}]")

    twitterStream.filterStatuses(tracks = twitterConfig.tracks, follow = twitterConfig.follow)({
      case tweet: Tweet =>
        val referencedTopics = topicReferencesIn(allUrlsFrom(tweet).map(_.expanded_url))
        logger.info(s"Tweet: $tweet referenced topics: $referencedTopics")
        if (referencedTopics.isEmpty) {
          enqueueTopicFrom(tweet)
        }
        //else {
//          referencedTopics.foreach { topic =>
//            enqueueMentionPostFrom(tweet, topic)
//          }
//        }
      case other =>
        logger.trace(other.toString)
    }, errorHandler)
  }

  private def enqueueTopicFrom(tweet: Tweet): Unit =
    TopicFactory.topicFrom(tweet, AppConfig.categoryId) match {
      case Some(topic) =>
        logger.trace(s"Enqueuing $topic")
        forum.enqueue(topic)
      case None =>
        logger.warn(s"Couldn't create topic from tweet: $tweet")
    }

  private def enqueueMentionPostFrom(tweet: Tweet, topicReference: TopicReference): Unit =
    TopicFactory.topicFrom(tweet, categoryId) match {
      case Some(topic) =>
        logger.trace(s"Enqueuing $topic")
        forum.enqueue(topic)
      case None =>
        logger.warn(s"Couldn't create topic from tweet: $tweet")
    }
}
