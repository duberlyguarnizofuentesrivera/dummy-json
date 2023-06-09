package com.duberlyguarnizo.dummyjson.jsoncontent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JsonContentRepository extends JpaRepository<JsonContent, Long> {
    public List<JsonContent> findAllByCreatedBy(Long id);
}
