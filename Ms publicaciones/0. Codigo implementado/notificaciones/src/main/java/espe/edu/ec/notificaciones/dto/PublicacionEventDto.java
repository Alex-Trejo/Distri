package espe.edu.ec.notificaciones.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PublicacionEventDto implements Serializable {
    private String id;
    private String titulo;
    private String tipo;
    private LocalDateTime fechaEvento;
}
