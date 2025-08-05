package espe.edu.ec.notificaciones.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory; // IMPORTAR
import org.springframework.amqp.rabbit.core.RabbitTemplate; // IMPORTAR
import org.springframework.amqp.support.converter.MessageConverter; // IMPORTAR



@Configuration
public class RabbitMQConfig {

    public static final String NOTIFICATIONS_QUEUE = "notificaciones.cola";
    public static final String CLOCK_ADJUSTMENT_QUEUE = "reloj.ajustment";
    public static final String CLOCK_REQUEST_QUEUE = "reloj.solicitd";



    @Bean
    public Queue notificacionesQueue() {
        return QueueBuilder.durable(NOTIFICATIONS_QUEUE).build();
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