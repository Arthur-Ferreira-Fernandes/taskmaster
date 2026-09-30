package br.ifsp.taskmaster.dto.task;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados de entrada para criar ou atualizar uma tarefa")
public class TaskRequestDTO {

    @Schema(description = "Título da tarefa", example = "Estudar Spring Boot")
    @NotBlank(message = "o título é obrigatório")
    @Size(max = 100, message = "o título deve ter no máximo 100 caracteres")
    private String titulo;

    @Schema(description = "Descrição da tarefa", example = "Revisar DTOs, paginação e testes")
    @Size(max = 255, message = "a descrição deve ter no máximo 255 caracteres")
    private String descricao;

    @Schema(description = "Categoria da tarefa", example = "Estudo")
    @NotBlank(message = "a categoria é obrigatória")
    @Size(max = 50, message = "a categoria deve ter no máximo 50 caracteres")
    private String categoria;

    @Schema(description = "Data limite no formato yyyy-MM-dd", example = "2026-12-31", type = "string", format = "date")
    @NotNull(message = "a data limite é obrigatória")
    private LocalDate dataLimite;
}
