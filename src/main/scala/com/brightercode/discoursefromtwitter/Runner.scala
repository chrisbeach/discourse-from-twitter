package com.brightercode.discoursefromtwitter

import com.brightercode.discoursefromtwitter.config.AppConfig
import com.brightercode.discoursefromtwitter.discourse.{AsyncForum, TopicFactory}
import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps


// TODO: If tweet contains link to topic, post share info on topic
object Runner extends App with LazyLogging with AppConfig {

  val twitterStream = TwitterStreamingClient()
  val forum = new AsyncForum()

  tweetStream()

  implicit lazy val errorHandler: PartialFunction[Throwable, Unit] = {
    case e: Exception =>
      logger.error(e.getMessage, e)
      System.exit(1)
  }

  /**
    * Blocks until stream is complete
    */
  private def tweetStream() = {
    logger.info(s"Establishing tweet stream, " +
      s"tracks=[${tracks.mkString(", ")}], " +
      s"follow=[${follow.mkString(", ")}]")

    twitterStream.filterStatuses(tracks = tracks, follow = follow)({
      case tweet: Tweet => enqueueTopicFrom(tweet)
      case other => logger.trace(other.toString)
    }, errorHandler)
  }

  private def enqueueTopicFrom(tweet: Tweet): Unit =
    TopicFactory.topicFrom(tweet, categoryId) match {
      case Some(topic) =>
        logger.trace(s"Enqueuing $topic")
        forum.enqueue(topic)
      case None =>
        logger.warn(s"Couldn't create topic from tweet: $tweet")
    }
}
