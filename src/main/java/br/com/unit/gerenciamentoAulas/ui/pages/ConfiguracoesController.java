package br.com.unit.gerenciamentoAulas.ui.pages;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

@Component
public class ConfiguracoesController implements Initializable {

    private static final Map<String, String> DESCRICOES_PERFIL = new LinkedHashMap<>();

    static {
        DESCRICOES_PERFIL.put("Administrador",
                """
                • Cria, edita e cancela aulas;
                • Associa instrutores e define locais;
                • Ajusta a capacidade e acompanha impactos das mudanças.""");
        DESCRICOES_PERFIL.put("Instrutor",
                """
                • Visualiza suas aulas e conteúdos;
                • Pode editar materiais;
                • Solicita cancelamentos quando necessário.""");
        DESCRICOES_PERFIL.put("Aluno",
                """
                • Consulta aulas disponíveis;
                • Realiza inscrições e cancelamentos;
                • Acompanha status das suas vagas.""");
    }

    @FXML
    private CheckBox auditoriaCheckBox;
    @FXML
    private Label auditoriaStatusLabel;
    @FXML
    private ComboBox<String> perfilComboBox;
    @FXML
    private Label perfilDescricaoLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        perfilComboBox.setItems(FXCollections.observableArrayList(DESCRICOES_PERFIL.keySet()));
        perfilComboBox.getSelectionModel().selectFirst();
        atualizarDescricaoPerfil();
        atualizarStatusAuditoria();
    }

    @FXML
    private void handleAuditoriaAlterada() {
        atualizarStatusAuditoria();
    }

    @FXML
    private void handlePerfilAlterado() {
        atualizarDescricaoPerfil();
    }

    private void atualizarStatusAuditoria() {
        boolean habilitada = auditoriaCheckBox.isSelected();
        auditoriaStatusLabel.setText(
                habilitada ? "Auditoria habilitada: todas as ações serão registradas."
                           : "Auditoria desativada: alterações não serão logadas.");
        auditoriaStatusLabel.setStyle(
                habilitada ? "-fx-text-fill: #166534;" : "-fx-text-fill: #92400e;");
    }

    private void atualizarDescricaoPerfil() {
        String perfil = perfilComboBox.getSelectionModel().getSelectedItem();
        String descricao = DESCRICOES_PERFIL.getOrDefault(perfil, "");
        perfilDescricaoLabel.setText(perfil != null
                ? perfil + "\n" + descricao
                : "Selecione um modo operacional para visualizar suas permissões.");
    }
}