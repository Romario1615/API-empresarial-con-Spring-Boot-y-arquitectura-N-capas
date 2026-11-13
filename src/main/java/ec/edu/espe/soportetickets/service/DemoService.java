package ec.edu.espe.soportetickets.service;

import ec.edu.espe.soportetickets.dto.request.*;
import ec.edu.espe.soportetickets.dto.response.*;
import ec.edu.espe.soportetickets.entity.enums.*;
import java.util.List;

public interface DemoService {
    DemoNotificacionResponse notificar(String modo);
}

