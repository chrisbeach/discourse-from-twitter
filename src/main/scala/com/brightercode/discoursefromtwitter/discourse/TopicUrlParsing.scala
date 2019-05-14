package com.brightercode.discoursefromtwitter.discourse
import java.util.regex.Pattern

import com.brightercode.discoursefromtwitter.config.AppConfig.discourseEndpointConfig

import scala.util.matching.Regex

// TODO parse username from query string
object TopicUrlParsing {
  private val TopicUrlPrefix = Pattern.quote(s"${discourseEndpointConfig.baseUrl}/t/")

  /**
    * Example URL: https://se23.life/t/12084/2?u=chrisbeach
    */
  private val TopicIdAndPostIdRegex: Regex =
    s"$TopicUrlPrefix(\\d+)\\/(\\d+)(\\?.*)".r

  /**
    * Example URL: https://se23.life/t/open-gardens/12084/2?u=chrisbeach
    */
  private val TopicIdAndSlugAndPostIdRegex: Regex =
    s"$TopicUrlPrefix([^/]+)\\/(\\d+)\\/(\\d+)(\\?.*)".r

  /**
    * Example URL: https://se23.life/t/open-gardens/12084?u=chrisbeach
    */
  private val TopicIdAndSlugRegex: Regex =
    s"$TopicUrlPrefix([^/]+)\\/(\\d+)(\\?.*)".r

  case class TopicReference(topicId: TopicId,
                            slug: Option[String] = None,
                            postId: Option[PostId] = None)

  def topicReferencesIn(urls: Set[String]): Set[TopicReference] =
    urls.collect {
      case TopicIdAndPostIdRegex(topicId, postId, _) =>
        TopicReference(topicId.toInt, None, Some(postId.toInt))
      case TopicIdAndSlugAndPostIdRegex(slug, topicId, postId, _) =>
        TopicReference(topicId.toInt, Some(slug), Some(postId.toInt))
      case TopicIdAndSlugRegex(slug, topicId, _) =>
        TopicReference(topicId.toInt, Some(slug), None)
    }

  type TopicId = Long
  type PostId = Long
}
