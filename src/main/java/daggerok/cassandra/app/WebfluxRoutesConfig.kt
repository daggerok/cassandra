package daggerok.cassandra.app

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Configuration
class WebfluxRoutesConfig {

  @Bean
  fun webClient() = WebClient.create()

  val stopHandler: (ServerRequest) -> Mono<ServerResponse> = {
    val url = it.uri().toURL()
    ServerResponse.ok().body(
        webClient()
            .mutate()
            .baseUrl("${url.protocol}://${url.host}:${url.port}" + "/cassandra/shutdown")
            .build()
            .post()
            .exchange()
            .subscribeOn(Schedulers.elastic())
            .flatMap { it.bodyToMono<Any>() }
    )
  }

  val fallbackHandler: (ServerRequest) -> Mono<ServerResponse> = {
    val url = it.uri().toURL()
    ServerResponse.ok().body(
        webClient()
            .mutate()
            .baseUrl("${url.protocol}://${url.host}:${url.port}" + "/cassandra/")
            .build()
            .get()
            .exchange()
            .subscribeOn(Schedulers.elastic())
            .flatMap { it.bodyToMono<Any>() }
    )
  }

  @Bean
  fun routes() = router {
    ("/").nest {
      contentType(MediaType.APPLICATION_JSON_UTF8)

      mapOf(

          "/stop*" to stopHandler,
          "/**" to fallbackHandler

      ).forEach { path, handler ->

        GET(path, handler)
        POST(path, handler)
        PUT(path, handler)
        DELETE(path, handler)
        PATCH(path, handler)
        HEAD(path, handler)
      }
    }
  }
}
