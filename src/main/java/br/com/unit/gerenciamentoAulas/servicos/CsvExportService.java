package br.com.unit.gerenciamentoAulas.servicos;

import br.com.unit.gerenciamentoAulas.entidades.Aula;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CsvExportService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public String exportAulasToCsv(List<Aula> aulas) {

        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {


            pw.println("ID;Titulo;Descricao;Data_Inicio;Data_Fim;Instrutor;Local;Status");


            for (Aula aula : aulas) {
                pw.printf("%d;%s;%s;%s;%s;%s;%s;%s\n",
                        aula.getId(),
                        escapeCsv(aula.getTitulo()),
                        escapeCsv(aula.getDescricao()),
                        escapeCsv(aula.getDataHoraInicio().format(FORMATTER)),
                        escapeCsv(aula.getDataHoraFim().format(FORMATTER)),
                        escapeCsv(aula.getInstrutor().getNome()),
                        escapeCsv(aula.getLocal().getNome()),
                        escapeCsv(aula.getStatus())
                );
            }
        }
        return sw.toString();
    }


    private String escapeCsv(String data) {
        if (data == null) {
            return "";
        }

        if (data.contains(";") || data.contains("\"") || data.contains("\n")) {
            return "\"" + data.replace("\"", "\"\"") + "\"";
        }
        return data;
    }
}