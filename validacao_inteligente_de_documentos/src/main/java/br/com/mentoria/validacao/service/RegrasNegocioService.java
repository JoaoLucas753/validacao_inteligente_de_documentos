package br.com.mentoria.validacao.service;

import br.com.mentoria.validacao.dto.DadosExtraidos;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Regras de negócio do comprovante de residência.
 * Devolve a lista de motivos de reprovação; lista vazia significa APROVADO.
 */
@Service
public class RegrasNegocioService {

    static final int LIMITE_DIAS = 90;

    public List<String> validar(DadosExtraidos dados) {
        List<String> motivos = new ArrayList<>();

        // R2: nome identificado
        if (vazio(dados.nome())) {
            motivos.add("Nome não identificado.");
        }
        // R3: endereço identificado
        if (vazio(dados.endereco()) || vazio(dados.cep())) {
            motivos.add("Endereço não identificado.");
        }
        // R4: emissão recente (menos de 90 dias)
        if (dados.dataEmissao() == null) {
            motivos.add("Data de emissão não identificada.");
        } else {
            long dias = ChronoUnit.DAYS.between(dados.dataEmissao(), LocalDate.now());
            if (dias < 0) {
                motivos.add("Data de emissão no futuro.");
            } else if (dias >= LIMITE_DIAS) {
                motivos.add("Emissão há mais de " + LIMITE_DIAS + " dias.");
            }
        }
        // R5: CPF válido (quando informado)
        if (!vazio(dados.cpf()) && !cpfValido(dados.cpf())) {
            motivos.add("CPF inválido.");
        }
        return motivos;
    }

    static boolean cpfValido(String cpf) {
        String d = cpf.replaceAll("\\D", "");
        if (d.length() != 11 || d.chars().distinct().count() == 1) {
            return false;
        }
        return digito(d, 9) == d.charAt(9) - '0' && digito(d, 10) == d.charAt(10) - '0';
    }

    private static int digito(String d, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += (d.charAt(i) - '0') * (tamanho + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    private static boolean vazio(String s) {
        return s == null || s.isBlank();
    }
}
