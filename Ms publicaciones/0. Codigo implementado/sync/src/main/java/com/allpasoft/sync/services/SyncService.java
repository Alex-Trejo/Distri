package com.allpasoft.sync.services;

import com.allpasoft.sync.dto.AdjustmentDto;
import com.allpasoft.sync.dto.ClientTimeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j; // Para los logs

@Service
@Slf4j
public class SyncService {
    private final Map<String, Long> clientTimes = new ConcurrentHashMap<>();
    @Autowired
    private AmqpTemplate amqpTemplate;

    //@Autowired
    //private ObjectMapper objectMapper;

    private static final int INTERVAL_SECONDS = 10;

    public void registerClientTime(ClientTimeDto dto) {
        log.info("Registrando tiempo de cliente: {}", dto); // Usar el log
        clientTimes.put(dto.getNodeName(), dto.getServerTime());
    }

    public void synchronizeClocks() {
        if (clientTimes.size() >= 2) {
            long currentTime = Instant.now().toEpochMilli();
            long averageTime = (currentTime + clientTimes.values().stream().mapToLong(Long::longValue).sum())
                    / (clientTimes.size() + 1);
            clientTimes.clear();
            applyAdjustment(averageTime);
            log.info("Sincronización completada. Tiempo promedio: {}", averageTime); // Usar el log

        }
    }

    public void applyAdjustment(long averageTime) {

        try {
            AdjustmentDto adjustmentDto = new AdjustmentDto(averageTime);
            //amqpTemplate.convertAndSend("reloj.ajustment", objectMapper.writeValueAsString(adjustmentDto));
            amqpTemplate.convertAndSend("reloj.ajustment", adjustmentDto);
            log.info("Ajuste de tiempo difundido: {}", adjustmentDto.getAjusteMillis()); // Usar el log
        } catch (Exception e) {
            log.error("Error al difundir el ajuste de tiempo: {}", e.getMessage(), e); // Usar el log
            //e.printStackTrace();
        }


        System.out.println("Adjusted time (ms): " + averageTime);
    }
}
