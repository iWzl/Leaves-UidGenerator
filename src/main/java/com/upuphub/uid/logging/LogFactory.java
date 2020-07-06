package com.upuphub.uid.logging;

import com.upuphub.uid.logging.commons.JakartaCommonsLoggingImpl;
import com.upuphub.uid.logging.jdk14.Jdk14LoggingImpl;
import com.upuphub.uid.logging.log4j.Log4jImpl;
import com.upuphub.uid.logging.log4j2.Log4j2Impl;
import com.upuphub.uid.logging.nologging.NoLoggingImpl;
import com.upuphub.uid.logging.slf4j.Slf4jImpl;
import com.upuphub.uid.logging.stdout.StdOutImpl;

import java.lang.reflect.Constructor;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public final class LogFactory {

  /**
   * Marker to be used by logging implementations that support markers.
   */
  public static final String MARKER = "LEAVES-UID";

  private static Constructor<? extends Log> logConstructor;

  static {
    tryImplementation(LogFactory::useSlf4jLogging);
    tryImplementation(LogFactory::useCommonsLogging);
    tryImplementation(LogFactory::useLog4J2Logging);
    tryImplementation(LogFactory::useLog4JLogging);
    tryImplementation(LogFactory::useJdkLogging);
    tryImplementation(LogFactory::useNoLogging);
  }

  private LogFactory() {
    // disable construction
  }

  public static Log getLog(Class<?> clazz) {
    return getLog(clazz.getName());
  }

  public static Log getLog(String logger) {
    try {
      return logConstructor.newInstance(logger);
    } catch (Throwable t) {
      throw new LogException("Error creating logger for logger " + logger + ".  Cause: " + t, t);
    }
  }

  public static synchronized void useCustomLogging(Class<? extends Log> clazz) {
    setImplementation(clazz);
  }

  public static synchronized void useSlf4jLogging() {
    setImplementation(Slf4jImpl.class);
  }

  public static synchronized void useCommonsLogging() {
    setImplementation(JakartaCommonsLoggingImpl.class);
  }

  public static synchronized void useLog4JLogging() {
    setImplementation(Log4jImpl.class);
  }

  public static synchronized void useLog4J2Logging() {
    setImplementation(Log4j2Impl.class);
  }

  public static synchronized void useJdkLogging() {
    setImplementation(Jdk14LoggingImpl.class);
  }

  public static synchronized void useStdOutLogging() {
    setImplementation(StdOutImpl.class);
  }

  public static synchronized void useNoLogging() {
    setImplementation(NoLoggingImpl.class);
  }

  private static void tryImplementation(Runnable runnable) {
    if (logConstructor == null) {
      try {
        runnable.run();
      } catch (Throwable t) {
        // ignore
      }
    }
  }

  private static void setImplementation(Class<? extends Log> implClass) {
    try {
      Constructor<? extends Log> candidate = implClass.getConstructor(String.class);
      Log log = candidate.newInstance(LogFactory.class.getName());
      if (log.isDebugEnabled()) {
        log.debug("Logging initialized using '" + implClass + "' adapter.");
      }
      logConstructor = candidate;
    } catch (Throwable t) {
      throw new LogException("Error setting Log implementation.  Cause: " + t, t);
    }
  }

}
