package br.ifsp.taskmaster.dto.page;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Resposta paginada padrão")
public class PagedResponse<T> {

    @Schema(description = "Itens da página atual")
    private List<T> content;

    @Schema(description = "Número da página atual (começa em 0)", example = "0")
    private int page;

    @Schema(description = "Tamanho da página", example = "10")
    private int size;

    @Schema(description = "Total de elementos", example = "25")
    private long totalElements;

    @Schema(description = "Total de páginas", example = "3")
    private int totalPages;

    @Schema(description = "Indica se é a última página", example = "false")
    private boolean last;
}
