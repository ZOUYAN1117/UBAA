package cn.edu.ubaa.auth

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.future.await

/** 管理共享 Redis 连接的 Cookie 存储工厂。 所有 [RedisCookieStorage] 实例共用同一个 Redis 连接，避免为每个用户创建独立连接导致资源泄漏。 */
class RedisCookieStorageFactory(private val redisUri: String) : ManagedCookieStorageFactory {
  private val client: RedisClient by lazy { RedisClient.create(redisUri) }
  private val connection: StatefulRedisConnection<String, String> by lazy { client.connect() }
  private val commands: RedisAsyncCommands<String, String> by lazy { connection.async() }

  override fun create(subject: String): ManagedCookieStorage = RedisCookieStorage(commands, subject)

  override suspend fun clearSubject(subject: String) {
    commands.del(RedisCookieStorage.storageKey(subject)).await()
  }

  override fun close() {
    runCatching { connection.close() }
    runCatching { client.shutdown() }
  }
}
