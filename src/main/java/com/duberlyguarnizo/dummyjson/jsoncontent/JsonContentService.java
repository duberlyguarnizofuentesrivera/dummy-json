package com.duberlyguarnizo.dummyjson.jsoncontent;

import com.duberlyguarnizo.dummyjson.auditing.CustomAuditorAware;
import com.duberlyguarnizo.dummyjson.exceptions.IdNotFoundException;
import com.duberlyguarnizo.dummyjson.exceptions.NotOwnedObjectException;
import com.duberlyguarnizo.dummyjson.exceptions.RepositoryException;
import com.duberlyguarnizo.dummyjson.jsoncontent.dto.JsonContentBasicDto;
import com.duberlyguarnizo.dummyjson.jsoncontent.dto.JsonContentCreationDto;
import com.duberlyguarnizo.dummyjson.jsoncontent.dto.JsonContentDetailDto;
import com.duberlyguarnizo.dummyjson.jsoncontent.dto.JsonContentMapper;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JsonContentService {
    private final JsonContentRepository repository;
    private final JsonContentMapper mapper;
    private final CustomAuditorAware auditorAware;

    public JsonContentService(JsonContentRepository repository, JsonContentMapper mapper, CustomAuditorAware auditorAware) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditorAware = auditorAware;
    }

    //CRUD
    public JsonContentDetailDto getById(Long id) {
        var json = repository.findById(id);
        if (json.isEmpty()) {
            throw new IdNotFoundException("No JSON content found with id: " + id + " in the database");
        } else {
            return mapper.toDetailDto(json.get());
        }
    }

    public List<JsonContentBasicDto> getAllByCurrentUser() {
        var currentAuditor = auditorAware.getCurrentAuditor();
        if (currentAuditor.isEmpty()) {
            throw new AccessDeniedException("You do not have the permissions to list this resource.");
        } else {
            Long currentUserId = currentAuditor.get();
            return repository.findAllByCreatedBy(currentUserId)
                    .stream()
                    .map(mapper::toBasicDto)
                    .toList();
        }
    }

    public Long create(@Valid JsonContentCreationDto jsonDto) {
        var json = mapper.toEntity(jsonDto);
        try {
            var savedJson = repository.save(json);
            return savedJson.getId();
        } catch (RuntimeException e) {
            throw new RepositoryException("Error while trying to save the JSON content in database.");
        }
    }

    public void update(@Valid JsonContentCreationDto jsonDto) {
        //TODO: refactor to use @POSTAUTHORIZE
        var currentAuditor = auditorAware.getCurrentAuditor();
        if (currentAuditor.isEmpty()) {
            throw new AccessDeniedException("You do not have the permissions to update this resource.");
        } else {
            var json = repository.findById(jsonDto.getId());
            if (json.isEmpty()) {
                throw new IdNotFoundException("No JSON content found with id: " + jsonDto.getId() + " in the database");
            } else {
                Long currentUserId = currentAuditor.get();
                JsonContent jsonObject = json.get();
                if (jsonObject.getCreatedBy() != currentUserId) {
                    throw new NotOwnedObjectException("You do not have the permissions to update this resource.");
                }
                var updatedJson = mapper.partialUpdate(jsonDto, jsonObject);
                repository.save(updatedJson);
            }
        }
    }

    public void delete(Long id) {
        var currentAuditor = auditorAware.getCurrentAuditor();
        if (currentAuditor.isEmpty()) {
            throw new AccessDeniedException("You do not have the permissions to delete this resource.");
        } else {
            var json = repository.findById(id);
            if (json.isEmpty()) {
                throw new IdNotFoundException("No JSON content found with id: " + id + " in the database");
            } else {
                Long currentUserId = currentAuditor.get();
                JsonContent jsonObject = json.get();
                if (jsonObject.getCreatedBy() != currentUserId) {
                    throw new NotOwnedObjectException("You do not have the permissions to delete this resource.");
                }
                repository.deleteById(id);
            }
        }
    }
}

