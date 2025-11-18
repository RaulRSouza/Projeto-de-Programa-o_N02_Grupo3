package br.com.unit.gerenciamentoAulas.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.unit.gerenciamentoAulas.entidades.Instrutor;

@Repository
public interface InstrutorRepository extends JpaRepository<Instrutor, Long> {
    
    Optional<Instrutor> findByRegistro(String registro);
    
    List<Instrutor> findByEspecialidade(String especialidade);
    
    boolean existsByRegistro(String registro);

    Optional<Instrutor> findByEmail(String email);

    boolean existsByEmail(String email);
    
    @Query("SELECT i FROM Instrutor i WHERE i.id NOT IN " +
           "(SELECT a.instrutor.id FROM Aula a WHERE " +
           "(a.dataHoraInicio < :fim AND a.dataHoraFim > :inicio) " +
           "AND a.status != 'CANCELADA')")
    List<Instrutor> findInstrutoresDisponiveis(
        @Param("inicio") LocalDateTime inicio, 
        @Param("fim") LocalDateTime fim
    );
}
