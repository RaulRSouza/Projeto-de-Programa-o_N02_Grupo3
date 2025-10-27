package br.com.unit.gerenciamentoAulas.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.unit.gerenciamentoAulas.entidades.Aluno;
import br.com.unit.gerenciamentoAulas.entidades.Aula;
import br.com.unit.gerenciamentoAulas.entidades.Notificacao;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByAlunoOrderByDataEnvioDesc(Aluno aluno);

    List<Notificacao> findByAulaOrderByDataEnvioDesc(Aula aula);

    @Query("SELECT n FROM Notificacao n WHERE n.aluno.id = :alunoId AND n.lida = false ORDER BY n.dataEnvio DESC")
    List<Notificacao> findNaoLidasPorAluno(@Param("alunoId") Long alunoId);

    long countByAlunoIdAndLidaFalse(Long alunoId);

}
