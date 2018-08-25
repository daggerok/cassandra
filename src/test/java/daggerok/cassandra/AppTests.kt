package daggerok.cassandra

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [App::class], webEnvironment = RANDOM_PORT)
class AppTests {

  @Test
  fun contextLoads() {}
}
