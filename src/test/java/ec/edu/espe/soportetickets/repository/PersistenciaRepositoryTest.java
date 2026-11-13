package ec.edu.espe.soportetickets.repository;

import ec.edu.espe.soportetickets.entity.*;
import ec.edu.espe.soportetickets.entity.enums.*;
import ec.edu.espe.soportetickets.support.IntegrationTestBase;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Persistencia JPA y consultas personalizadas")
class PersistenciaRepositoryTest extends IntegrationTestBase {
    @Test @DisplayName("Flush y clear conservan campos, relaciones y código definitivo")
    void persistirYReleer() {
        ticket.setTecnico(tecnico);
        Observacion observacion = new Observacion();
        observacion.setAutor("Ana"); observacion.setContenido("Diagnóstico inicial");
        ticket.agregarObservacion(observacion);
        ticketRepository.saveAndFlush(ticket);
        Long id = ticket.getId();
        entityManager.clear();
        Ticket releido = ticketRepository.findById(id).orElseThrow();
        assertThat(releido.getCodigo()).isEqualTo("TCK-%06d".formatted(id));
        assertThat(releido.getTitulo()).isEqualTo("Falla de equipo");
        assertThat(releido.getDescripcion()).isEqualTo("El equipo no enciende");
        assertThat(releido.getPrioridad()).isEqualTo(Prioridad.MEDIA);
        assertThat(releido.getEstado()).isEqualTo(EstadoTicket.ABIERTO);
        assertThat(releido.getFechaCreacion()).isNotNull();
        assertThat(releido.getFechaActualizacion()).isNotNull();
        assertThat(releido.getFechaCierre()).isNull();
        assertThat(releido.getUsuario().getEmail()).isEqualTo("ana@espe.edu.ec");
        assertThat(releido.getCategoria().getNombre()).isEqualTo("Hardware");
        assertThat(releido.getTecnico().getNombres()).isEqualTo("Luis");
        assertThat(releido.getObservaciones()).singleElement().satisfies(o -> {
            assertThat(o.getContenido()).isEqualTo("Diagnóstico inicial");
            assertThat(o.getFechaRegistro()).isNotNull();
        });
    }

    @Test @DisplayName("Orphan removal elimina una observación retirada del ticket")
    void eliminarObservacionHuerfana() {
        Observacion observacion = new Observacion();
        observacion.setAutor("Ana"); observacion.setContenido("Temporal");
        ticket.agregarObservacion(observacion);
        Ticket guardado = ticketRepository.saveAndFlush(ticket);
        Long observacionId = guardado.getObservaciones().getFirst().getId();
        assertThat(observacionId).isNotNull();
        guardado.getObservaciones().clear(); ticketRepository.flush(); entityManager.clear();
        assertThat(observacionRepository.findById(observacionId)).isEmpty();
    }

    @Test @DisplayName("Las consultas derivadas detectan duplicados sin distinguir mayúsculas")
    void duplicados() {
        assertThat(usuarioRepository.existsByEmailIgnoreCase("ANA@ESPE.EDU.EC")).isTrue();
        assertThat(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("ana@espe.edu.ec", usuario.getId())).isFalse();
        assertThat(tecnicoRepository.existsByEmailIgnoreCase("LUIS@ESPE.EDU.EC")).isTrue();
        assertThat(categoriaRepository.existsByNombreIgnoreCase("HARDWARE")).isTrue();
        assertThat(ticketRepository.existsByCategoriaId(categoria.getId())).isTrue();
    }

    @Test @DisplayName("A igual prioridad los pendientes más antiguos aparecen primero")
    void prioridadYFecha() {
        Ticket segundo = nuevoTicket(EstadoTicket.ABIERTO, Prioridad.MEDIA, null);
        // El primer ticket se insertó antes; el ID desempata fechas de igual precisión.
        var pendientes = ticketRepository.buscarPendientesOrdenados(
                java.util.List.of(EstadoTicket.ABIERTO, EstadoTicket.EN_PROCESO));
        assertThat(pendientes).extracting(Ticket::getId).containsExactly(ticket.getId(), segundo.getId());
    }
}

