package com.duberlyguarnizo.dummyjson.jsoncontent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JsonContentRepository extends JpaRepository<JsonContent, Long> {

}
