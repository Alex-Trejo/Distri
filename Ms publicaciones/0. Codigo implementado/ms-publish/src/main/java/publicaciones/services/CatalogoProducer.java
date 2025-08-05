package publicaciones.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import publicaciones.dto.CatalogoDto;
import publicaciones.dto.NotificacionDto;

@Service
@Slf4j
public class CatalogoProducer {
	@Autowired
	private RabbitTemplate template;
	
	//@Autowired
	//private ObjectMapper mapper;

	public void enviarCatalogo(String titulo, String autor, String resumen, String tipoEvento) {
		try {
			// Combina la información en el campo 'mensaje' del CatalogoDto unificado
			String mensajeCombinado = String.format("Título: %s, Autor: %s, Resumen: %s", titulo, autor, resumen);

			// Crea el DTO del catálogo que coincide con el esperado por el consumidor
			CatalogoDto catalogoDto = new CatalogoDto(mensajeCombinado, tipoEvento);

			// --- ¡¡CAMBIO CLAVE!! Envía el OBJETO DTO directamente.
			// RabbitTemplate, con su converter, lo convertirá a JSON.
			template.convertAndSend("catalog.cola", catalogoDto);
			log.info("Mensaje de catálogo enviado a catalog.cola: {}", catalogoDto);
		} catch (Exception e) {
			log.error("Error al enviar mensaje de catálogo: {}", e.getMessage(), e);
		}
	}
}
