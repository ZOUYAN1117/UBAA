package cn.edu.ubaa.auth

import io.github.cdimascio.dotenv.dotenv
import java.time.Duration

/** 统一管理认证相关运行时配置。 */
object AuthConfig {
  private val dotenv = dotenv { ignoreIfMissing = true }

  val redisUri: String = env("REDIS_URI") ?: "redis://localhost:6379"

  val accessTokenTtl: Duration =
      Duration.ofMinutes(env("ACCESS_TOKEN_TTL_MINUTES")?.toLongOrNull() ?: 30L)

  val refreshTokenTtl: Duration =
      Duration.ofDays(env("REFRESH_TOKEN_TTL_DAYS")?.toLongOrNull() ?: 7L)

  val sessionTtl: Duration = Duration.ofDays(env("SESSION_TTL_DAYS")?.toLongOrNull() ?: 7L)

  val loginMaxConcurrency: Int = env("LOGIN_MAX_CONCURRENCY")?.toIntOrNull()?.coerceAtLeast(1) ?: 6

  val validationTimeoutMillis: Long =
      env("AUTH_VALIDATION_TIMEOUT_MS")?.toLongOrNull()?.coerceAtLeast(1L) ?: 3_000L

  val preloadTimeoutMillis: Long =
      env("AUTH_PRELOAD_TIMEOUT_MS")?.toLongOrNull()?.coerceAtLeast(1L) ?: 3_000L

  val loginTimeoutMillis: Long =
      env("AUTH_LOGIN_TIMEOUT_MS")?.toLongOrNull()?.coerceAtLeast(1L) ?: 18_000L

  private fun env(name: String): String? = dotenv[name] ?: System.getenv(name)
}
