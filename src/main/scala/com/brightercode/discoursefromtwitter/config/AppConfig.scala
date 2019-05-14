package com.brightercode.discoursefromtwitter.config

import com.brightercode.discourse.DiscourseEndpointConfig
import com.brightercode.discourse.util.TypesafeConfigHelper._
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._

object AppConfig {
  private val allConfig: Config = ConfigFactory.load()

  val twitterConfig: TwitterConfig = allConfig.getConfig("twitter")
  val discourseEndpointConfig: DiscourseEndpointConfig = allConfig.getConfig("discourse.endpoint")
  val categoryId: Int = allConfig.getInt("discourse.categoryId")

  implicit class TwitterConfig(config: Config) {
    val tracks: Seq[String] = config.getStringList("tracks").asScala
    val follow: Seq[scala.Long] = config.getLongList("follow").asScala.map(_.longValue())
  }
}
