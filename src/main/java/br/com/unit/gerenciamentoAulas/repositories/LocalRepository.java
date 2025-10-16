package br.com.unit.gerenciamentoAulas.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.unit.gerenciamentoAulas.entidades.Local;

@Repository
public interface LocalRepository extends JpaRepository<Local, Long> {
    
    List<Local> findByDisponivel(boolean disponivel);
    
    List<Local> findByTipo(String tipo);
    
    List<Local> findByCapacidadeGreaterThanEqual(int capacidade);
    
    @Query("SELECT l FROM Local l WHERE l.disponivel = true " +
           "AND l.capacidade >= :capacidadeMinima")
    List<Local> findLocaisDisponiveisComCapacidade(
        @Param("capacidadeMinima") int capacidadeMinima
    );
    
    @Query("SELECT l FROM Local l WHERE l.id NOT IN " +
           "(SELECT a.local.id FROM Aula a WHERE " +
           "(a.dataHoraInicio < :fim AND a.dataHoraFim > :inicio) " +
           "AND a.status != 'CANCELADA')")
    List<Local> findLocaisDisponiveis(
        @Param("inicio") LocalDateTime inicio, 
        @Param("fim") LocalDateTime fim
    );
}