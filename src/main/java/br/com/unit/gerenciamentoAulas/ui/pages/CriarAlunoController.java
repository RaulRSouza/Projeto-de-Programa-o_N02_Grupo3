package br.com.unit.gerenciamentoAulas.ui.pages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import br.com.unit.gerenciamentoAulas.entidades.Aluno;
import br.com.unit.gerenciamentoAulas.repositories.AlunoRepository;
import br.com.unit.gerenciamentoAulas.repositories.UsuarioRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

@Controller
public class CriarAlunoController {

    @FXML private TextField nomeField;
    @FXML private TextField emailField;
    @FXML private TextField cpfField;
    @FXML private TextField telefoneField;
    @FXML private TextField cursoField;
    @FXML private TextField matriculaField;
    @FXML private PasswordField senhaField;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @FXML
    private void salvarAluno() {
        try {
            if (!validarCampos()) {
                return;
            }

            String email = emailField.getText().trim();
            String cpf = cpfField.getText().trim();
            String matricula = matriculaField.getText().trim();

            if (alunoRepository.existsByEmail(email)) {
                mostrarAlerta("Validação", "Já existe usuário com este e-mail.", Alert.AlertType.WARNING);
                return;
            }
            if (usuarioRepository.existsByCpf(cpf)) {
                mostrarAlerta("Validação", "Já existe usuário com este CPF.", Alert.AlertType.WARNING);
                return;
            }
            if (alunoRepository.existsByMatricula(matricula)) {
                mostrarAlerta("Validação", "Essa matrícula já está cadastrada.", Alert.AlertType.WARNING);
                return;
            }

            Aluno aluno = new Aluno();
            aluno.setNome(nomeField.getText().trim());
            aluno.setEmail(email);
            aluno.setCpf(cpf);
            aluno.setTelefone(telefoneField.getText().trim());
            aluno.setCurso(cursoField.getText().trim());
            aluno.setMatricula(matricula);
            aluno.setSenha(senhaField.getText().trim());

            alunoRepository.save(aluno);

            mostrarAlerta("Sucesso", "Aluno cadastrado com sucesso!", Alert.AlertType.INFORMATION);
            fecharJanela();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Falha ao salvar aluno: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void cancelar() {
        fecharJanela();
    }

    private boolean validarCampos() {
        if (nomeField.getText() == null || nomeField.getText().trim().isEmpty()) {
            mostrarAlerta("Validação", "Informe o nome completo do aluno.", Alert.AlertType.WARNING);
            return false;
        }
        if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
            mostrarAlerta("Validação", "Informe o e-mail do aluno.", Alert.AlertType.WARNING);
            return false;
        }
        if (cpfField.getText() == null || cpfField.getText().trim().isEmpty()) {
            mostrarAlerta("Validação", "Informe o CPF do aluno.", Alert.AlertType.WARNING);
            return false;
        }
        if (matriculaField.getText() == null || matriculaField.getText().trim().isEmpty()) {
            mostrarAlerta("Validação", "Informe a matrícula do aluno.", Alert.AlertType.WARNING);
            return false;
        }
        if (cursoField.getText() == null || cursoField.getText().trim().isEmpty()) {
            mostrarAlerta("Validação", "Informe o curso do aluno.", Alert.AlertType.WARNING);
            return false;
        }
        if (senhaField.getText() == null || senhaField.getText().trim().isEmpty()) {
            mostrarAlerta("Validação", "Defina uma senha inicial para o aluno.", Alert.AlertType.WARNING);
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

