package br.com.stockfarma.service;

import br.com.stockfarma.model.Categoria;
import br.com.stockfarma.model.Medicamento;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ImportadorCsv {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final Path arquivoLog;
    private int erros = 0;

    public ImportadorCsv(Path arquivoLog) { this.arquivoLog = arquivoLog; }

    public int getErros() { return erros; }

    public List<Medicamento> importar(Path arquivo) throws IOException {
        List<Medicamento> lista = new ArrayList<>();
        List<String> linhas = Files.readAllLines(arquivo);
        erros = 0;

        for (int i = 1; i < linhas.size(); i++) {          // i = 1 ignora o cabeçalho
            int numeroLinha = i + 1;                       // número real da linha no arquivo
            String[] c = linhas.get(i).split(";", -1);
            String campo = "linha";
            try {
                if (c.length != 6) {
                    throw new IllegalArgumentException("Quantidade de colunas inválida");
                }
                String codigo = c[0].trim();
                String nome = c[1].trim();
                String categoriaTexto = c[2].trim();
                String precoTexto = c[3];
                String quantidadeTexto = c[4];
                String validadeTexto = c[5];

                campo = "preço";
                double preco = Double.parseDouble(precoTexto.trim());
                campo = "quantidade";
                int qtd = Integer.parseInt(quantidadeTexto.trim());
                campo = "categoria";
                Categoria cat = Categoria.valueOf(categoriaTexto);
                campo = "validade";
                LocalDate val = LocalDate.parse(validadeTexto.trim());
                campo = "medicamento";
                lista.add(new Medicamento(codigo, nome, cat, preco, qtd, val));
            } catch (NumberFormatException e) {
                registrar(numeroLinha, campo + " inválido '" + valor(c, campo) + "'");
            } catch (DateTimeParseException e) {
                registrar(numeroLinha, "validade inválida '" + valor(c, campo) + "'");
            } catch (IllegalArgumentException e) {
                registrar(numeroLinha, campo.equals("categoria")
                        ? "categoria inválida '" + c[2] + "'" : e.getMessage());
            } catch (ArrayIndexOutOfBoundsException e) {
                registrar(numeroLinha, "colunas insuficientes");
            }
        }
        return lista;
    }

    private String valor(String[] c, String campo) {
        return switch (campo) {
            case "preço" -> c[3];
            case "quantidade" -> c[4];
            case "validade" -> c[5];
            default -> "";
        };
    }

    private void registrar(int linha, String motivo) {
        erros++;
        String texto = "[" + LocalDateTime.now().format(FMT) + "] linha " + linha + ": " + motivo
                + System.lineSeparator();
        try {
            Files.writeString(arquivoLog, texto, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Não foi possível gravar o log: " + e.getMessage());
        }
    }
}