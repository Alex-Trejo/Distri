package publicaciones.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import publicaciones.dto.ClientTimeDto;

import java.time.Instant;

@Service
@Slf4j
public class ClockProducer {
    @Autowired
    private AmqpTemplate amqpTemplate;

    //@Autowired
    //private ObjectMapper objectMapper;

    private final String nodeName = "ms-publish";

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
