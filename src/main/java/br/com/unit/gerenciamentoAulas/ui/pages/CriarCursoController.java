package br.com.unit.gerenciamentoAulas.ui.pages;

import br.com.unit.gerenciamentoAulas.entidades.Curso;
import br.com.unit.gerenciamentoAulas.repositories.CursoRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class CriarCursoController {
    
    @FXML private TextField nomeField;
    @FXML private TextArea descricaoArea;
    @FXML private TextField cargaHorariaField;
    @FXML private TextField categoriaField;
    @FXML private CheckBox ativoCheckBox;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton;

    @Autowired
    private CursoRepository cursoRepository;

    @FXML
    public void initialize() {
        if (ativoCheckBox != null) {
            ativoCheckBox.setSelected(true);
        }
    }

    @FXML
    private void salvarCurso() {
        try {
            if (!validarCampos()) {
                return;
            }

            Curso curso = new Curso();
            curso.setNome(nomeField.getText().trim());
            curso.setDescricao(descricaoArea.getText().trim());
            curso.setCargaHoraria(Integer.parseInt(cargaHorariaField.getText().trim()));
            curso.setCategoria(categoriaField.getText().trim());
            curso.setAtivo(ativoCheckBox.isSelected());

            cursoRepository.save(curso);

            mostrarAlerta("Sucesso", "Curso criado com sucesso!", Alert.AlertType.INFORMATION);
            fecharJanela();
            
        } catch (NumberFormatException e) {
            mostrarAlerta("Erro", "Carga horária deve ser um número válido!", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao criar curso: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelar() {
        fecharJanela();
    }

    private boolean validarCampos() {
        if (nomeField.getText() == null || nomeField.getText().trim().isEmpty()) {
            mostrarAlerta("Validação", "Nome do curso é obrigatório!", Alert.AlertType.WARNING);
            return false;
        }
        if (cargaHorariaField.getText() == null || cargaHorariaField.getText().trim().isEmpty()) {
            mostrarAlerta("Validação", "Carga horária é obrigatória!", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void fecharJanela() {
        Stage stage = (Stage) salvarButton.getScene().getWindow();
        stage.close();
    }
}
