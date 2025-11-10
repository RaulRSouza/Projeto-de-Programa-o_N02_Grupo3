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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import br.com.unit.gerenciamentoAulas.entidades.PerfilUsuario;
import br.com.unit.gerenciamentoAulas.ui.SessionManager;

@Component
public class ConfiguracoesController implements Initializable {

    @Autowired
    private SessionManager sessionManager;

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

    private String perfilAnterior = "Administrador";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        perfilComboBox.setItems(FXCollections.observableArrayList(DESCRICOES_PERFIL.keySet()));
        perfilComboBox.getSelectionModel().selectFirst();
        perfilAnterior = perfilComboBox.getSelectionModel().getSelectedItem();
        atualizarDescricaoPerfil();
        atualizarStatusAuditoria();
    }

    @FXML
    private void handleAuditoriaAlterada() {
        atualizarStatusAuditoria();
    }

    @FXML
    private void handlePerfilAlterado() {
        String perfilSelecionado = perfilComboBox.getSelectionModel().getSelectedItem();
        
        // Se o perfil mudou, mostra alerta de confirmação
        if (!perfilSelecionado.equals(perfilAnterior)) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Mudança de Perfil");
            alert.setHeaderText("Você está mudando de perfil!");
            alert.setContentText(String.format(
                "Perfil Atual: %s\n" +
                "Novo Perfil: %s\n\n" +
                "Ao mudar de perfil, suas permissões serão alteradas.\n" +
                "Deseja continuar?",
                perfilAnterior, perfilSelecionado
            ));
            
            // Customiza os botões
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            
            Optional<ButtonType> resultado = alert.showAndWait();
            
            if (resultado.isPresent() && resultado.get() == ButtonType.YES) {
                perfilAnterior = perfilSelecionado;
                atualizarDescricaoPerfil();
                
                PerfilUsuario novoPerfil = converterParaEnum(perfilSelecionado);
                if (sessionManager != null) {
                    sessionManager.setPerfilAtual(novoPerfil);
                }
                
                // Mostra notificação de sucesso
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Perfil Alterado");
                info.setHeaderText("✅ Perfil alterado com sucesso!");
                info.setContentText(String.format(
                    "Agora você está operando como: %s\n\n%s",
                    perfilSelecionado,
                    DESCRICOES_PERFIL.get(perfilSelecionado)
                ));
                info.showAndWait();
            } else {
                perfilComboBox.getSelectionModel().select(perfilAnterior);
            }
        } else {
            atualizarDescricaoPerfil();
        }
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
    
    private PerfilUsuario converterParaEnum(String perfil) {
        switch (perfil) {
            case "Administrador":
                return PerfilUsuario.ADMINISTRADOR;
            case "Instrutor":
                return PerfilUsuario.INSTRUTOR;
            case "Aluno":
                return PerfilUsuario.ALUNO;
            default:
                return PerfilUsuario.ADMINISTRADOR;
        }
    }
}
