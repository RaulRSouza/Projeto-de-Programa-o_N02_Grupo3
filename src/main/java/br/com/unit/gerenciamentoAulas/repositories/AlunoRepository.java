package br.com.unit.gerenciamentoAulas.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.unit.gerenciamentoAulas.entidades.Aluno;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Long> {
    
    Optional<Aluno> findByMatricula(String matricula);
    
    List<Aluno> findByCurso(String curso);
    
    boolean existsByMatricula(String matricula);

    boolean existsByEmail(String email);
    
    Optional<Aluno> findByEmail(String email);
}