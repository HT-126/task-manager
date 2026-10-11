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
        return task(title, status, 1L);
    }

    private Task task(String title, TaskStatus status, Long ownerId) {
        Task t = new Task();
        t.setOwnerId(ownerId);
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
    void findByOwnerIdAndStatusReturnsOnlyMatching() {
        repository.save(task("a", TaskStatus.TODO));
        repository.save(task("b", TaskStatus.DONE));
        repository.save(task("other user's", TaskStatus.DONE, 2L));

        assertThat(repository.findByOwnerIdAndStatus(1L, TaskStatus.DONE))
                .extracting(Task::getTitle).containsExactly("b");
    }

    @Test
    void findByIdAndOwnerIdHidesOtherUsersTasks() {
        Task mine = repository.save(task("mine", TaskStatus.TODO, 1L));

        assertThat(repository.findByIdAndOwnerId(mine.getId(), 1L)).isPresent();
        assertThat(repository.findByIdAndOwnerId(mine.getId(), 2L)).isEmpty();
    }

    @Test
    void titleIsRequiredByDatabase() {
        assertThatThrownBy(() -> repository.saveAndFlush(task(null, TaskStatus.TODO)))
                .isInstanceOf(Exception.class);
    }
}
