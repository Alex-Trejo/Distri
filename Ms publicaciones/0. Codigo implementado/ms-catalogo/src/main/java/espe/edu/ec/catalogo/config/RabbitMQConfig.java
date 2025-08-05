package espe.edu.ec.catalogo.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {


    public static final String CATALOG_QUEUE = "catalog.cola";
    public static final String CLOCK_ADJUSTMENT_QUEUE = "reloj.ajustment";
    public static final String CLOCK_REQUEST_QUEUE = "reloj.solicitd";

    @Bean
    public Queue catalogoQueue() {
        return QueueBuilder.durable(CATALOG_QUEUE).build();
    }

    @Bean
    public Queue adjustmentQueue() {
        return QueueBuilder.durable(CLOCK_ADJUSTMENT_QUEUE).build();
    }

    @Bean
    public Queue solicitudQueue() {
        return QueueBuilder.durable(CLOCK_REQUEST_QUEUE).build();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
