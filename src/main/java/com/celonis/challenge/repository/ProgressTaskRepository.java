package com.celonis.challenge.repository;

import com.celonis.challenge.model.ProgressTask;
import com.celonis.challenge.model.ProjectGenerationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface ProgressTaskRepository extends JpaRepository<ProgressTask, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ProgressTask> findById(String id);
}
