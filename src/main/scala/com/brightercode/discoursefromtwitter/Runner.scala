package com.brightercode.discoursefromtwitter

import akka.actor.ActorSystem
import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import com.brightercode.discourse.DiscourseForumApiClient.withDiscourseForum
import com.brightercode.discourse.apidecorators.TopicApiAsync
import com.brightercode.discourse.util.TypesafeConfigHelper._
import com.brightercode.discoursefromtwitter.config.AppConfig
import com.brightercode.discoursefromtwitter.discourse.TopicFactory.topicFrom
import com.brightercode.discoursefromtwitter.util.ErrorHandlers.exitOnError
import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.danielasfregola.twitter4s.http.clients.streaming.TwitterStream
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

/**
 * Streams tweets from Twitter and posts topics to a Discourse forum
 *
 * Configured by application.conf (see [[AppConfig]])
 */
object Runner extends IOApp with LazyLogging with AppConfig {

  private val system = ActorSystem("twitter")
  private val twitter: TwitterStreamingClient = TwitterStreamingClient.withActorSystem(system)

  override def run(args: List[String]): IO[ExitCode] =
    IO {
      withDiscourseForum(discourseConfig, system) { forum =>
        val topicCreatorAsync =  new TopicApiAsync(forum.topics)
        Await.result(
          withEveryStreamedTweet { tweet =>
            topicFrom(tweet, categoryId)
              .map(topicCreatorAsync.enqueue)
              .getOrElse(logger.warn(s"Couldn't create topic from tweet: $tweet"))
          },
          atMost = 60 seconds
        )
        logger.info(s"Twitter stream initialised")
        Await.result(system.whenTerminated, atMost = Duration.Inf)
      }
    }.as(ExitCode.Success)

  private def withEveryStreamedTweet(operation: Tweet => Any): Future[TwitterStream] = {
    logger.info(s"Establishing tweet stream, tracks=[${tracks.mkString(", ")}], follow=[${follow.mkString(", ")}]")
    twitter.filterStatuses(tracks = tracks, follow = follow)({
      case tweet: Tweet => operation(tweet)
      case other => logger.trace(other.toString)
    }, errorHandler = exitOnError)
  }
}