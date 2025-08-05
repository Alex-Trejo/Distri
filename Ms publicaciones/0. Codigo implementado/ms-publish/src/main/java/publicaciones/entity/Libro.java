package publicaciones.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "libro")
public class Libro extends Publicacion {

    private String genero;
    private int numPaginas;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autor")
	@JsonBackReference
    private Autor autor;

	public String getGenero() {
		return genero;
	}

	public void setGenero(String genero) {
		this.genero = genero;
	}

	public int getNumPaginas() {
		return numPaginas;
	}

	public void setNumPaginas(int numPaginas) {
		this.numPaginas = numPaginas;
	}

	public Autor getAutor() {
		return autor;
	}

	public void setAutor(Autor autor) {
		this.autor = autor;
	}

	public Libro() {
		this.genero = genero;
		this.numPaginas = numPaginas;
		this.autor = autor;
	}
    
}
