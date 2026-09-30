package br.ifsp.taskmaster.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.ifsp.taskmaster.model.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByCategoriaIgnoreCase(String categoria, Pageable pageable);
}
