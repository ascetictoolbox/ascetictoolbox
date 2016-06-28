package es.bsc.paas

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

@RunWith(SpringJUnit4ClassRunner::class)
@SpringApplicationConfiguration(classes = arrayOf(EmslamintegratorApplication::class))
class EmslamintegratorApplicationTests {

	@Test
	fun contextLoads() {
	}

}
