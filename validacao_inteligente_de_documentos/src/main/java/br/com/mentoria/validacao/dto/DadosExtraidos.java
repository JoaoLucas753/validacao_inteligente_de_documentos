package br.com.mentoria.validacao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

/** Campos extraídos do comprovante de residência (a IA devolve isso em JSON). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DadosExtraidos(
        String nome,
        String cpf,
        String endereco,
        String cep,
        String cidade,
        String estado,
        LocalDate dataEmissao
) {
    public static DadosExtraidos vazio() {
        return new DadosExtraidos(null, null, null, null, null, null, null);
    }
}
