package ec.edu.espe.soportetickets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ActiveProfiles("test")
class SoporteTicketsApplicationTests {

    @Test
    @DisplayName("El contexto inicia con H2 y perfil test")
    void contextLoads() {
    }

}
