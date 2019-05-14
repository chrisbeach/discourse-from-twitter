package com.brightercode.discoursefromtwitter.discourse

import java.util
import java.util.concurrent.LinkedBlockingQueue

import com.brightercode.discourse.DiscourseForumApiClient.withDiscourseForum
import com.brightercode.discourse.exceptions.RateLimitException
import com.brightercode.discourse.model.TopicTemplate
import com.brightercode.discourse.{DiscourseEndpointConfig, DiscourseForumApiClient}
import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

/**
  * Posts topics to Discourse forum asynchronously
  *
  * @param topicQueueSize maximum number of topics to queue
  * @param ec execution context on which to run background thread
  */
class AsyncForum(config: DiscourseEndpointConfig,
                 topicQueueSize: Int = 1000)
                (implicit ec: ExecutionContext) extends LazyLogging {

  private val queue = new LinkedBlockingQueue[TopicTemplate](topicQueueSize)

  def enqueue(topic: TopicTemplate): Unit = queue.put(topic)

  Future {
    Thread.currentThread().setName("Async forum")
    withDiscourseForum(config) { forum =>
      while (true) {
        try {
          val outstandingTopics = new util.ArrayList[TopicTemplate]()
          queue.drainTo(outstandingTopics)
          createTopics(forum, outstandingTopics.asScala.toList)
        } catch {
          case e: RateLimitException => sleepForRateLimit(e)
          case e: Exception => logger.warn(e.getMessage, e)
          case e: Throwable => logger.error(e.getMessage, e)
        }
      }
    }
    logger.error("Thread unexpectedly died, exiting app")
    System.exit(1)
  }

  private def createTopics(forum: DiscourseForumApiClient, topics: List[TopicTemplate]): Unit =
    if (topics.nonEmpty) {
      logger.debug(s"${topics.size} topic(s) outstanding. Taking first.")
      topics.foreach { topic =>
        try {
          val createdTopic = Await.result(forum.topics.create(topic), 10 seconds)
          logger.info(createdTopic.toString)
        } catch {
          case e: RateLimitException => throw e
          case e: Exception => logger.error(e.getMessage, e)
        }
      }
      Thread.sleep(5000)
    } else {
      Thread.sleep(1000)
    }

  private def sleepForRateLimit(e: RateLimitException): Unit = {
    logger.warn(e.getMessage, e)
    e.waitSecs match {
      case Some(secs) =>
        logger.warn(s"Pausing forum posts for $secs seconds")
        Thread.sleep(secs * 1000)
      case _ => logger.error("No wait time specified in RateLimitException")
    }
  }
}
