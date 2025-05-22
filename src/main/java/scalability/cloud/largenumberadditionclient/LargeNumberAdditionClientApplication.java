package scalability.cloud.largenumberadditionclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@SpringBootApplication
public class LargeNumberAdditionClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(LargeNumberAdditionClientApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.connectTimeout(Duration.ofMinutes(200))
                .readTimeout(Duration.ofMinutes(200))
                .build();
    }

}
