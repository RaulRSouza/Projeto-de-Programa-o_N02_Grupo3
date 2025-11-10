package br.com.unit.gerenciamentoAulas.ui.pages;

import br.com.unit.gerenciamentoAulas.repositories.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

@Controller
public class Dashboard implements Initializable {
    
    @FXML private Label totalCursosLabel;
    @FXML private Label totalAulasLabel;
    @FXML private Label totalInstrutoresLabel;
    @FXML private Label totalAlunosLabel;
    @FXML private Label totalInscricoesLabel;
    @FXML private Label totalLocaisLabel;

    @Autowired private CursoRepository cursoRepository;
    @Autowired private AulaRepository aulaRepository;
    @Autowired private InstrutorRepository instrutorRepository;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private InscricaoRepository inscricaoRepository;
    @Autowired private LocalRepository localRepository;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        carregarDashboard();
    }

    public void carregarDashboard() {
        try {
            if (totalCursosLabel != null && cursoRepository != null) {
                long totalCursos = cursoRepository.count();
                totalCursosLabel.setText(String.valueOf(totalCursos));
            }
            
            if (totalAulasLabel != null && aulaRepository != null) {
                long totalAulas = aulaRepository.count();
                totalAulasLabel.setText(String.valueOf(totalAulas));
            }
            
            if (totalInstrutoresLabel != null && instrutorRepository != null) {
                long totalInstrutores = instrutorRepository.count();
                totalInstrutoresLabel.setText(String.valueOf(totalInstrutores));
            }
            
            if (totalAlunosLabel != null && alunoRepository != null) {
                long totalAlunos = alunoRepository.count();
                totalAlunosLabel.setText(String.valueOf(totalAlunos));
            }
            
            if (totalInscricoesLabel != null && inscricaoRepository != null) {
                long totalInscricoes = inscricaoRepository.count();
                totalInscricoesLabel.setText(String.valueOf(totalInscricoes));
            }
            
            if (totalLocaisLabel != null && localRepository != null) {
                long totalLocais = localRepository.count();
                totalLocaisLabel.setText(String.valueOf(totalLocais));
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
