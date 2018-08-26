package daggerok.cassandra.app

import info.archinnov.achilles.embedded.CassandraEmbeddedServer
import info.archinnov.achilles.embedded.CassandraEmbeddedServerBuilder
import info.archinnov.achilles.embedded.CassandraShutDownHook
import info.archinnov.achilles.type.TypedMap
import org.springframework.beans.factory.InitializingBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.io.File
import java.net.InetAddress
import java.net.NetworkInterface
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.annotation.PreDestroy

@Service
class EmbeddedCassandraServer(val env: Environment) : InitializingBean {

  companion object {
    private val now = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyMMddhhmmssSSS"))
    private val cassandraEmbeddedServerBuilder = CassandraEmbeddedServerBuilder
        .builder()
        .withConcurrentReads(16)
        .withConcurrentWrites(16)
        .withParams(TypedMap())
        .withClusterName("embedded-cassandra")
  }

  lateinit var cassandraShutDownHook: CassandraShutDownHook
  lateinit var server: CassandraEmbeddedServer

  override fun afterPropertiesSet() {

    val cleanDataFilesAtStartup = env.getProperty("cassandra.clean-data-files-at-startup", "false").toBoolean()
    cassandraEmbeddedServerBuilder
        .cleanDataFilesAtStartup(cleanDataFilesAtStartup)

    val prefix = if (cleanDataFilesAtStartup) "" else now
    val cassandraDir = ".cassandra/$prefix"

    if (cleanDataFilesAtStartup) {
      val dir = File(cassandraDir).normalize()
      dir.mkdirs()
      dir.deleteRecursively()
    }

    val dataPath = "$cassandraDir/data"
    File(dataPath).mkdirs()
    cassandraEmbeddedServerBuilder
        .withDataFolder(dataPath)

    val hintsPath = "$cassandraDir/hints"
    File(hintsPath).mkdirs()
    cassandraEmbeddedServerBuilder
        .withHintsFolder(hintsPath)

    val cdcRawPath = "$cassandraDir/cdc-raw"
    File(cdcRawPath).mkdirs()
    cassandraEmbeddedServerBuilder
        .withCdcRawFolder(cdcRawPath)

    val commitLogPath = "$cassandraDir/commit-log"
    File(commitLogPath).mkdirs()
    cassandraEmbeddedServerBuilder
        .withCommitLogFolder(commitLogPath)

    val savedCaches = "$cassandraDir/saved-caches"
    File(savedCaches).mkdirs()
    cassandraEmbeddedServerBuilder
        .withSavedCachesFolder(savedCaches)

    val addresses = addresses().filterNot { it.contains("127.0.0.1") }
    val address = if (addresses.isEmpty()) "127.0.0.1" else addresses.first()
    cassandraEmbeddedServerBuilder
        .withBroadcastRpcAddress(address)
        .withBroadcastAddress(address)
        .withRpcAddress(address)
        .withListenAddress(address)
    println("\n\nlisten address: $address\n")

    val cqlPort = env.getProperty("cassandra.port", "9042").toInt()
    cassandraEmbeddedServerBuilder
        //.withStorageSSLPort(7999)
        //.withStoragePort(7990)
        //.withThriftPort(9160)
        .withCQLPort(cqlPort)

    val keyspaceName = env.getProperty("cassandra.keyspace", "demo")
    cassandraEmbeddedServerBuilder
        .withKeyspaceName(keyspaceName)

    cassandraShutDownHook = CassandraShutDownHook()
    cassandraEmbeddedServerBuilder
        .withShutdownHook(cassandraShutDownHook)

    server = cassandraEmbeddedServerBuilder.buildServer()
  }

  fun addresses() = InetAddress
      .getAllByName(InetAddress.getLocalHost().canonicalHostName)
      .orEmpty()
      .filterNotNull()
      .toMutableSet()
      .plus(
          NetworkInterface
              .getNetworkInterfaces()
              .toList()
              .filterNotNull()
              .map { it.inetAddresses }
              .flatMap { it.toList() }
              .filterNotNull()
              .toTypedArray()
      )
      .map { it.hostAddress }
      .filterNot { it.contains("127.0.0.1") }
      .toSet()

  @PreDestroy
  fun destroy() {

    println("\nShutdown cassandra...\n")
    cassandraShutDownHook.shutDownNow()

    Thread {
      TimeUnit.SECONDS.sleep(3)
      println("\nDone.\n")
      System.exit(0)
    }.start()
  }
}
