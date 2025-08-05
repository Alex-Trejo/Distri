package espe.edu.ec.catalogo.service;

import espe.edu.ec.catalogo.dto.CatalogDto;
import espe.edu.ec.catalogo.entity.Catalog;
import espe.edu.ec.catalogo.repository.CatalogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CatalogService {

    @Autowired
    private CatalogRepository catalogRepository;

    @Transactional
    public void saveEntity(CatalogDto dto) {
        Catalog catalog = new Catalog();
        catalog.setMensaje(dto.getMensaje());
        catalog.setTipo(dto.getTipo());
        catalog.setFecha(LocalDateTime.now());

        catalogRepository.save(catalog);
    }

    public List<Catalog> getAll() {
        return catalogRepository.findAll();
    }
}
