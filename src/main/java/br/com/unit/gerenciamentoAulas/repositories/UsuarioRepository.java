package br.com.unit.gerenciamentoAulas.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.unit.gerenciamentoAulas.entidades.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByCpf(String cpf);
    
    boolean existsByCpf(String cpf);
}