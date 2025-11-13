package br.com.unit.gerenciamentoAulas.ui.pages;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import br.com.unit.gerenciamentoAulas.dtos.MaterialComplementarDTO;
import br.com.unit.gerenciamentoAulas.entidades.Aula;
import br.com.unit.gerenciamentoAulas.entidades.Curso;
import br.com.unit.gerenciamentoAulas.entidades.Instrutor;
import br.com.unit.gerenciamentoAulas.entidades.Local;
import br.com.unit.gerenciamentoAulas.servicos.AulaService;
import br.com.unit.gerenciamentoAulas.repositories.CursoRepository;
import br.com.unit.gerenciamentoAulas.repositories.InstrutorRepository;
import br.com.unit.gerenciamentoAulas.repositories.LocalRepository;
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
public class CriarAulaController {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String DEFAULT_CONTENT_TYPE = "application/pdf";

    @Autowired
    private AulaService aulaService;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private InstrutorRepository instrutorRepository;

    @Autowired
    private LocalRepository localRepository;

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

    private byte[] arquivoMaterialSelecionado;
    private String arquivoMaterialNome;
    private String arquivoMaterialContentType = DEFAULT_CONTENT_TYPE;

    @FXML
    public void initialize() {
        configurarComboBoxes();
        configurarSpinner();
        configurarCamposData();
        if (materialArquivoField != null) {
            materialArquivoField.setEditable(false);
        }
    }

    private void configurarComboBoxes() {
        configurarComboBoxGenerica(cursoComboBox, Curso::getNome);
        configurarComboBoxGenerica(instrutorComboBox, Instrutor::getNome);
        configurarComboBoxGenerica(localComboBox, Local::getNome);

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

    private void configurarCamposData() {
        LocalDate hoje = LocalDate.now();
        dataInicioPicker.setValue(hoje);
        dataFimPicker.setValue(hoje);
    }

    @FXML
    private void salvarAula() {
        try {
            if (!validarCampos()) {
                return;
            }

            Curso curso = cursoComboBox.getValue();
            Instrutor instrutor = instrutorComboBox.getValue();
            Local local = localComboBox.getValue();

            LocalDateTime inicio = combinarDataHora(
                dataInicioPicker.getValue(), horaInicioField.getText().trim());
            LocalDateTime fim = combinarDataHora(
                dataFimPicker.getValue(), horaFimField.getText().trim());

            if (!fim.isAfter(inicio)) {
                mostrarAlerta("Validação",
                        "A data/hora de término deve ser posterior ao início.",
                        Alert.AlertType.WARNING);
                return;
            }

            vagasSpinner.increment(0); // garante que o valor digitado seja confirmado
            Integer vagasValor = vagasSpinner.getValue();
            int vagasTotais = vagasValor != null ? vagasValor : 0;
            String observacoes = observacoesArea.getText() != null
                    ? observacoesArea.getText().trim()
                    : "";
            String titulo = tituloField.getText().trim();
            String descricao = descricaoArea.getText() != null
                    ? descricaoArea.getText().trim()
                    : "";

            if (!validarMaterialCampos()) {
                return;
            }

            Aula aulaSalva = aulaService.criarAula(
                curso.getId(),
                instrutor.getId(),
                local.getId(),
                inicio,
                fim,
                vagasTotais,
                observacoes,
                titulo,
                descricao
            );

            if (possuiMaterialInformado()) {
                MaterialComplementarDTO materialDTO = construirMaterialDTO(aulaSalva.getId(), titulo);
                aulaService.salvarMaterialComplementar(aulaSalva.getId(), materialDTO);
            }

            mostrarAlerta("Sucesso", "Aula criada com sucesso!", Alert.AlertType.INFORMATION);
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
            mostrarAlerta("Validação", "Informe horários válidos no formato HH:mm.", Alert.AlertType.WARNING);
            return false;
        }
        if (vagasSpinner.getValue() == null || vagasSpinner.getValue() <= 0) {
            mostrarAlerta("Validação", "Informe um número de vagas maior que zero.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean validarMaterialCampos() {
        String url = materialUrlField != null && materialUrlField.getText() != null
                ? materialUrlField.getText().trim()
                : "";
        boolean possuiUrl = !url.isBlank();
        boolean possuiArquivo = arquivoMaterialSelecionado != null && arquivoMaterialSelecionado.length > 0;

        if (possuiUrl && possuiArquivo) {
            mostrarAlerta("Validação", "Informe apenas URL ou apenas o arquivo do material.", Alert.AlertType.WARNING);
            return false;
        }
        if (possuiArquivo && (arquivoMaterialNome == null || arquivoMaterialNome.isBlank())) {
            mostrarAlerta("Validação", "Selecione um arquivo válido para o material.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean possuiMaterialInformado() {
        String url = materialUrlField != null && materialUrlField.getText() != null
                ? materialUrlField.getText().trim()
                : "";
        boolean possuiUrl = !url.isBlank();
        boolean possuiArquivo = arquivoMaterialSelecionado != null && arquivoMaterialSelecionado.length > 0;
        return possuiUrl || possuiArquivo;
    }

    private MaterialComplementarDTO construirMaterialDTO(Long aulaId, String tituloAula) {
        String tituloMaterial = tituloAula != null && !tituloAula.isBlank()
                ? tituloAula + " - Material"
                : "Material complementar";
        String url = materialUrlField != null && materialUrlField.getText() != null
                ? materialUrlField.getText().trim()
                : "";
        if (!url.isBlank()) {
            return MaterialComplementarDTO.criarLink(aulaId, tituloMaterial, url);
        }
        return MaterialComplementarDTO.criarArquivo(
                aulaId,
                tituloMaterial,
                arquivoMaterialNome,
                arquivoMaterialContentType,
                arquivoMaterialSelecionado
        );
    }

    @FXML
    private void selecionarArquivoMaterial() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Selecionar material complementar (PDF)");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Arquivos PDF (*.pdf)", "*.pdf"));
            Window owner = selecionarMaterialButton != null ? selecionarMaterialButton.getScene().getWindow() : null;
            File arquivo = fileChooser.showOpenDialog(owner);
            if (arquivo == null) {
                return;
            }
            byte[] conteudo = Files.readAllBytes(arquivo.toPath());
            if (conteudo.length == 0) {
                mostrarAlerta("Arquivo inválido", "O arquivo selecionado está vazio.", Alert.AlertType.WARNING);
                return;
            }
            arquivoMaterialSelecionado = conteudo;
            arquivoMaterialNome = arquivo.getName();
            String contentType = Files.probeContentType(arquivo.toPath());
            arquivoMaterialContentType = contentType != null ? contentType : DEFAULT_CONTENT_TYPE;
            if (materialArquivoField != null) {
                materialArquivoField.setText(arquivoMaterialNome);
            }
            if (materialUrlField != null) {
                materialUrlField.clear();
            }
        } catch (IOException e) {
            mostrarAlerta("Erro", "Não foi possível ler o arquivo selecionado.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void limparMaterialSelecionado() {
        arquivoMaterialSelecionado = null;
        arquivoMaterialNome = null;
        arquivoMaterialContentType = DEFAULT_CONTENT_TYPE;
        if (materialArquivoField != null) {
            materialArquivoField.clear();
        }
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
        LocalTime hora = LocalTime.parse(horaTexto, TIME_FORMATTER);
        return LocalDateTime.of(data, hora);
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
