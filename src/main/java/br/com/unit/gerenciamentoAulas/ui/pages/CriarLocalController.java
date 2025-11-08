package br.com.unit.gerenciamentoAulas.ui.pages;

import br.com.unit.gerenciamentoAulas.entidades.Local;
import br.com.unit.gerenciamentoAulas.repositories.LocalRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class CriarLocalController {
    
    @FXML private TextField nomeField;
    @FXML private TextField enderecoField;
    @FXML private TextField capacidadeField;
    @FXML private TextField tipoField;
    @FXML private CheckBox disponivelCheckBox;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton;

    @Autowired
    private LocalRepository localRepository;

    @FXML
    public void initialize() {
        if (disponivelCheckBox != null) {
            disponivelCheckBox.setSelected(true);
        }
    }

    @FXML
    private void salvarLocal() {
        try {
            if (!validarCampos()) {
                return;
            }

            Local local = new Local();
            local.setNome(nomeField.getText().trim());
            local.setEndereco(enderecoField.getText().trim());
            local.setCapacidade(Integer.parseInt(capacidadeField.getText().trim()));
            local.setTipo(tipoField.getText().trim());
            local.setDisponivel(disponivelCheckBox.isSelected());

            localRepository.save(local);

            mostrarAlerta("Sucesso", "Local criado com sucesso!", Alert.AlertType.INFORMATION);
            fecharJanela();
            
        } catch (NumberFormatException e) {
            mostrarAlerta("Erro", "Capacidade deve ser um número válido!", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao criar local: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelar() {
        fecharJanela();
    }

    private boolean validarCampos() {
        if (nomeField.getText() == null || nomeField.getText().trim().isEmpty()) {
            mostrarAlerta("Validação", "Nome do local é obrigatório!", Alert.AlertType.WARNING);
            return false;
        }
        if (capacidadeField.getText() == null || capacidadeField.getText().trim().isEmpty()) {
            mostrarAlerta("Validação", "Capacidade é obrigatória!", Alert.AlertType.WARNING);
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
