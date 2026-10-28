package br.com.mentoria.validacao.service;

import br.com.mentoria.validacao.dto.DadosExtraidos;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Etapa de IA: recebe o texto do OCR e devolve os dados estruturados (JSON -> DadosExtraidos).
 * Sem chave da OpenAI (ou se a chamada falhar), usa extração por Regex como alternativa.
 */
@Service
public class ExtratorIaService {

    private static final Logger log = LoggerFactory.getLogger(ExtratorIaService.class);

    private static final String PROMPT = """
            Extraia as informações do documento abaixo.
            Retorne apenas os campos: nome, cpf, endereco, cep, cidade, estado e dataEmissao.
            A dataEmissao deve estar no formato yyyy-MM-dd.
            Se não encontrar um campo, retorne null. Não invente informações.
            Responda somente com JSON válido.

            TEXTO:
            %s
            """;

    private final ObjectMapper mapper;
    private final RestClient client;

    @Value("${app.ai.openai.api-key:}")
    private String apiKey;

    @Value("${app.ai.openai.model:gpt-4o-mini}")
    private String modelo;

    public ExtratorIaService(ObjectMapper mapper,
                             @Value("${app.ai.openai.base-url:https://api.openai.com/v1}") String baseUrl) {
        this.mapper = mapper;
        this.client = RestClient.builder().baseUrl(baseUrl).build();
    }

    public DadosExtraidos extrair(String texto) {
        if (apiKey == null || apiKey.isBlank()) {
            log.info("Chave da OpenAI não configurada: usando extração por Regex.");
            return extrairComRegex(texto);
        }
        try {
            return extrairComOpenAi(texto);
        } catch (Exception e) {
            log.warn("Falha ao usar a OpenAI ({}). Usando Regex como alternativa.", e.getMessage());
            return extrairComRegex(texto);
        }
    }

    // ---------------------------------------------------------------- OpenAI

    private DadosExtraidos extrairComOpenAi(String texto) throws Exception {
        Map<String, Object> corpo = Map.of(
                "model", modelo,
                "temperature", 0,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", "Você extrai dados de documentos e responde apenas JSON."),
                        Map.of("role", "user", "content", PROMPT.formatted(texto))
                )
        );

        String resposta = client.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(corpo)
                .retrieve()
                .body(String.class);

        JsonNode raiz = mapper.readTree(resposta);
        String json = raiz.path("choices").path(0).path("message").path("content").asText();
        return mapper.readValue(json, DadosExtraidos.class);
    }

    // ----------------------------------------------------------------- Regex

    private static final Pattern NOME = Pattern.compile("(?im)^\\s*nome\\s*:\\s*(.+)$");
    private static final Pattern ENDERECO = Pattern.compile("(?im)^\\s*endere[cç]o\\s*:\\s*(.+)$");
    private static final Pattern CPF = Pattern.compile("\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}");
    private static final Pattern CEP = Pattern.compile("\\b\\d{5}-?\\d{3}\\b");
    private static final Pattern CIDADE_UF = Pattern.compile("(?m)^[ \\t]*([\\p{L} ]+?)[ \\t]*-[ \\t]*([A-Z]{2})[ \\t]*$");
    private static final Pattern DATA_EMISSAO = Pattern.compile("(?i)emiss[aã]o[^\\d]*(\\d{2})/(\\d{2})/(\\d{4})");
    private static final Pattern DATA_QUALQUER = Pattern.compile("(\\d{2})/(\\d{2})/(\\d{4})");

    DadosExtraidos extrairComRegex(String texto) {
        if (texto == null) {
            return DadosExtraidos.vazio();
        }
        String cidade = null;
        String estado = null;
        Matcher cidadeUf = CIDADE_UF.matcher(texto);
        if (cidadeUf.find()) {
            cidade = cidadeUf.group(1).trim();
            estado = cidadeUf.group(2);
        }
        return new DadosExtraidos(
                achar(NOME, texto, 1),
                achar(CPF, texto, 0),
                achar(ENDERECO, texto, 1),
                achar(CEP, texto, 0),
                cidade,
                estado,
                acharData(texto));
    }

    private LocalDate acharData(String texto) {
        Matcher m = DATA_EMISSAO.matcher(texto);
        if (!m.find()) {
            m = DATA_QUALQUER.matcher(texto);
            if (!m.find()) {
                return null;
            }
        }
        try {
            return LocalDate.of(Integer.parseInt(m.group(3)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(1)));
        } catch (Exception e) {
            return null;
        }
    }

    private String achar(Pattern pattern, String texto, int grupo) {
        Matcher m = pattern.matcher(texto);
        return m.find() ? m.group(grupo).trim() : null;
    }
}
