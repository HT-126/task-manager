package com.example.taskmanager.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

// @DataJpaTest: starts only the JPA layer with an in-memory H2 database, rolled back per test.
@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    TaskRepository repository;

    private Task task(String title, TaskStatus status) {
        Task t = new Task();
        t.setTitle(title);
        t.setStatus(status);
        return t;
    }

    @Test
    void saveSetsIdAndCreatedAtAndDefaultStatus() {
        Task t = new Task();
        t.setTitle("write plan");
        t.setDueDate(LocalDate.of(2026, 12, 1));
        Task saved = repository.saveAndFlush(t);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    void findByStatusReturnsOnlyMatching() {
        repository.save(task("a", TaskStatus.TODO));
        repository.save(task("b", TaskStatus.DONE));

        assertThat(repository.findByStatus(TaskStatus.DONE))
                .extracting(Task::getTitle).containsExactly("b");
    }

    @Test
    void titleIsRequiredByDatabase() {
        assertThatThrownBy(() -> repository.saveAndFlush(task(null, TaskStatus.TODO)))
                .isInstanceOf(Exception.class);
    }
}
