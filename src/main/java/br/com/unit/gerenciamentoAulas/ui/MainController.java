package br.com.unit.gerenciamentoAulas.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import br.com.unit.gerenciamentoAulas.entidades.Aula;
import br.com.unit.gerenciamentoAulas.services.AulaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

@Controller
public class MainController {

    @Autowired
    private AulaService aulaService;

    @FXML
    private TableView<AulaTableRow> tabelaAulas;

    @FXML
    private TableColumn<AulaTableRow, Long> colId;

    @FXML
    private TableColumn<AulaTableRow, String> colCurso;

    @FXML
    private TableColumn<AulaTableRow, String> colInstrutor;

    @FXML
    private TableColumn<AulaTableRow, String> colLocal;

    @FXML
    private TableColumn<AulaTableRow, String> colDataHora;

    @FXML
    private TableColumn<AulaTableRow, String> colVagas;

    @FXML
    private TableColumn<AulaTableRow, String> colStatus;

    @FXML
    private Button btnAtualizar;

    @FXML
    private Button btnFuturas;

    @FXML
    private Button btnDisponiveis;

    @FXML
    private Button btnTodas;

    @FXML
    private Label lblTotal;

    @FXML
    private Label lblStatus;

    private ObservableList<AulaTableRow> aulasData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCurso.setCellValueFactory(new PropertyValueFactory<>("curso"));
        colInstrutor.setCellValueFactory(new PropertyValueFactory<>("instrutor"));
        colLocal.setCellValueFactory(new PropertyValueFactory<>("local"));
        colDataHora.setCellValueFactory(new PropertyValueFactory<>("dataHora"));
        colVagas.setCellValueFactory(new PropertyValueFactory<>("vagas"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        tabelaAulas.setItems(aulasData);

        carregarTodasAulas();
    }

    @FXML
    private void handleAtualizar() {
        carregarTodasAulas();
    }

    @FXML
    private void handleFuturas() {
        try {
            List<Aula> aulas = aulaService.listarAulasFuturas();
            atualizarTabela(aulas);
            lblStatus.setText("Exibindo: Aulas Futuras");
        } catch (Exception e) {
            mostrarErro("Erro ao carregar aulas futuras", e.getMessage());
        }
    }

    @FXML
    private void handleDisponiveis() {
        try {
            List<Aula> aulas = aulaService.listarAulasDisponiveis();
            atualizarTabela(aulas);
            lblStatus.setText("Exibindo: Aulas com Vagas Disponíveis");
        } catch (Exception e) {
            mostrarErro("Erro ao carregar aulas disponíveis", e.getMessage());
        }
    }

    @FXML
    private void handleTodas() {
        carregarTodasAulas();
    }

    private void carregarTodasAulas() {
        try {
            List<Aula> aulas = aulaService.listarTodas();
            atualizarTabela(aulas);
            lblStatus.setText("Exibindo: Todas as Aulas");
        } catch (Exception e) {
            mostrarErro("Erro ao carregar aulas", e.getMessage());
        }
    }

    private void atualizarTabela(List<Aula> aulas) {
        aulasData.clear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Aula aula : aulas) {
            aulasData.add(new AulaTableRow(
                aula.getId(),
                aula.getCurso().getNome(),
                aula.getInstrutor().getNome(),
                aula.getLocal().getNome(),
                aula.getDataHoraInicio().format(formatter),
                aula.getVagasDisponiveis() + "/" + aula.getVagasTotais(),
                aula.getStatus()
            ));
        }

        lblTotal.setText("Total: " + aulas.size() + " aula(s)");
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    public static class AulaTableRow {
        private final Long id;
        private final String curso;
        private final String instrutor;
        private final String local;
        private final String dataHora;
        private final String vagas;
        private final String status;

        public AulaTableRow(Long id, String curso, String instrutor, String local,
                           String dataHora, String vagas, String status) {
            this.id = id;
            this.curso = curso;
            this.instrutor = instrutor;
            this.local = local;
            this.dataHora = dataHora;
            this.vagas = vagas;
            this.status = status;
        }

        public Long getId() { return id; }
        public String getCurso() { return curso; }
        public String getInstrutor() { return instrutor; }
        public String getLocal() { return local; }
        public String getDataHora() { return dataHora; }
        public String getVagas() { return vagas; }
        public String getStatus() { return status; }
    }
}