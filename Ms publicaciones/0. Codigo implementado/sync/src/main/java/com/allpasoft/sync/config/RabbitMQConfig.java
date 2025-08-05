package com.allpasoft.sync.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.MessageConverter; // Importar


@Configuration
public class RabbitMQConfig {

    public static final String CLOCK_REQUEST_QUEUE = "reloj.solicitd";
    public static final String AJUSTES_RELOJ_QUEUE = "reloj.ajustment";

    @Bean
    public Queue solicitud() {
        return QueueBuilder.durable(CLOCK_REQUEST_QUEUE).build();
    }


    @Bean
    public Queue ajustesRelojQueue() {
        return QueueBuilder.durable(AJUSTES_RELOJ_QUEUE).build();
    }


    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }


}
