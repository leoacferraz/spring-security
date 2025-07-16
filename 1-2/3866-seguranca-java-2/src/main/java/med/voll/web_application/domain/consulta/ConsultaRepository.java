package med.voll.web_application.domain.consulta;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    Page<Consulta> findAllByOrderByData(Pageable paginacao);

    @Query("""
            SELECT c FROM Consulta c
            WHERE c.paciente.id = :id
            ORDER BY c.data
    """)
    Page<Consulta> buscarConsultas(Pageable paginacao, Long id);
}
