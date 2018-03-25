package springbootmq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("springbootmq")
@SpringBootApplication
//定制自己的ConnectionFactory，RabbitTemplate等对象，在Configuration中禁用RabbitAutoConfiguration
@EnableAutoConfiguration(exclude = RabbitAutoConfiguration.class)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
