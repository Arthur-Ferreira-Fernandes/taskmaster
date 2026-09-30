package br.ifsp.taskmaster.exception;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Resposta de erro padronizada")
public class ErrorResponse {

    @Schema(description = "Código HTTP", example = "400")
    private int status;

    @Schema(description = "Descrição do status HTTP", example = "Bad Request")
    private String error;

    @Schema(description = "Mensagem detalhada do erro", example = "titulo: o título é obrigatório")
    private String message;

    @Schema(description = "Caminho da requisição", example = "/tasks")
    private String path;

    @Schema(description = "Momento em que o erro ocorreu", example = "2026-09-30T10:15:30")
    private LocalDateTime timestamp;

    public ErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}
