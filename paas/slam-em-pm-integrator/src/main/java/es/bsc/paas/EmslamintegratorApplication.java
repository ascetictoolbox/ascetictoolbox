package es.bsc.paas;

import org.apache.activemq.broker.region.Queue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

@SpringBootApplication
@EnableAutoConfiguration
@EnableJms
public class EmslamintegratorApplication {

	public static final void main(String[] args) {
		SpringApplication.run(EmslamintegratorApplication.class, args);
    }
}

