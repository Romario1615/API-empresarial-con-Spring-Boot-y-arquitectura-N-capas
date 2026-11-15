package ec.edu.espe.soportetickets.config;

import ec.edu.espe.soportetickets.entity.*;
import ec.edu.espe.soportetickets.entity.enums.*;
import ec.edu.espe.soportetickets.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Profile("!test")
@ConditionalOnProperty(name = "app.data-loader.enabled", havingValue = "true", matchIfMissing = true)
public class DataLoader implements CommandLineRunner {
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TecnicoRepository tecnicoRepository;
    private final TicketRepository ticketRepository;

    public DataLoader(CategoriaRepository categoriaRepository, UsuarioRepository usuarioRepository,
            TecnicoRepository tecnicoRepository, TicketRepository ticketRepository) {
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.ticketRepository = ticketRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categoriaRepository.count() != 0) return;
        List<Categoria> categorias = List.of(categoria("Hardware", 24), categoria("Software", 48),
                categoria("Redes", 8), categoria("Accesos y cuentas", 12));
        List<Usuario> usuarios = List.of(usuario("Ana Torres", "ana.torres@espe.edu.ec", "Administración", true),
                usuario("Carlos Pérez", "carlos.perez@espe.edu.ec", "Docencia", true),
                usuario("Lucía Vega", "lucia.vega@espe.edu.ec", "Biblioteca", true),
                usuario("Pedro Ruiz", "pedro.ruiz@espe.edu.ec", "Archivo", false));
        List<Tecnico> tecnicos = List.of(tecnico("Diego López", "diego.lopez@espe.edu.ec", categorias.get(0)),
                tecnico("María Salas", "maria.salas@espe.edu.ec", categorias.get(1)),
                tecnico("José Molina", "jose.molina@espe.edu.ec", categorias.get(2)));
        EstadoTicket[] estados = {EstadoTicket.ABIERTO, EstadoTicket.EN_PROCESO, EstadoTicket.RESUELTO,
                EstadoTicket.CERRADO, EstadoTicket.CANCELADO, EstadoTicket.ABIERTO};
        String[] titulos = {"Monitor sin imagen", "Error en aplicación académica", "Conexión de red restaurada",
                "Cuenta institucional recuperada", "Solicitud duplicada de teclado", "Impresora sin conexión"};
        Prioridad[] prioridades = {Prioridad.ALTA, Prioridad.CRITICA, Prioridad.MEDIA,
                Prioridad.BAJA, Prioridad.BAJA, Prioridad.MEDIA};
        for (int i = 0; i < estados.length; i++) {
            Ticket ticket = new Ticket();
            ticket.setCodigo("TMP-" + UUID.randomUUID());
            ticket.setTitulo(titulos[i]);
            ticket.setDescripcion("Solicitud de soporte: " + titulos[i]);
            ticket.setPrioridad(prioridades[i]);
            ticket.setEstado(estados[i]);
            ticket.setUsuario(usuarios.get(i % 3));
            ticket.setCategoria(categorias.get(i % 4));
            if (i > 0 && i < 4) ticket.setTecnico(tecnicos.get((i - 1) % 3));
            ticket.setFechaCreacion(LocalDateTime.now().minusHours(6 - i));
            if (estados[i] == EstadoTicket.CERRADO) ticket.setFechaCierre(LocalDateTime.now());
            Observacion observacion = new Observacion();
            observacion.setAutor("Mesa de ayuda");
            observacion.setContenido("Registro de ejemplo para demostrar el estado " + estados[i]);
            ticket.agregarObservacion(observacion);
            ticket = ticketRepository.save(ticket);
            ticket.setCodigo(String.format(Locale.ROOT, "TCK-%06d", ticket.getId()));
            ticketRepository.save(ticket);
        }
    }

    private Categoria categoria(String nombre, int horas) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion("Soporte de " + nombre.toLowerCase(Locale.ROOT));
        categoria.setTiempoRespuestaHoras(horas);
        return categoriaRepository.save(categoria);
    }

    private Usuario usuario(String nombres, String email, String departamento, boolean activo) {
        Usuario usuario = new Usuario();
        usuario.setNombres(nombres); usuario.setEmail(email);
        usuario.setDepartamento(departamento); usuario.setActivo(activo);
        return usuarioRepository.save(usuario);
    }

    private Tecnico tecnico(String nombres, String email, Categoria especialidad) {
        Tecnico tecnico = new Tecnico();
        tecnico.setNombres(nombres); tecnico.setEmail(email);
        tecnico.setEspecialidad(especialidad); tecnico.setActivo(true);
        return tecnicoRepository.save(tecnico);
    }
}
