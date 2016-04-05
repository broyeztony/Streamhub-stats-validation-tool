package com.example

import org.slf4j.LoggerFactory

object InsightUncaughtExceptionHandler
  extends Thread.UncaughtExceptionHandler  {

  def logger = LoggerFactory.getLogger(this.getClass)
  override def uncaughtException(thread: Thread, exception: Throwable) {
    logger.error("Insights > Uncaught exception in thread " + thread, exception)

    exception match {
      case oom: OutOfMemoryError =>
        logger.error("Insights > UncaughtException - OutOfMemoryError. ", oom)
        oom.printStackTrace()
      case t: Throwable =>
        logger.error("Insights > UncaughtException - Some Exception. ", t)
        t.printStackTrace()
    }
  }

  def uncaughtException(exception: Throwable) {
    uncaughtException(Thread.currentThread(), exception)
  }
}
