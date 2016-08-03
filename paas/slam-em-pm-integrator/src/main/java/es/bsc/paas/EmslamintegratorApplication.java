package es.bsc.paas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * This class just defines the application entry point
 */
@SpringBootApplication
@EnableAutoConfiguration
@EnableJms
@EnableScheduling
public class EmslamintegratorApplication {
    public static final void main(String[] args) {
        SpringApplication.run(EmslamintegratorApplication.class, args);
    }
}

