package br.com.unit.gerenciamentoAulas.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.unit.gerenciamentoAulas.entidades.Administrador;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    
    List<Administrador> findBySetor(String setor);
    
    List<Administrador> findByNivelAcesso(String nivelAcesso);
}