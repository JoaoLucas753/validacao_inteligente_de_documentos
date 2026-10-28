package br.com.mentoria.validacao.controller;

import br.com.mentoria.validacao.dto.ResultadoDTO;
import br.com.mentoria.validacao.service.ValidacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/validacoes")
@CrossOrigin
public class ValidacaoController {

    private final ValidacaoService service;

    public ValidacaoController(ValidacaoService service) {
        this.service = service;
    }

    /** Upload + processamento. Ex.: curl -F tipo=COMPROVANTE_RESIDENCIA -F arquivo=@comprovante.pdf */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResultadoDTO> enviar(@RequestParam("tipo") String tipo,
                                               @RequestParam("arquivo") MultipartFile arquivo) {
        if (arquivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo vazio.");
        }
        if (!ValidacaoService.COMPROVANTE_RESIDENCIA.equals(tipo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Tipo não suportado no MVP. Use COMPROVANTE_RESIDENCIA.");
        }
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.processar(arquivo, tipo));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IOException | IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResultadoDTO buscar(@PathVariable("id") Long id) {
        return service.buscar(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Validação não encontrada."));
    }

    @GetMapping
    public List<ResultadoDTO> listar() {
        return service.listar();
    }
}
