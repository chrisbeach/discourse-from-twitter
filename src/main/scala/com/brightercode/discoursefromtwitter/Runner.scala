package com.brightercode.discoursefromtwitter

import akka.actor.ActorSystem
import cats.effect.concurrent.MVar
import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.brightercode.discourse.DiscourseForumApiClient.{withDiscourseForum, discourseForumResource}
import com.brightercode.discourse.apidecorators.AsyncTopicPoster
import com.brightercode.discourse.model.{Topic, TopicTemplate}
import com.brightercode.discourse.util.TypesafeConfigHelper._
import com.brightercode.discoursefromtwitter.config.AppConfig
import com.brightercode.discoursefromtwitter.discourse.TopicFactory.topicFrom
import com.brightercode.discoursefromtwitter.util.ErrorHandlers.exitOnError
import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.danielasfregola.twitter4s.http.clients.streaming.TwitterStream
import com.typesafe.scalalogging.LazyLogging

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
    discourseForumResource(discourseConfig, system).use { forum =>
      val topicPoster = new AsyncTopicPoster(forum.topics)
      for {
        topicChannel <- MVar[IO].empty[TopicTemplate]
        topicPostingFiber <- topicPoster.loop(topicChannel).start
        tweetStream <- withEveryStreamedTweet { tweet =>
          topicFrom(tweet, categoryId)
            .map(topicChannel.put)
            .getOrElse(logger.warn(s"Couldn't create topic from tweet: $tweet"))
        }
        _ <- IO(logger.info(s"Started stream: $tweetStream"))
        _ <- topicPostingFiber.join
      } yield ExitCode.Success
    }

  private def withEveryStreamedTweet(operation: Tweet => Any): IO[TwitterStream] = {
    logger.info(s"Establishing tweet stream, tracks=[${tracks.mkString(", ")}], follow=[${follow.mkString(", ")}]")
    IO.fromFuture { IO {
      twitter.filterStatuses(tracks = tracks, follow = follow, stall_warnings = true)({
        case tweet: Tweet => operation(tweet)
        case other => logger.debug(other.toString)
      }, errorHandler = exitOnError)
    }}
  }
}