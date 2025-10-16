package br.com.unit.gerenciamentoAulas.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.unit.gerenciamentoAulas.entidades.Aluno;
import br.com.unit.gerenciamentoAulas.entidades.Aula;
import br.com.unit.gerenciamentoAulas.entidades.Inscricao;

@Repository
public interface InscricaoRepository extends JpaRepository<Inscricao, Long> {
    
    List<Inscricao> findByAluno(Aluno aluno);
    
    List<Inscricao> findByAula(Aula aula);
    
    List<Inscricao> findByStatus(String status);
    
    Optional<Inscricao> findByAlunoAndAula(Aluno aluno, Aula aula);
    
    boolean existsByAlunoAndAula(Aluno aluno, Aula aula);
    
    @Query("SELECT i FROM Inscricao i WHERE i.aluno.id = :alunoId " +
           "AND i.status = :status ORDER BY i.dataInscricao DESC")
    List<Inscricao> findByAlunoIdAndStatus(
        @Param("alunoId") Long alunoId, 
        @Param("status") String status
    );
    
    @Query("SELECT i FROM Inscricao i WHERE i.aula.id = :aulaId " +
           "AND i.status = 'CONFIRMADA'")
    List<Inscricao> findInscricoesConfirmadasPorAula(@Param("aulaId") Long aulaId);
    
    @Query("SELECT COUNT(i) FROM Inscricao i WHERE i.aula.id = :aulaId " +
           "AND i.status = 'CONFIRMADA'")
    long countInscricoesConfirmadasPorAula(@Param("aulaId") Long aulaId);
    
    @Query("SELECT COUNT(i) > 0 FROM Inscricao i " +
           "WHERE i.aluno = :aluno AND i.status = 'CONFIRMADA' " +
           "AND i.aula.status != 'CANCELADA' " +
           "AND ((i.aula.dataHoraInicio < :fim AND i.aula.dataHoraFim > :inicio))")
    boolean existsConflitoHorarioAluno(
        @Param("aluno") Aluno aluno,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );
    
    @Query("SELECT i FROM Inscricao i WHERE i.aluno.id = :alunoId " +
           "AND i.status = 'CONFIRMADA' " +
           "AND i.aula.dataHoraInicio >= :agora " +
           "ORDER BY i.aula.dataHoraInicio")
    List<Inscricao> findInscricoesFuturasAluno(
        @Param("alunoId") Long alunoId,
        @Param("agora") LocalDateTime agora
    );
}