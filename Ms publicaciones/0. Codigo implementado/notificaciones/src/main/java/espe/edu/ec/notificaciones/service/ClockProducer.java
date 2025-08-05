package espe.edu.ec.notificaciones.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import espe.edu.ec.notificaciones.dto.ClientTimeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class ClockProducer {
    @Autowired
    private AmqpTemplate amqpTemplate;

    //@Autowired
    //private ObjectMapper objectMapper;

    private final String nodeName = "notification";

    public void sendTime() {
        try {
            ClientTimeDto clientTimeDto = new ClientTimeDto(nodeName, Instant.now().toEpochMilli());
            log.info("Enviando ClientTimeDto a reloj.solicitd: {}", clientTimeDto);
            //amqpTemplate.convertAndSend("reloj.solicitd", objectMapper.writeValueAsString(clientTimeDto));
            amqpTemplate.convertAndSend("reloj.solicitd", clientTimeDto);
        } catch (Exception e) {
            log.error("Error al enviar tiempo de reloj: {}", e.getMessage(), e); // Usar el log
            //e.printStackTrace();
        }
    }
}
