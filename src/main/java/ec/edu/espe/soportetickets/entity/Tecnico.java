package ec.edu.espe.soportetickets.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import ec.edu.espe.soportetickets.entity.enums.*;

@Entity
@Table(name = "tecnicos", uniqueConstraints = @UniqueConstraint(name = "uk_tecnicos_email", columnNames = "email"))
public class Tecnico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 254, unique = true)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "especialidad_id", nullable = true, foreignKey = @ForeignKey(name = "fk_tecnicos_especialidad"))
    private Categoria especialidad;

    @Column(nullable = false)
    private boolean activo = true;

    @OneToMany(mappedBy = "tecnico")
    private List<Ticket> tickets = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Categoria getEspecialidad() { return especialidad; }
    public void setEspecialidad(Categoria especialidad) { this.especialidad = especialidad; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public List<Ticket> getTickets() { return tickets; }
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }


}

