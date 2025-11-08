package br.com.unit.gerenciamentoAulas.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.unit.gerenciamentoAulas.entidades.Aula;
import br.com.unit.gerenciamentoAulas.entidades.Curso;
import br.com.unit.gerenciamentoAulas.entidades.Instrutor;
import br.com.unit.gerenciamentoAulas.entidades.Local;

@Repository
public interface AulaRepository extends JpaRepository<Aula, Long> {
    
    List<Aula> findByStatus(String status);
    
    List<Aula> findByCurso(Curso curso);
    
    List<Aula> findByInstrutor(Instrutor instrutor);
    
    List<Aula> findByLocal(Local local);

    long countByCursoId(Long cursoId);
    long countByInstrutorId(Long instrutorId);
    long countByLocalId(Long localId);
    
    @Query("SELECT a FROM Aula a WHERE a.dataHoraInicio >= :inicio " +
           "AND a.dataHoraInicio <= :fim ORDER BY a.dataHoraInicio")
    List<Aula> findAulasPorPeriodo(
        @Param("inicio") LocalDateTime inicio, 
        @Param("fim") LocalDateTime fim
    );
    
    @Query("SELECT a FROM Aula a WHERE a.status != 'CANCELADA' " +
           "AND a.dataHoraInicio >= :agora ORDER BY a.dataHoraInicio")
    List<Aula> findAulasFuturas(@Param("agora") LocalDateTime agora);
    
    @Query("SELECT a FROM Aula a WHERE a.vagasDisponiveis > 0 " +
           "AND a.status = 'AGENDADA' AND a.dataHoraInicio >= :agora " +
           "ORDER BY a.dataHoraInicio")
    List<Aula> findAulasComVagasDisponiveis(@Param("agora") LocalDateTime agora);
    
    @Query("SELECT COUNT(a) > 0 FROM Aula a WHERE a.instrutor = :instrutor " +
           "AND a.status != 'CANCELADA' " +
           "AND ((a.dataHoraInicio < :fim AND a.dataHoraFim > :inicio))")
    boolean existsConflitoInstrutor(
        @Param("instrutor") Instrutor instrutor,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    @Query("SELECT COUNT(a) > 0 FROM Aula a WHERE a.instrutor = :instrutor " +
           "AND a.status != 'CANCELADA' AND a.id <> :aulaId " +
           "AND ((a.dataHoraInicio < :fim AND a.dataHoraFim > :inicio))")
    boolean existsConflitoInstrutorExcluindoAula(
        @Param("instrutor") Instrutor instrutor,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim,
        @Param("aulaId") Long aulaId
    );
    
    @Query("SELECT COUNT(a) > 0 FROM Aula a WHERE a.local = :local " +
           "AND a.status != 'CANCELADA' " +
           "AND ((a.dataHoraInicio < :fim AND a.dataHoraFim > :inicio))")
    boolean existsConflitoLocal(
        @Param("local") Local local,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    @Query("SELECT COUNT(a) > 0 FROM Aula a WHERE a.local = :local " +
           "AND a.status != 'CANCELADA' AND a.id <> :aulaId " +
           "AND ((a.dataHoraInicio < :fim AND a.dataHoraFim > :inicio))")
    boolean existsConflitoLocalExcluindoAula(
        @Param("local") Local local,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim,
        @Param("aulaId") Long aulaId
    );
    
    @Query("SELECT a FROM Aula a WHERE a.curso.id = :cursoId " +
           "AND a.status = :status ORDER BY a.dataHoraInicio DESC")
    List<Aula> findByCursoIdAndStatus(
        @Param("cursoId") Long cursoId, 
        @Param("status") String status
    );
    
    @Query("SELECT a FROM Aula a WHERE a.instrutor.id = :instrutorId " +
           "AND a.dataHoraInicio >= :inicio AND a.dataHoraInicio <= :fim " +
           "ORDER BY a.dataHoraInicio")
    List<Aula> findByInstrutorIdAndPeriodo(
        @Param("instrutorId") Long instrutorId,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );
}
