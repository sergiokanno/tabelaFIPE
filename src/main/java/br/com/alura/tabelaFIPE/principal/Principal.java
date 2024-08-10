package br.com.alura.tabelaFIPE.principal;

import br.com.alura.tabelaFIPE.model.Dados;
import br.com.alura.tabelaFIPE.model.Modelos;
import br.com.alura.tabelaFIPE.model.Veiculo;
import br.com.alura.tabelaFIPE.service.ConsumoApi;
import br.com.alura.tabelaFIPE.service.ConverteDados;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {
    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();

    private final String URL_BASE = "https://parallelum.com.br/fipe/api/v1/";

    public void exibeMenu() {
        var menu = """
            *** OPÇÕES ***
            Carro
            Moto
            Caminhao
            
            Digite uma das opções para consulta:
            
            """;

        System.out.println(menu);
        var opcao = leitura.nextLine();
        String endereco;

        if (opcao.toLowerCase().contains("carr")) {
            endereco = URL_BASE + "carros/marcas";

        } else if (opcao.toLowerCase().contains("mot")) {
            endereco = URL_BASE + "motos/marcas";

        } else {
            endereco = URL_BASE + "caminhoes/marcas";

        }

        var json = consumo.obterDados(endereco);
        System.out.println(endereco);
        System.out.println(json);

        var marcas = conversor.obterLista(json, Dados.class);

        if (marcas.isEmpty()) {
            System.out.println("Nenhuma marca encontrada");
            return;
        }

        marcas.stream()
                .sorted (Comparator.comparing (Dados::codigo))
                .forEach(System.out::println);

        System.out.println("Informe o código da marca para consulta:");
        var codigoMarca = leitura.nextLine();

        endereco = endereco + "/" + codigoMarca + "/modelos";
        json = consumo.obterDados(endereco);
        var modeloLista = conversor.obterDados(json, Modelos.class);

        if (modeloLista.modelos() == null ) {
            System.out.println("Nenhum modelo encontrado");
            return;
        }

        System.out.println("\nModelos dessa marca: ");
        modeloLista.modelos().stream()
                .sorted(Comparator.comparing(Dados::codigo))
                .forEach(System.out::println);

        System.out.println("\nDigite um trecho do nome do carro a ser buscado");
        var nomeVeiculo = leitura.nextLine();

        if (nomeVeiculo.isBlank()) {
            System.out.println("Nenhum nome informado");
            return;
        }

        List<Dados> modelosFiltrados = modeloLista.modelos().stream()
                .filter(m -> m.nome().toLowerCase().contains(nomeVeiculo.toLowerCase()))
                .collect(Collectors.toList());

        if (modelosFiltrados.isEmpty()) {
            System.out.println("Nenhum modelo encontrado");
            return;
        }

        System.out.println("\nModelos filtrados");
        modelosFiltrados.forEach(System.out::println);

        System.out.println("Digite por favor o código do modelo para buscar os valores de avaliação: ");
        var codigoModelo = leitura.nextLine();

        endereco = endereco + "/" + codigoModelo + "/anos";
        json = consumo.obterDados(endereco);

        if (json.contains("error")) {
            System.out.println("Nenhum ano encontrado para codigo do modelo informado");
            return;
        }

        List<Dados> anos = conversor.obterLista(json, Dados.class);

        if (anos.isEmpty()) {
            System.out.println("Nenhum ano encontrado");
            return;
        }

        List<Veiculo> veiculos = new ArrayList<>();

        for (int i = 0; i < anos.size(); i++) {
            var enderecoAnos = endereco + "/" + anos.get(i).codigo();
            json = consumo.obterDados(enderecoAnos);
            Veiculo veiculo = conversor.obterDados(json, Veiculo.class);
            veiculos.add(veiculo);
        }

        System.out.println("\nTodos os veículos filtrados com avaliações por ano: ");
        veiculos.forEach(System.out::println);

    }

}
