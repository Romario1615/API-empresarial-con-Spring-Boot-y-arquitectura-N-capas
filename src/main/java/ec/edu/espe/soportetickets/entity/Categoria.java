package ec.edu.espe.soportetickets.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import ec.edu.espe.soportetickets.entity.enums.*;

@Entity
@Table(name = "categorias", uniqueConstraints = @UniqueConstraint(name = "uk_categorias_nombre", columnNames = "nombre"))
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80, unique = true)
    private String nombre;

    @Column(nullable = true, length = 255)
    private String descripcion;

    @jakarta.validation.constraints.Positive
    @Column(nullable = false)
    private int tiempoRespuestaHoras;

    @OneToMany(mappedBy = "categoria")
    private List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "especialidad")
    private List<Tecnico> tecnicos = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getTiempoRespuestaHoras() { return tiempoRespuestaHoras; }
    public void setTiempoRespuestaHoras(int tiempoRespuestaHoras) { this.tiempoRespuestaHoras = tiempoRespuestaHoras; }

    public List<Ticket> getTickets() { return tickets; }
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }

    public List<Tecnico> getTecnicos() { return tecnicos; }
    public void setTecnicos(List<Tecnico> tecnicos) { this.tecnicos = tecnicos; }


}

