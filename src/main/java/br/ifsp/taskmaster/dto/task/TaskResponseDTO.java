package br.ifsp.taskmaster.dto.task;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Representação de uma tarefa retornada pela API")
public class TaskResponseDTO {

    @Schema(description = "Identificador gerado pelo sistema", example = "1")
    private Long id;

    @Schema(description = "Título da tarefa", example = "Estudar Spring Boot")
    private String titulo;

    @Schema(description = "Descrição da tarefa", example = "Revisar DTOs, paginação e testes")
    private String descricao;

    @Schema(description = "Categoria da tarefa", example = "Estudo")
    private String categoria;

    @Schema(description = "Data limite", example = "2026-12-31")
    private LocalDate dataLimite;

    @Schema(description = "Data e hora de criação", example = "2026-09-30T10:15:30")
    private LocalDateTime dataCriacao;
}
