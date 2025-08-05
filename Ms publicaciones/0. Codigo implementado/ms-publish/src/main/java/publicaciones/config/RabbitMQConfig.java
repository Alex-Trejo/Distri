package publicaciones.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String CLOCK_REQUEST_QUEUE = "reloj.solicitd";

    public static final String CLOCK_ADJUSTMENT_QUEUE = "reloj.ajustment"; // <-- Cola del listener
    public static final String CATALOG_QUEUE = "catalog.cola"; // <-- Cola del CatalogoProducer
    public static final String NOTIFICATIONS_QUEUE = "notificaciones.cola"; // <-- Cola del NotificacionProducer


    @Bean
    public Queue solicitudQueue() {
        return QueueBuilder.durable(CLOCK_REQUEST_QUEUE).build();
    }

    @Bean
    public Queue adjustmentQueue() {
        return QueueBuilder.durable(CLOCK_ADJUSTMENT_QUEUE).build();
    }

    @Bean
    public Queue catalogoQueue() {
        return QueueBuilder.durable(CATALOG_QUEUE).build();
    }

    @Bean
    public Queue notificacionesQueue() {
        return QueueBuilder.durable(NOTIFICATIONS_QUEUE).build();
    }




    @Bean
    public Queue solicitud() {
        return QueueBuilder.durable(CLOCK_REQUEST_QUEUE).build();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }



}
