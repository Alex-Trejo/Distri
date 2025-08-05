package espe.edu.ec.catalogo.listener;

import com.fasterxml.jackson.databind.ObjectMapper;

import espe.edu.ec.catalogo.dto.AdjustmentDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class ClockAdjustmentListener {

    //private final ObjectMapper objectMapper = new ObjectMapper();

    private volatile long currentAdjustment = 0L;


    @RabbitListener(queues = "reloj.ajustment")
    public void recibirAjuste(@Payload AdjustmentDto dto) {
        try {
            // 'dto.getAjusteMillis()' es la hora promedio calculada por el servidor de sincronización.
            long receivedAverageTime = dto.getAjusteMillis();

            // Capturamos la hora local de este microservicio justo en el momento de recibir el mensaje.
            long localTimeAtReception = Instant.now().toEpochMilli();

            // Calculamos la diferencia que este nodo debe aplicar a su reloj local.
            // La lógica es: "Para llegar a la hora promedio, ¿cuánto necesito sumar/restar a mi hora actual?"
            // ajuste = (hora_promedio - mi_hora_local_al_recibir)
            this.currentAdjustment = receivedAverageTime - localTimeAtReception;

            log.info("Ajuste de reloj recibido. Hora promedio esperada: {} ms", receivedAverageTime);
            log.info("Hora local del nodo al recibir el ajuste: {} ms", localTimeAtReception);
            log.info("Este nodo aplicará un ajuste de: {} ms", this.currentAdjustment);

            // Opcional: imprimir la hora sincronizada inmediatamente para verificación
            log.info("Hora actual SINCRONIZADA de este nodo: {} ms", getAdjustedCurrentTime());

        } catch (Exception e) {
            log.error("Error al procesar el DTO de ajuste de reloj: {}", dto, e);
        }
    }

    /**
     * Proporciona la hora actual del sistema, pero ajustada por la última sincronización.
     * Cualquier parte de este microservicio que necesite la "hora correcta/sincronizada"
     * debería llamar a este método en lugar de `System.currentTimeMillis()`.
     *
     * @return La hora actual en milisegundos, corregida por el ajuste de sincronización.
     */
    public long getAdjustedCurrentTime() {
        return Instant.now().toEpochMilli() + this.currentAdjustment;
    }

}