package br.com.mentoria.validacao.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Resposta da API: status (APROVADO/REPROVADO), motivos e dados extraídos. */
public record ResultadoDTO(
        Long id,
        String tipo,
        String nomeArquivo,
        String status,
        List<String> motivos,
        DadosExtraidos dados,
        LocalDateTime criadoEm
) {
}
