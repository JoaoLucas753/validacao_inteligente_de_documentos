package br.com.mentoria.validacao.service;

import br.com.mentoria.validacao.dto.DadosExtraidos;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegrasNegocioServiceTest {

    private final RegrasNegocioService service = new RegrasNegocioService();

    private DadosExtraidos dados(String nome, String endereco, String cep, LocalDate data, String cpf) {
        return new DadosExtraidos(nome, cpf, endereco, cep, "Recife", "PE", data);
    }

    @Test
    void documentoCompletoERecenteEAprovado() {
        var d = dados("João da Silva", "Rua das Flores, 150", "46430000", LocalDate.now().minusDays(10), "529.982.247-25");
        assertTrue(service.validar(d).isEmpty());
    }

    @Test
    void emissaoHaMaisDeNoventaDiasEReprovada() {
        var d = dados("João", "Rua A, 1", "50000000", LocalDate.now().minusDays(120), null);
        assertTrue(service.validar(d).contains("Emissão há mais de 90 dias."));
    }

    @Test
    void emissaoExatamenteHaNoventaDiasEReprovada() {
        var d = dados("João", "Rua A, 1", "50000000", LocalDate.now().minusDays(90), null);
        assertFalse(service.validar(d).isEmpty());
    }

    @Test
    void semEnderecoEReprovado() {
        var d = dados("João", null, null, LocalDate.now().minusDays(5), null);
        assertTrue(service.validar(d).contains("Endereço não identificado."));
    }

    @Test
    void semNomeEReprovado() {
        var d = dados("  ", "Rua A, 1", "50000000", LocalDate.now().minusDays(5), null);
        assertTrue(service.validar(d).contains("Nome não identificado."));
    }

    @Test
    void semDataEReprovado() {
        var d = dados("João", "Rua A, 1", "50000000", null, null);
        assertTrue(service.validar(d).contains("Data de emissão não identificada."));
    }

    @Test
    void cpfInvalidoEReprovado() {
        var d = dados("João", "Rua A, 1", "50000000", LocalDate.now().minusDays(5), "111.111.111-11");
        assertTrue(service.validar(d).contains("CPF inválido."));
    }

    @Test
    void cpfValidoPassa() {
        assertTrue(RegrasNegocioService.cpfValido("529.982.247-25"));
    }
}
