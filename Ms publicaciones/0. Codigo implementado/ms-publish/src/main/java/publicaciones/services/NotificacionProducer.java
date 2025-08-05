package publicaciones.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import publicaciones.dto.NotificacionDto;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class NotificacionProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //@Autowired
    //private ObjectMapper objectMapper;

    public void enviarNotificacion(String mensaje, String tipo) {
        try{
            NotificacionDto notificacionDto = new NotificacionDto(mensaje, tipo);
            // --- ¡¡CAMBIO CLAVE!! Envía el OBJETO DTO directamente.
            // RabbitTemplate, con su converter, lo convertirá a JSON con el content-type correcto.
            rabbitTemplate.convertAndSend("notificaciones.cola", notificacionDto);
            log.info("Notificación enviada a notificaciones.cola: {}", notificacionDto);
        }catch(Exception e){
            log.error("Error al enviar notificación a RabbitMQ: {}", e.getMessage(), e);
        }
    }
}
