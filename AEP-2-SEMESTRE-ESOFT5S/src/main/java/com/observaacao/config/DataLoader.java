package com.observaacao.config;

import com.observaacao.model.Categoria;
import com.observaacao.repository.CategoriaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Classe responsável por inicializar dados no banco de dados.
 * Executa automaticamente ao iniciar a aplicação.
 */
@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner loadData(CategoriaRepository categoriaRepository) {
        return args -> {
            // Verificar se já existem categorias
            if (categoriaRepository.count() == 0) {
                categoriaRepository.save(new Categoria(
                        "Iluminação Pública",
                        "Lâmpadas queimadas, postes apagados"));

                categoriaRepository.save(new Categoria(
                        "Buracos e Pavimento",
                        "Buracos na rua, calçadas esburacadas"));

                categoriaRepository.save(new Categoria(
                        "Poda de Árvores",
                        "Árvores encostando em fiação, risco à passagem"));

                categoriaRepository.save(new Categoria(
                        "Limpeza Urbana",
                        "Terrenos baldios, lixo acumulado"));

                categoriaRepository.save(new Categoria(
                        "Saúde",
                        "Falta de medicamentos, infraestrutura de postos de saúde"));

                categoriaRepository.save(new Categoria(
                        "Segurança",
                        "Violência, assédio, crimes"));

                categoriaRepository.save(new Categoria(
                        "Acessibilidade",
                        "Calçadas quebradas, falta de rampa, sem acesso"));

                categoriaRepository.save(new Categoria(
                        "Água e Esgoto",
                        "Falta de água, rompimento de tubulação"));

                categoriaRepository.save(new Categoria(
                        "Educação",
                        "Infraestrutura escolar, segurança em escolas"));

                categoriaRepository.save(new Categoria(
                        "Outros",
                        "Problemas não classificados"));

                System.out.println("✓ Categorias carregadas com sucesso!");
            }
        };
    }
}
