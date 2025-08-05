package publicaciones.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CatalogoDto implements Serializable {
	private String mensaje; // Esto contendrá el título, autor y resumen combinado
	private String tipo;    // Esto contendrá el tipo de evento (ej: "nuevo-libro", "libro actualizado")
}
