package ec.edu.espe.soportetickets.service.impl;

import ec.edu.espe.soportetickets.dto.response.AuditoriaResponse;
import ec.edu.espe.soportetickets.mapper.AuditoriaMapper;
import ec.edu.espe.soportetickets.repository.RegistroAuditoriaRepository;
import ec.edu.espe.soportetickets.service.AuditoriaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuditoriaServiceImpl implements AuditoriaService {
    private final RegistroAuditoriaRepository repository;
    public AuditoriaServiceImpl(RegistroAuditoriaRepository repository) { this.repository = repository; }

    public List<AuditoriaResponse> listarUltimos() {
        return repository.findTop50ByOrderByFechaDesc().stream().map(AuditoriaMapper::toResponse).toList();
    }
}

