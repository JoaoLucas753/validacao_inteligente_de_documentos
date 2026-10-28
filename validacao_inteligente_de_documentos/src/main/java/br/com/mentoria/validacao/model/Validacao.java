package br.com.mentoria.validacao.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Uma validação de documento: o que foi enviado, o que foi extraído e a decisão. */
@Entity
@Table(name = "validacao")
public class Validacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String tipoDocumento;

    @Column(nullable = false)
    private String nomeArquivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusValidacao status;

    @Column(length = 20000)
    private String textoOcr;

    // Dados extraídos
    private String nome;
    private String cpf;
    private String endereco;
    private String cep;
    private String cidade;
    private String estado;
    private LocalDate dataEmissao;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "validacao_motivo", joinColumns = @JoinColumn(name = "validacao_id"))
    @Column(name = "descricao")
    private List<String> motivos = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public Long getId() { return id; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNomeArquivo() { return nomeArquivo; }
    public void setNomeArquivo(String nomeArquivo) { this.nomeArquivo = nomeArquivo; }

    public StatusValidacao getStatus() { return status; }
    public void setStatus(StatusValidacao status) { this.status = status; }

    public String getTextoOcr() { return textoOcr; }
    public void setTextoOcr(String textoOcr) { this.textoOcr = textoOcr; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDate getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(LocalDate dataEmissao) { this.dataEmissao = dataEmissao; }

    public List<String> getMotivos() { return motivos; }
    public void setMotivos(List<String> motivos) { this.motivos = motivos; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
}
