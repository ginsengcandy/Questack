package com.questack.source;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceRepository extends JpaRepository<Source, Long> {

    Optional<Source> findByName(String name);

    List<Source> findByActiveTrueOrderByPriorityAscNameAsc();
}
