package publicaciones.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "autor")
@Getter
@Setter
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, name = "nombre")
    private String nombre;

    @Column(nullable = false, length = 50, name = "apellido")
    private String apellido;

	@Column(name="email", nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false, unique = true)
    private String telefono;

    @Column(nullable = false, unique = true, length = 20)
    private String orcid;

    private String nacionalidad;
    private String institucion;



	public List<Libro> getLibros() {
		return libros;
	}

	public void setLibros(List<Libro> libros) {
		this.libros = libros;
	}

	@OneToMany(mappedBy = "autor" , cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
    private List<Libro> libros;
	
	@OneToMany(mappedBy = "autor" , cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<Articulo> articulos; 
}
