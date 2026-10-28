package br.com.mentoria.validacao.service;

import br.com.mentoria.validacao.dto.DadosExtraidos;
import br.com.mentoria.validacao.dto.ResultadoDTO;
import br.com.mentoria.validacao.model.StatusValidacao;
import br.com.mentoria.validacao.model.Validacao;
import br.com.mentoria.validacao.repository.ValidacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Orquestra o fluxo: OCR -> IA -> regras de negócio -> banco -> resultado. */
@Service
public class ValidacaoService {

    public static final String COMPROVANTE_RESIDENCIA = "COMPROVANTE_RESIDENCIA";
    private static final int MIN_CARACTERES_LEGIVEL = 30;
    private static final int MAX_TEXTO_OCR = 20000;

    private final LeitorOcrService leitor;
    private final ExtratorIaService extrator;
    private final RegrasNegocioService regras;
    private final ValidacaoRepository repository;

    public ValidacaoService(LeitorOcrService leitor, ExtratorIaService extrator,
                            RegrasNegocioService regras, ValidacaoRepository repository) {
        this.leitor = leitor;
        this.extrator = extrator;
        this.regras = regras;
        this.repository = repository;
    }

    public ResultadoDTO processar(MultipartFile arquivo, String tipo) throws IOException {
        String texto = leitor.lerTexto(arquivo);

        List<String> motivos = new ArrayList<>();
        DadosExtraidos dados;

        // R1: documento legível
        if (texto == null || texto.trim().length() < MIN_CARACTERES_LEGIVEL) {
            motivos.add("Documento ilegível: não foi possível ler texto suficiente.");
            dados = DadosExtraidos.vazio();
        } else {
            dados = extrator.extrair(texto);
            motivos.addAll(regras.validar(dados));
        }

        Validacao v = new Validacao();
        v.setTipoDocumento(tipo);
        v.setNomeArquivo(arquivo.getOriginalFilename() == null ? "documento" : arquivo.getOriginalFilename());
        v.setTextoOcr(texto == null ? null : texto.substring(0, Math.min(texto.length(), MAX_TEXTO_OCR)));
        v.setNome(dados.nome());
        v.setCpf(dados.cpf());
        v.setEndereco(dados.endereco());
        v.setCep(dados.cep());
        v.setCidade(dados.cidade());
        v.setEstado(dados.estado());
        v.setDataEmissao(dados.dataEmissao());
        v.setMotivos(motivos);
        v.setStatus(motivos.isEmpty() ? StatusValidacao.APROVADO : StatusValidacao.REPROVADO);

        return paraDTO(repository.save(v));
    }

    public Optional<ResultadoDTO> buscar(Long id) {
        return repository.findById(id).map(this::paraDTO);
    }

    public List<ResultadoDTO> listar() {
        return repository.findAllByOrderByCriadoEmDesc().stream().map(this::paraDTO).toList();
    }

    private ResultadoDTO paraDTO(Validacao v) {
        DadosExtraidos dados = new DadosExtraidos(
                v.getNome(), v.getCpf(), v.getEndereco(), v.getCep(),
                v.getCidade(), v.getEstado(), v.getDataEmissao());
        return new ResultadoDTO(v.getId(), v.getTipoDocumento(), v.getNomeArquivo(),
                v.getStatus().name(), List.copyOf(v.getMotivos()), dados, v.getCriadoEm());
    }
}
