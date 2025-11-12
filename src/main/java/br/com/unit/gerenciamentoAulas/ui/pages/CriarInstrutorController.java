package br.com.unit.gerenciamentoAulas.ui.pages;

import br.com.unit.gerenciamentoAulas.entidades.Instrutor;
import br.com.unit.gerenciamentoAulas.repositories.InstrutorRepository;
import br.com.unit.gerenciamentoAulas.repositories.UsuarioRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
    
    @Autowired
    private UsuarioRepository usuarioRepository;

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
            
            instrutor.setSenha(senhaField.getText().trim());

            if (usuarioRepository.existsByEmail(instrutor.getEmail())) {
                mostrarAlerta("Validação", "Já existe usuário com este e-mail.", Alert.AlertType.WARNING);
                return;
            }
            if (!instrutor.getCpf().isBlank() && usuarioRepository.existsByCpf(instrutor.getCpf())) {
                mostrarAlerta("Validação", "Já existe usuário com este CPF.", Alert.AlertType.WARNING);
                return;
            }
            if (instrutorRepository.existsByRegistro(instrutor.getRegistro())) {
                mostrarAlerta("Validação", "Registro profissional já utilizado.", Alert.AlertType.WARNING);
                return;
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
        if (senhaField.getText() == null || senhaField.getText().trim().isEmpty()) {
            mostrarAlerta("Validação", "Defina uma senha inicial para o instrutor.", Alert.AlertType.WARNING);
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
