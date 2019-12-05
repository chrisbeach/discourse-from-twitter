package com.brightercode.discoursefromtwitter.util

import com.typesafe.scalalogging.LazyLogging

object ErrorHandlers extends LazyLogging {
  val exitOnError: PartialFunction[Throwable, Unit] = {
    case e: Exception =>
      logger.error(e.getMessage, e)
      System.exit(1)
  }
}
