package br.com.mentoria.validacao.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Etapa de OCR: transforma o documento (imagem ou PDF) em texto.
 * Usa o Tesseract instalado no sistema (comando e idioma em application.properties).
 */
@Service
public class LeitorOcrService {

    @Value("${app.ocr.command:tesseract}")
    private String comando;

    @Value("${app.ocr.language:por}")
    private String idioma;

    public String lerTexto(MultipartFile arquivo) throws IOException {
        String nome = arquivo.getOriginalFilename() == null ? "" : arquivo.getOriginalFilename().toLowerCase();

        if (nome.endsWith(".pdf")) {
            return lerPdf(arquivo.getBytes());
        }
        if (nome.endsWith(".png") || nome.endsWith(".jpg") || nome.endsWith(".jpeg")) {
            String extensao = nome.substring(nome.lastIndexOf('.'));
            Path temp = Files.createTempFile("validacao-", extensao);
            try {
                Files.write(temp, arquivo.getBytes());
                return executarTesseract(temp);
            } finally {
                Files.deleteIfExists(temp);
            }
        }
        throw new IllegalArgumentException("Formato não suportado. Envie PDF, PNG ou JPG.");
    }

    /** PDF com texto digital: lê direto. PDF escaneado: renderiza a 1ª página e aplica OCR. */
    private String lerPdf(byte[] bytes) throws IOException {
        try (PDDocument doc = Loader.loadPDF(bytes)) {
            String texto = new PDFTextStripper().getText(doc);
            if (texto != null && texto.trim().length() > 30) {
                return texto;
            }
            BufferedImage imagem = new PDFRenderer(doc).renderImageWithDPI(0, 300);
            Path temp = Files.createTempFile("validacao-", ".png");
            try {
                ImageIO.write(imagem, "png", temp.toFile());
                return executarTesseract(temp);
            } finally {
                Files.deleteIfExists(temp);
            }
        }
    }

    private String executarTesseract(Path imagem) throws IOException {
        // equivale a:  tesseract <arquivo> stdout -l por
        ProcessBuilder pb = new ProcessBuilder(comando, imagem.toString(), "stdout", "-l", idioma);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        Process processo = pb.start();

        String saida = new String(processo.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        try {
            if (!processo.waitFor(60, TimeUnit.SECONDS)) {
                processo.destroyForcibly();
                throw new IllegalStateException("O Tesseract demorou demais para responder.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Leitura (OCR) interrompida.", e);
        }
        if (processo.exitValue() != 0) {
            throw new IllegalStateException(
                    "Falha ao executar o Tesseract. Ele está instalado? (sudo apt install tesseract-ocr tesseract-ocr-por)");
        }
        return saida;
    }
}
