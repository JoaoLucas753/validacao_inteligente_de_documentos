package br.com.mentoria.validacao.service;

import br.com.mentoria.validacao.dto.DadosExtraidos;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExtratorIaServiceTest {

    private final ExtratorIaService extrator = new ExtratorIaService(new ObjectMapper(), "http://localhost");

    @Test
    void extraiCamposPorRegex() {
        String texto = """
                COMPROVANTE DE RESIDÊNCIA
                Nome: João da Silva
                CPF: 529.982.247-25
                Endereço: Rua das Flores, 150
                CEP: 46430-000
                Guanambi - BA
                Data de emissão: 10/08/2026
                """;

        DadosExtraidos d = extrator.extrairComRegex(texto);

        assertEquals("João da Silva", d.nome());
        assertEquals("529.982.247-25", d.cpf());
        assertEquals("Rua das Flores, 150", d.endereco());
        assertEquals("46430-000", d.cep());
        assertEquals("Guanambi", d.cidade());
        assertEquals("BA", d.estado());
        assertEquals(LocalDate.of(2026, 8, 10), d.dataEmissao());
    }

    @Test
    void textoSemCamposDevolveNulos() {
        DadosExtraidos d = extrator.extrairComRegex("texto qualquer sem dados");
        assertNull(d.nome());
        assertNull(d.dataEmissao());
    }
}
