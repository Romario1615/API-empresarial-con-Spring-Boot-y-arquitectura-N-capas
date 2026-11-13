package ec.edu.espe.soportetickets.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import ec.edu.espe.soportetickets.entity.enums.*;

@Entity
@Table(name = "auditoria")
public class RegistroAuditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String metodo;

    @Column(nullable = false)
    private long duracionMs;

    @Column(nullable = false, length = 5)
    private String resultado;

    @Column(nullable = true, length = 500)
    private String mensajeError;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }

    public long getDuracionMs() { return duracionMs; }
    public void setDuracionMs(long duracionMs) { this.duracionMs = duracionMs; }

    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }

    public String getMensajeError() { return mensajeError; }
    public void setMensajeError(String mensajeError) { this.mensajeError = mensajeError; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    @PrePersist
    void alRegistrar() {
        if (fecha == null) fecha = LocalDateTime.now();
    }
}

