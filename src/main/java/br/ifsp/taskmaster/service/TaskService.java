package br.ifsp.taskmaster.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.taskmaster.dto.page.PagedResponse;
import br.ifsp.taskmaster.dto.task.TaskRequestDTO;
import br.ifsp.taskmaster.dto.task.TaskResponseDTO;
import br.ifsp.taskmaster.exception.BusinessException;
import br.ifsp.taskmaster.exception.ResourceNotFoundException;
import br.ifsp.taskmaster.mapper.PagedResponseMapper;
import br.ifsp.taskmaster.model.Task;
import br.ifsp.taskmaster.repository.TaskRepository;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    public TaskService(TaskRepository taskRepository, ModelMapper modelMapper,
            PagedResponseMapper pagedResponseMapper) {
        this.taskRepository = taskRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }

    @Transactional
    public TaskResponseDTO createTask(TaskRequestDTO taskDto) {
        validateDataLimite(taskDto.getDataLimite());
        Task task = modelMapper.map(taskDto, Task.class);
        task.setDataCriacao(LocalDateTime.now());
        Task savedTask = taskRepository.save(task);
        return modelMapper.map(savedTask, TaskResponseDTO.class);
    }

    public PagedResponse<TaskResponseDTO> getAllTasks(String categoria, Pageable pageable) {
        Page<Task> tasks = (categoria == null || categoria.isBlank())
                ? taskRepository.findAll(pageable)
                : taskRepository.findByCategoriaIgnoreCase(categoria.trim(), pageable);
        return pagedResponseMapper.toPagedResponse(tasks, TaskResponseDTO.class);
    }

    public TaskResponseDTO getTaskById(Long id) {
        return modelMapper.map(findTaskOrThrow(id), TaskResponseDTO.class);
    }

    @Transactional
    public TaskResponseDTO updateTask(Long id, TaskRequestDTO taskDto) {
        Task existingTask = findTaskOrThrow(id);
        validateDataLimite(taskDto.getDataLimite());
        modelMapper.map(taskDto, existingTask);
        Task updatedTask = taskRepository.save(existingTask);
        return modelMapper.map(updatedTask, TaskResponseDTO.class);
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = findTaskOrThrow(id);
        taskRepository.delete(task);
    }

    private Task findTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada com o ID: " + id));
    }

    private void validateDataLimite(LocalDate dataLimite) {
        if (dataLimite.isBefore(LocalDate.now())) {
            throw new BusinessException("dataLimite: a data limite não pode estar no passado");
        }
    }
}
