package br.com.unit.gerenciamentoAulas.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.unit.gerenciamentoAulas.entidades.Curso;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {
    
    List<Curso> findByAtivo(boolean ativo);
    
    List<Curso> findByCategoria(String categoria);
    
    List<Curso> findByNomeContainingIgnoreCase(String nome);
    
    @Query("SELECT DISTINCT c.categoria FROM Curso c ORDER BY c.categoria")
    List<String> findAllCategorias();
    
    @Query("SELECT c FROM Curso c WHERE c.ativo = true ORDER BY c.nome")
    List<Curso> findAllAtivos();
}