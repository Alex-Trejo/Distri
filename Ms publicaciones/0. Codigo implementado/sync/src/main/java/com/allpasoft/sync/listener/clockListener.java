package com.allpasoft.sync.listener;

import com.allpasoft.sync.dto.ClientTimeDto;
import com.allpasoft.sync.services.SyncService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.handler.annotation.Payload; // IMPORTAR @Payload

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class clockListener {

    @Autowired
    private SyncService syncService;

    //@Autowired
    //private ObjectMapper objectMapper;

    @RabbitListener(queues = "reloj.solicitd")
    public void receiveMessage(@Payload ClientTimeDto dto) {
        log.info("Mensaje recibido y deserializado automáticamente a DTO: {}", dto); // Usar el log
        try {
            //ClientTimeDto dto = objectMapper.readValue(jsonMessage, ClientTimeDto.class);
            syncService.registerClientTime(dto);
            //System.out.println(dto);
            //syncService.registerClientTime(dto);
            log.info("Tiempo del cliente {} registrado exitosamente.", dto.getNodeName()); // Usar el log
        } catch (Exception e) {
            //System.err.println("Failed to process message: " + jsonMessage);
            log.error("Error al procesar el mensaje del reloj para el DTO: {}", dto, e);
            //e.printStackTrace();
        }
    }
}
