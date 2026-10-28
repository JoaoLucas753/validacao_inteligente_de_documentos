package br.com.mentoria.validacao.repository;

import br.com.mentoria.validacao.model.Validacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ValidacaoRepository extends JpaRepository<Validacao, Long> {

    List<Validacao> findAllByOrderByCriadoEmDesc();
}
