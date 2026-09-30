package br.ifsp.taskmaster.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import br.ifsp.taskmaster.dto.page.PagedResponse;
import br.ifsp.taskmaster.dto.task.TaskRequestDTO;
import br.ifsp.taskmaster.dto.task.TaskResponseDTO;
import br.ifsp.taskmaster.exception.BusinessException;
import br.ifsp.taskmaster.exception.ResourceNotFoundException;
import br.ifsp.taskmaster.mapper.PagedResponseMapper;
import br.ifsp.taskmaster.model.Task;
import br.ifsp.taskmaster.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        taskService = new TaskService(taskRepository, modelMapper, new PagedResponseMapper(modelMapper));
    }

    private TaskRequestDTO validRequest() {
        return new TaskRequestDTO("Estudar Spring", "Revisar testes", "Estudo", LocalDate.now().plusDays(5));
    }

    private Task existingTask(Long id, String categoria) {
        Task task = new Task();
        task.setId(id);
        task.setTitulo("Tarefa " + id);
        task.setDescricao("Descrição " + id);
        task.setCategoria(categoria);
        task.setDataLimite(LocalDate.now().plusDays(id));
        task.setDataCriacao(LocalDateTime.now().minusDays(1));
        return task;
    }

    @Test
    void shouldCreateTaskWithValidData() {
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(1L);
            return task;
        });

        TaskResponseDTO response = taskService.createTask(validRequest());

        assertEquals(1L, response.getId());
        assertEquals("Estudar Spring", response.getTitulo());
        assertEquals("Estudo", response.getCategoria());
        assertTrue(response.getDataCriacao() != null);
    }

    @Test
    void shouldNotCreateTaskWithDataLimiteInThePast() {
        TaskRequestDTO dto = validRequest();
        dto.setDataLimite(LocalDate.now().minusDays(1));

        BusinessException ex = assertThrows(BusinessException.class, () -> taskService.createTask(dto));

        assertTrue(ex.getMessage().contains("dataLimite"));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void shouldUpdateExistingTask() {
        Task task = existingTask(1L, "Trabalho");
        LocalDateTime dataCriacaoOriginal = task.getDataCriacao();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDTO response = taskService.updateTask(1L, validRequest());

        assertEquals(1L, response.getId());
        assertEquals("Estudar Spring", response.getTitulo());
        assertEquals("Estudo", response.getCategoria());
        assertEquals(dataCriacaoOriginal, response.getDataCriacao());
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonexistentTask() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.updateTask(99L, validRequest()));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void shouldNotUpdateTaskWithDataLimiteInThePast() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask(1L, "Trabalho")));
        TaskRequestDTO dto = validRequest();
        dto.setDataLimite(LocalDate.now().minusDays(3));

        assertThrows(BusinessException.class, () -> taskService.updateTask(1L, dto));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void shouldDeleteExistingTask() {
        Task task = existingTask(1L, "Trabalho");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.deleteTask(1L);

        verify(taskRepository).delete(task);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonexistentTask() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(99L));
        verify(taskRepository, never()).delete(any());
    }

    @Test
    void shouldListAllTasksWithPaginationMetadata() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<Task> page = new PageImpl<>(List.of(existingTask(1L, "Trabalho"), existingTask(2L, "Estudo")),
                pageable, 3);
        when(taskRepository.findAll(pageable)).thenReturn(page);

        PagedResponse<TaskResponseDTO> response = taskService.getAllTasks(null, pageable);

        assertEquals(2, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(3, response.getTotalElements());
        assertEquals(2, response.getTotalPages());
    }

    @Test
    void shouldFilterTasksByCategoria() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> page = new PageImpl<>(List.of(existingTask(1L, "Trabalho")), pageable, 1);
        when(taskRepository.findByCategoriaIgnoreCase("Trabalho", pageable)).thenReturn(page);

        PagedResponse<TaskResponseDTO> response = taskService.getAllTasks("Trabalho", pageable);

        assertEquals(1, response.getContent().size());
        assertEquals("Trabalho", response.getContent().get(0).getCategoria());
        verify(taskRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void shouldReturnEmptyPageWhenCategoriaHasNoTasks() {
        Pageable pageable = PageRequest.of(0, 10);
        when(taskRepository.findByCategoriaIgnoreCase("Lazer", pageable)).thenReturn(Page.empty(pageable));

        PagedResponse<TaskResponseDTO> response = taskService.getAllTasks("Lazer", pageable);

        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getTotalElements());
    }

    @Test
    void shouldGetTaskById() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask(1L, "Trabalho")));

        TaskResponseDTO response = taskService.getTaskById(1L);

        assertEquals(1L, response.getId());
    }

    @Test
    void shouldThrowNotFoundWhenGettingNonexistentTask() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(99L));
    }
}
