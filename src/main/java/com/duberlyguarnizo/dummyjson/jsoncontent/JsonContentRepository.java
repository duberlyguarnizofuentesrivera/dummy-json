package com.duberlyguarnizo.dummyjson.jsoncontent;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JsonContentRepository extends JpaRepository<JsonContent, Long> {
    public Page<JsonContent> findAllByCreatedBy(Long id, Pageable pageable);

    public Page<JsonContent> findByNameContainsIgnoreCase(String name, Pageable pageable);
}
