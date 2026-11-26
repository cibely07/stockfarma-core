package br.com.stockfarma.app;

import br.com.stockfarma.exception.EstoqueInsuficienteException;
import br.com.stockfarma.model.Categoria;
import br.com.stockfarma.model.Medicamento;
import br.com.stockfarma.service.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Predicate;

public class Main {

    public static void main(String[] args) throws IOException {
        EstoqueService estoque = new EstoqueService();
        RelatorioService relatorios = new RelatorioService(estoque);

        // 1. Importar CSV
        System.out.println("=== 1. Importação do CSV ===");
        ImportadorCsv importador = new ImportadorCsv(Path.of("erros.log"));
        List<Medicamento>         importados = importador.importar(Path.of("estoque.csv"));
        estoque.carregar(importados);
        System.out.println(importados.size() + " medicamentos importados e "
                + importador.getErros() + " erros registrados (ver erros.log)");

        // 2. Entrada e saída
        System.out.println("\n=== 2. Movimentações ===");
        estoque.registrarEntrada("MED003", 50);
        try {
            estoque.registrarSaida("MED001", 30);
        } catch (EstoqueInsuficienteException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("MED003 agora: " + estoque.buscar("MED003").quantidade());

        // 3. Saída maior que o estoque -> exceção checked
        System.out.println("\n=== 3. Saída inválida ===");
        try {
            estoque.registrarSaida("MED006", 10);
        } catch (EstoqueInsuficienteException e) {
            System.out.println("Erro de negócio: " + e.getMessage());
        }

        // 4. Desfazer
        System.out.println("\n=== 4. Desfazer ===");
        System.out.println("MED001 antes: " + estoque.buscar("MED001").quantidade());
        estoque.desfazerUltima();
        System.out.println("MED001 depois: " + estoque.buscar("MED001").quantidade());

        // 5. Relatórios
        System.out.println("\n=== 5. Relatórios ===");
        double total = relatorios.valorTotalEstoque();
        System.out.printf("Valor total do estoque: R$ %.2f%n", total);
        System.out.println("Vencendo em 30 dias: " + nomes(relatorios.vencendoEm(30)));
        System.out.println("Quantidade por categoria: " + relatorios.quantidadePorCategoria());
        System.out.println("3 mais caros: " + nomes(relatorios.maisCaros(3)));
        System.out.println("Estoque baixo (<20): " + relatorios.nomesComEstoqueBaixo(20));
        System.out.println("Mais próximo de vencer: "
                + relatorios.maisProximoDeVencer().map(Medicamento::nome).orElse("nenhum"));
        System.out.println("Categorias com estoque baixo: " + estoque.categoriasComEstoqueBaixo(20));

        Predicate<Medicamento> antibiotico = m -> m.categoria() == Categoria.ANTIBIOTICO;
        Predicate<Medicamento> estoqueBaixo = m -> m.quantidade() < 20;
        System.out.println("Antibióticos com menos de 20 unidades:");
        relatorios.filtrar(antibiotico.and(estoqueBaixo)).forEach(System.out::println);

        // 6. Gravar relatorio.txt
        String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String resumo = "Relatório StockFarma - gerado em " + dataHora + System.lineSeparator()
                + "Valor total: R$ " + String.format("%.2f", total) + System.lineSeparator()
                + "Vencendo em 30 dias: " + nomes(relatorios.vencendoEm(30)) + System.lineSeparator()
                + "Estoque baixo: " + relatorios.nomesComEstoqueBaixo(20) + System.lineSeparator();
        Files.writeString(Path.of("relatorio.txt"), resumo);
        System.out.println("\nrelatorio.txt gerado.");
    }

    private static String nomes(List<Medicamento> lista) {
        return lista.stream().map(Medicamento::nome).toList().toString();
    }
}