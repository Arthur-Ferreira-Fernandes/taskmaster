package br.ifsp.taskmaster.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import br.ifsp.taskmaster.model.Task;
import br.ifsp.taskmaster.repository.TaskRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void cleanDb() {
        taskRepository.deleteAll();
    }

    private Task saveTask(String titulo, String categoria, LocalDate dataLimite) {
        Task task = new Task();
        task.setTitulo(titulo);
        task.setDescricao("Descrição de " + titulo);
        task.setCategoria(categoria);
        task.setDataLimite(dataLimite);
        task.setDataCriacao(LocalDateTime.now());
        return taskRepository.save(task);
    }

    private String json(String titulo, String categoria, String dataLimite) {
        return """
                {
                    "titulo": "%s",
                    "descricao": "Descrição da tarefa",
                    "categoria": "%s",
                    "dataLimite": "%s"
                }
                """.formatted(titulo, categoria, dataLimite);
    }

    @Test
    void shouldCreateTask() throws Exception {
        String dataLimite = LocalDate.now().plusDays(7).toString();

        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Estudar Spring", "Estudo", dataLimite)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.titulo").value("Estudar Spring"))
                .andExpect(jsonPath("$.categoria").value("Estudo"))
                .andExpect(jsonPath("$.dataLimite").value(dataLimite));
    }

    @Test
    void shouldReturnBadRequestWhenTituloIsMissing() throws Exception {
        String body = """
                {
                    "descricao": "Sem título",
                    "categoria": "Estudo",
                    "dataLimite": "%s"
                }
                """.formatted(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("titulo")));
    }

    @Test
    void shouldReturnBadRequestWhenDataLimiteHasInvalidFormat() throws Exception {
        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Estudar Spring", "Estudo", "31/12/2026")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("dataLimite")));
    }

    @Test
    void shouldReturnBadRequestWhenDataLimiteIsInThePast() throws Exception {
        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Estudar Spring", "Estudo", LocalDate.now().minusDays(1).toString())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("passado")));
    }

    @Test
    void shouldUpdateExistingTask() throws Exception {
        Task saved = saveTask("Tarefa antiga", "Trabalho", LocalDate.now().plusDays(2));
        String dataLimite = LocalDate.now().plusDays(10).toString();

        mockMvc.perform(put("/tasks/" + saved.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(json("Tarefa atualizada", "Estudo", dataLimite)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.titulo").value("Tarefa atualizada"))
                .andExpect(jsonPath("$.descricao").value("Descrição da tarefa"))
                .andExpect(jsonPath("$.categoria").value("Estudo"))
                .andExpect(jsonPath("$.dataLimite").value(dataLimite))
                .andExpect(jsonPath("$.dataCriacao").exists());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonexistentTask() throws Exception {
        mockMvc.perform(put("/tasks/9999").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Qualquer", "Estudo", LocalDate.now().plusDays(1).toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldDeleteExistingTask() throws Exception {
        Task saved = saveTask("Remover", "Trabalho", LocalDate.now().plusDays(2));

        mockMvc.perform(delete("/tasks/" + saved.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/tasks/" + saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonexistentTask() throws Exception {
        mockMvc.perform(delete("/tasks/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldListFirstPageWithDefaultSize() throws Exception {
        for (int i = 1; i <= 12; i++) {
            saveTask("Tarefa " + i, "Trabalho", LocalDate.now().plusDays(i));
        }

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void shouldListRequestedPageAndSize() throws Exception {
        for (int i = 1; i <= 12; i++) {
            saveTask("Tarefa " + i, "Trabalho", LocalDate.now().plusDays(i));
        }

        mockMvc.perform(get("/tasks").param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.content[0].titulo").value("Tarefa 6"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void shouldListTasksSortedByDataLimiteAsc() throws Exception {
        saveTask("Mais distante", "Trabalho", LocalDate.now().plusDays(30));
        saveTask("Mais próxima", "Trabalho", LocalDate.now().plusDays(1));
        saveTask("Intermediária", "Trabalho", LocalDate.now().plusDays(10));

        mockMvc.perform(get("/tasks").param("sort", "dataLimite,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Mais próxima"))
                .andExpect(jsonPath("$.content[1].titulo").value("Intermediária"))
                .andExpect(jsonPath("$.content[2].titulo").value("Mais distante"))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void shouldReturnBadRequestWhenSortPropertyIsInvalid() throws Exception {
        mockMvc.perform(get("/tasks").param("sort", "inexistente,asc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFilterTasksByCategoria() throws Exception {
        saveTask("Relatório", "Trabalho", LocalDate.now().plusDays(1));
        saveTask("Reunião", "Trabalho", LocalDate.now().plusDays(2));
        saveTask("Prova", "Estudo", LocalDate.now().plusDays(3));

        mockMvc.perform(get("/tasks").param("categoria", "Trabalho"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].categoria").value("Trabalho"))
                .andExpect(jsonPath("$.content[1].categoria").value("Trabalho"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldReturnEmptyListWhenCategoriaHasNoTasks() throws Exception {
        saveTask("Prova", "Estudo", LocalDate.now().plusDays(3));

        mockMvc.perform(get("/tasks").param("categoria", "Lazer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }
}
