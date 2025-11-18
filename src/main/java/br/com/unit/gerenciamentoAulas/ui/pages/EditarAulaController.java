package br.com.unit.gerenciamentoAulas.ui.pages;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import br.com.unit.gerenciamentoAulas.JavaFXApplication;
import br.com.unit.gerenciamentoAulas.dtos.MaterialComplementarDTO;
import br.com.unit.gerenciamentoAulas.entidades.Aula;
import br.com.unit.gerenciamentoAulas.entidades.Curso;
import br.com.unit.gerenciamentoAulas.entidades.Instrutor;
import br.com.unit.gerenciamentoAulas.entidades.Local;
import br.com.unit.gerenciamentoAulas.repositories.CursoRepository;
import br.com.unit.gerenciamentoAulas.repositories.InstrutorRepository;
import br.com.unit.gerenciamentoAulas.repositories.LocalRepository;
import br.com.unit.gerenciamentoAulas.servicos.AulaService;
import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

@Controller
public class EditarAulaController {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String DEFAULT_CONTENT_TYPE = "application/pdf";

    @Autowired private AulaService aulaService;
    @Autowired private CursoRepository cursoRepository;
    @Autowired private InstrutorRepository instrutorRepository;
    @Autowired private LocalRepository localRepository;

    @Autowired
    public void configureHostServices() {
        this.hostServices = JavaFXApplication.getHost();
    }

    @FXML private TextField tituloField;
    @FXML private TextArea descricaoArea;
    @FXML private ComboBox<Curso> cursoComboBox;
    @FXML private ComboBox<Instrutor> instrutorComboBox;
    @FXML private ComboBox<Local> localComboBox;
    @FXML private DatePicker dataInicioPicker;
    @FXML private TextField horaInicioField;
    @FXML private DatePicker dataFimPicker;
    @FXML private TextField horaFimField;
    @FXML private Spinner<Integer> vagasSpinner;
    @FXML private TextArea observacoesArea;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton;
    @FXML private TextField materialUrlField;
    @FXML private TextField materialArquivoField;
    @FXML private Button selecionarMaterialButton;
    @FXML private Button limparMaterialButton;

    private Long aulaId;
    private Aula aulaAtual;

    private byte[] arquivoMaterialSelecionado;
    private String arquivoMaterialNome;
    private String arquivoMaterialContentType = DEFAULT_CONTENT_TYPE;

    private HostServices hostServices;

    @FXML
    public void initialize() {
        configurarComboBoxes();
        configurarSpinner();
        carregarDadosAuxiliares();
    }

    public void setAulaId(Long aulaId) {
        this.aulaId = aulaId;
        carregarAulaSelecionada();
    }

    // ---------- ABRIR LINKS ----------
    public void abrirLink(String url) {
        try {
            if (hostServices != null) {
                hostServices.showDocument(url);
                return;
            }

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
                return;
            }

            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"open", url});
            } else {
                Runtime.getRuntime().exec(new String[]{"xdg-open", url});
            }

        } catch (Exception e) {
            mostrarAlerta("Erro", "Não foi possível abrir o link:\n" + url, Alert.AlertType.ERROR);
        }
    }

    // ---------- SALVAR ALTERAÇÕES ----------
    @FXML
    private void salvarAlteracoes() {
        try {
            if (!validarCampos() || aulaId == null) return;

            Curso curso = cursoComboBox.getValue();
            Instrutor instrutor = instrutorComboBox.getValue();
            Local local = localComboBox.getValue();

            LocalDateTime inicio = combinarDataHora(dataInicioPicker.getValue(), horaInicioField.getText().trim());
            LocalDateTime fim = combinarDataHora(dataFimPicker.getValue(), horaFimField.getText().trim());

            if (!fim.isAfter(inicio)) {
                mostrarAlerta("Validação", "A data/hora de término deve ser posterior ao início.", Alert.AlertType.WARNING);
                return;
            }

            vagasSpinner.increment(0);
            int vagasTotais = vagasSpinner.getValue();

            aulaService.editarAula(
                aulaId,
                curso.getId(),
                instrutor.getId(),
                local.getId(),
                inicio,
                fim,
                vagasTotais,
                observacoesArea.getText() != null ? observacoesArea.getText().trim() : "",
                tituloField.getText().trim(),
                descricaoArea.getText() != null ? descricaoArea.getText().trim() : ""
            );

            if (possuiMaterialInformado()) {
                MaterialComplementarDTO materialDTO = construirMaterialDTO(aulaId, tituloField.getText());
                aulaService.salvarMaterialComplementar(aulaId, materialDTO);
            }

            mostrarAlerta("Sucesso", "Aula atualizada com sucesso!", Alert.AlertType.INFORMATION);
            fecharJanela();

        } catch (Exception e) {
            mostrarAlerta("Erro ao salvar", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelar() {
        fecharJanela();
    }

    // ---------- SELEÇÃO DE ARQUIVO ----------
    @FXML
    private void selecionarArquivoMaterial() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Selecionar material complementar (PDF)");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos PDF (*.pdf)", "*.pdf"));

            Window owner = selecionarMaterialButton.getScene().getWindow();
            File arquivo = fileChooser.showOpenDialog(owner);
            if (arquivo == null) return;

            byte[] conteudo = Files.readAllBytes(arquivo.toPath());
            if (conteudo.length == 0) {
                mostrarAlerta("Arquivo inválido", "O arquivo selecionado está vazio.", Alert.AlertType.WARNING);
                return;
            }

            arquivoMaterialSelecionado = conteudo;
            arquivoMaterialNome = arquivo.getName();
            arquivoMaterialContentType = Files.probeContentType(arquivo.toPath());

            materialArquivoField.setText(arquivoMaterialNome);
            materialUrlField.clear();

        } catch (IOException e) {
            mostrarAlerta("Erro", "Não foi possível ler o arquivo selecionado.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void limparMaterialSelecionado() {
        arquivoMaterialSelecionado = null;
        arquivoMaterialNome = null;
        arquivoMaterialContentType = DEFAULT_CONTENT_TYPE;
        materialArquivoField.clear();
    }

    // ---------- CARREGAMENTO DA AULA ----------
    private void carregarDadosAuxiliares() {
        cursoComboBox.setItems(FXCollections.observableArrayList(
            cursoRepository.findAll().stream()
                .sorted(Comparator.comparing(Curso::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList()
        ));

        instrutorComboBox.setItems(FXCollections.observableArrayList(
            instrutorRepository.findAll().stream()
                .sorted(Comparator.comparing(Instrutor::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList()
        ));

        localComboBox.setItems(FXCollections.observableArrayList(
            localRepository.findAll().stream()
                .sorted(Comparator.comparing(Local::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList()
        ));
    }

    private void carregarAulaSelecionada() {
        if (aulaId == null) return;

        aulaAtual = aulaService.buscarPorId(aulaId);

        tituloField.setText(aulaAtual.getTitulo());
        descricaoArea.setText(aulaAtual.getDescricao() != null ? aulaAtual.getDescricao() : "");
        observacoesArea.setText(aulaAtual.getObservacoes() != null ? aulaAtual.getObservacoes() : "");

        selecionarValor(cursoComboBox, aulaAtual.getCurso());
        selecionarValor(instrutorComboBox, aulaAtual.getInstrutor());
        selecionarValor(localComboBox, aulaAtual.getLocal());

        LocalDateTime inicio = aulaAtual.getDataHoraInicio();
        LocalDateTime fim = aulaAtual.getDataHoraFim();

        if (inicio != null) {
            dataInicioPicker.setValue(inicio.toLocalDate());
            horaInicioField.setText(inicio.toLocalTime().format(TIME_FORMATTER));
        }

        if (fim != null) {
            dataFimPicker.setValue(fim.toLocalDate());
            horaFimField.setText(fim.toLocalTime().format(TIME_FORMATTER));
        }

        if (vagasSpinner.getValueFactory() != null) {
            vagasSpinner.getValueFactory().setValue(aulaAtual.getVagasTotais());
        }

        carregarMaterialExistente();
    }

    private void carregarMaterialExistente() {
        Optional<MaterialComplementarDTO> materialOpt = aulaService.obterMaterialComplementar(aulaId);

        materialOpt.ifPresent(material -> {
            if (material.possuiLink()) {
                materialUrlField.setText(material.getUrl());
            } else if (material.possuiArquivo()) {
                materialArquivoField.setText(material.getNomeArquivo());
            }
        });
    }

    private <T> void selecionarValor(ComboBox<T> comboBox, T valor) {
        if (valor == null) {
            comboBox.setValue(null);
            return;
        }
        comboBox.getItems().stream()
            .filter(item -> item != null && item.equals(valor))
            .findFirst()
            .ifPresent(comboBox::setValue);
    }

    private void configurarComboBoxes() {
        configurarComboBoxGenerica(cursoComboBox, Curso::getNome);
        configurarComboBoxGenerica(instrutorComboBox, Instrutor::getNome);
        configurarComboBoxGenerica(localComboBox, Local::getNome);
    }

    private <T> void configurarComboBoxGenerica(ComboBox<T> comboBox, Function<T, String> labelProvider) {
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(T object) {
                return object == null ? "" : labelProvider.apply(object);
            }
            @Override
            public T fromString(String string) {
                return null;
            }
        });

        comboBox.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : labelProvider.apply(item));
            }
        });
    }

    private void configurarSpinner() {
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 20);
        vagasSpinner.setValueFactory(valueFactory);
        vagasSpinner.setEditable(true);
    }

    private boolean validarCampos() {
        if (tituloField.getText() == null || tituloField.getText().trim().isEmpty()) {
            mostrarAlerta("Validação", "Informe o título da aula.", Alert.AlertType.WARNING);
            return false;
        }
        if (cursoComboBox.getValue() == null) {
            mostrarAlerta("Validação", "Selecione o curso.", Alert.AlertType.WARNING);
            return false;
        }
        if (instrutorComboBox.getValue() == null) {
            mostrarAlerta("Validação", "Selecione o instrutor.", Alert.AlertType.WARNING);
            return false;
        }
        if (localComboBox.getValue() == null) {
            mostrarAlerta("Validação", "Selecione o local.", Alert.AlertType.WARNING);
            return false;
        }
        if (dataInicioPicker.getValue() == null || dataFimPicker.getValue() == null) {
            mostrarAlerta("Validação", "Informe as datas de início e término.", Alert.AlertType.WARNING);
            return false;
        }
        if (!validarHora(horaInicioField.getText()) || !validarHora(horaFimField.getText())) {
            mostrarAlerta("Validação", "Horário inválido! Formato correto: HH:mm", Alert.AlertType.WARNING);
            return false;
        }
        if (vagasSpinner.getValue() == null || vagasSpinner.getValue() <= 0) {
            mostrarAlerta("Validação", "Informe vagas maiores que zero.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean validarMaterialCampos() {
        String url = materialUrlField.getText().trim();
        boolean possuiUrl = !url.isBlank();
        boolean possuiArquivo = arquivoMaterialSelecionado != null;

        if (possuiUrl && possuiArquivo) {
            mostrarAlerta("Validação", "Use apenas URL ou apenas arquivo.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean possuiMaterialInformado() {
        return !materialUrlField.getText().trim().isBlank()
            || arquivoMaterialSelecionado != null;
    }

    private MaterialComplementarDTO construirMaterialDTO(Long aulaId, String tituloAula) {
        String tituloMaterial = tituloAula + " - Material";

        if (!materialUrlField.getText().trim().isBlank()) {
            return MaterialComplementarDTO.criarLink(
                aulaId,
                tituloMaterial,
                materialUrlField.getText().trim()
            );
        }

        return MaterialComplementarDTO.criarArquivo(
            aulaId,
            tituloMaterial,
            arquivoMaterialNome,
            arquivoMaterialContentType,
            arquivoMaterialSelecionado
        );
    }

    private boolean validarHora(String hora) {
        try {
            LocalTime.parse(hora, TIME_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private LocalDateTime combinarDataHora(LocalDate data, String horaTexto) {
        return LocalDateTime.of(data, LocalTime.parse(horaTexto, TIME_FORMATTER));
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