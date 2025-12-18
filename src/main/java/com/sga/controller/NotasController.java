package com.sga.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sga.dto.ImportacaoResumoDTO;
import com.sga.dto.NotaDebitoDetalheDTO;
import com.sga.dto.NotaDebitoResumoDTO;
import com.sga.repository.ImportacaoSPCRepository;
import com.sga.service.NotaDebitoService;

import com.itextpdf.layout.properties.UnitValue;

@RestController
@RequestMapping("/api/notas")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class NotasController {

	@Autowired
	private ImportacaoSPCRepository importacaoRepo;

	@Autowired
	private NotaDebitoService notaService;

	// lista resumida de importações (para select)
	@GetMapping("/importacoes/resumo")
	public List<ImportacaoResumoDTO> listarImportacoes() {
		// recuperar importações (pode mapear para DTO)
		return importacaoRepo.findAll().stream()
				.map(i -> new ImportacaoResumoDTO(i.getId(), i.getNomeArquivo(), null, i.getDataImportacao(), 0, 0))
				.collect(java.util.stream.Collectors.toList());
	}

	// listagem paginada de notas
	@GetMapping("/importacao/{id}")
	public Page<NotaDebitoResumoDTO> listarNotas(@PathVariable Long id, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String filtro) {
		return notaService.listarNotasPaginado(id, page, size, filtro);
	}

	// detalhes de nota
	@GetMapping("/{id}")
	public NotaDebitoDetalheDTO detalhes(@PathVariable Long id) {
		return notaService.buscarDetalhes(id);
	}

	// export CSV da nota (itens + resumo)
	@GetMapping("/{id}/export/csv")
	public ResponseEntity<InputStreamResource> exportCsv(@PathVariable Long id) {
		NotaDebitoDetalheDTO dto = notaService.buscarDetalhes(id);
		StringBuilder sb = new StringBuilder();
		sb.append("NumeroNota;CodigoSocio;NomeAssociado;TotalDebitos;TotalCreditos;ValorFaturado\n");
		sb.append(dto.numeroNota).append(";").append(dto.codigoSocio).append(";\"").append(dto.nomeAssociado)
				.append("\";").append(dto.totalDebitos).append(";").append(dto.totalCreditos).append(";")
				.append(dto.valorFaturado).append("\n\n");
		sb.append("Seq;Descricao;Qtd;ValorUnit;ValorTotal;Credito/Debito\n");
		int seq = 1;
		for (var i : dto.itens) {
			sb.append(seq++).append(";\"").append(i.getDescricaoServico()).append("\";")
					.append(i.getQuantidadeServicos()).append(";").append(i.getValorUnitario()).append(";")
					.append(i.getValorTotal()).append(";").append(i.getCreditoDebito()).append("\n");
		}
		byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
		InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(bytes));
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDisposition(
				ContentDisposition.builder("attachment").filename("nota_" + dto.numeroNota + ".csv").build());
		return ResponseEntity.ok().headers(headers).contentLength(bytes.length)
				.contentType(MediaType.parseMediaType("text/csv; charset=UTF-8")).body(resource);
	}

	// export PDF simples (exemplo com iText) - retornando PDF binário
	@GetMapping("/{id}/export/pdf")
	public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) {
		NotaDebitoDetalheDTO dto = notaService.buscarDetalhes(id);

		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			// Exemplo iText 7 (adapte imports/dependência) - criar PDF minimal
			com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(out);
			com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
			com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf);

			document.add(new com.itextpdf.layout.element.Paragraph("Nota Débito: " + dto.numeroNota));
			document.add(new com.itextpdf.layout.element.Paragraph(
					"Associado: " + dto.codigoSocio + " - " + dto.nomeAssociado));
			document.add(new com.itextpdf.layout.element.Paragraph("Total Débitos: " + dto.totalDebitos
					+ " | Total Créditos: " + dto.totalCreditos + " | Valor Faturado: " + dto.valorFaturado));
			document.add(new com.itextpdf.layout.element.Paragraph(" "));

			com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(
					new float[] { 1, 4, 1, 1, 1, 1 });
			table.setWidth(UnitValue.createPercentValue(100));;
			table.addHeaderCell("Seq");
			table.addHeaderCell("Descrição");
			table.addHeaderCell("Qtd");
			table.addHeaderCell("V.Unit");
			table.addHeaderCell("V.Total");
			table.addHeaderCell("C/D");

			int seq = 1;
			for (var i : dto.itens) {
				table.addCell(String.valueOf(seq++));
				table.addCell(i.getDescricaoServico() != null ? i.getDescricaoServico() : "");
				table.addCell(String.valueOf(i.getQuantidadeServicos()));
				table.addCell(String.valueOf(i.getValorUnitario()));
				table.addCell(String.valueOf(i.getValorTotal()));
				table.addCell(i.getCreditoDebito() != null ? i.getCreditoDebito() : "");
			}
			document.add(table);
			document.close();

			byte[] pdfBytes = out.toByteArray();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentDisposition(
					ContentDisposition.builder("attachment").filename("nota_" + dto.numeroNota + ".pdf").build());
			headers.setContentType(MediaType.APPLICATION_PDF);
			return ResponseEntity.ok().headers(headers).body(pdfBytes);
		} catch (Exception e) {
			return ResponseEntity.status(500)
					.body(("Erro gerando PDF: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
		}
	}
}
