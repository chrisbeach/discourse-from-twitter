package com.brightercode.discoursefromtwitter.config

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._

trait AppConfig {
  private val allConfig: Config = ConfigFactory.load()
  val discourseConfig: Config = allConfig.getConfig("discourse")
  val twitterConfig: Config = allConfig.getConfig("twitter")
  val categoryId: Int = discourseConfig.getInt("categoryId")
  val tracks: Seq[String] = twitterConfig.getStringList("tracks").asScala
  val follow: Seq[scala.Long] = twitterConfig.getLongList("follow").asScala.map(_.longValue())
}
