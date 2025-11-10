package br.com.unit.gerenciamentoAulas.ui.pages;

import br.com.unit.gerenciamentoAulas.entidades.Instrutor;
import br.com.unit.gerenciamentoAulas.repositories.InstrutorRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class CriarInstrutorController {
    
    @FXML private TextField nomeField;
    @FXML private TextField emailField;
    @FXML private TextField cpfField;
    @FXML private TextField telefoneField;
    @FXML private TextField especialidadeField;
    @FXML private TextField registroField;
    @FXML private PasswordField senhaField;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton;

    @Autowired
    private InstrutorRepository instrutorRepository;

    @FXML
    private void salvarInstrutor() {
        try {
            if (!validarCampos()) {
                return;
            }

            Instrutor instrutor = new Instrutor();
            instrutor.setNome(nomeField.getText().trim());
            instrutor.setEmail(emailField.getText().trim());
            instrutor.setCpf(cpfField.getText().trim());
            instrutor.setTelefone(telefoneField.getText().trim());
            instrutor.setEspecialidade(especialidadeField.getText().trim());
            instrutor.setRegistro(registroField.getText().trim());
            
            if (senhaField != null && !senhaField.getText().isEmpty()) {
                instrutor.setSenha(senhaField.getText());
            }

            instrutorRepository.save(instrutor);

            mostrarAlerta("Sucesso", "Instrutor criado com sucesso!", Alert.AlertType.INFORMATION);
            fecharJanela();
            
        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao criar instrutor: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelar() {
        fecharJanela();
    }

    private boolean validarCampos() {
        if (nomeField.getText() == null || nomeField.getText().trim().isEmpty()) {
            mostrarAlerta("Validação", "Nome do instrutor é obrigatório!", Alert.AlertType.WARNING);
            return false;
        }
        if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
            mostrarAlerta("Validação", "Email é obrigatório!", Alert.AlertType.WARNING);
            return false;
        }
        if (especialidadeField.getText() == null || especialidadeField.getText().trim().isEmpty()) {
            mostrarAlerta("Validação", "Especialidade é obrigatória!", Alert.AlertType.WARNING);
            return false;
        }
        if (registroField.getText() == null || registroField.getText().trim().isEmpty()) {
            mostrarAlerta("Validação", "Registro é obrigatório!", Alert.AlertType.WARNING);
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
