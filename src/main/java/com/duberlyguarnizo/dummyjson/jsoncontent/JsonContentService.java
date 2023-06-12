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
import lombok.extern.java.Log;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.List;

@Log
@Service
public class JsonContentService {
    private final JsonContentRepository repository;
    private final JsonContentMapper mapper;
    private final CustomAuditorAware auditorAware;
    private final MessageSource messages;
    LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();


    public JsonContentService(JsonContentRepository repository, JsonContentMapper mapper, CustomAuditorAware auditorAware, MessageSource messages) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditorAware = auditorAware;
        this.messages = messages;
    }

    //CRUD
    public JsonContentDetailDto getById(Long id) {
        var json = repository.findById(id);
        if (json.isEmpty()) {
            Long[] array = {id};
            throw new IdNotFoundException(messages.getMessage("exception_json_id_not_found_detail", array, LocaleContextHolder.getLocaleContext().getLocale()));
        } else {
            return mapper.toDetailDto(json.get());
        }
    }

    public List<JsonContentBasicDto> getAllByCurrentUser() {
        var currentAuditor = auditorAware.getCurrentAuditor();
        if (currentAuditor.isEmpty()) {
            throw new AccessDeniedException(messages.getMessage("error_list_no_permissions", null, LocaleContextHolder.getLocaleContext().getLocale()));
        } else {
            Long currentUserId = currentAuditor.get();
            return repository.findAllByCreatedBy(currentUserId)
                    .stream()
                    .map(mapper::toBasicDto)
                    .toList();
        }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')") //Only admins and supervisors can list all JSONs
    public List<JsonContentBasicDto> getAllByAnyUser() {
        return repository.findAll()
                .stream()
                .map(mapper::toBasicDto)
                .toList();
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
        var currentAuditorId = auditorAware
                .getCurrentAuditor()
                .orElseThrow(() -> new AccessDeniedException("You do not have the permissions to update this resource."));

        var jsonContent = repository.findById(jsonDto.getId()).orElseThrow(() -> new IdNotFoundException("No JSON content found with id: " + jsonDto.getId() + " in the database"));
        if (!jsonContent.getCreatedBy().equals(currentAuditorId)) {
            throw new NotOwnedObjectException("You do not have the permissions to update this resource.");
        }
        var updatedJson = mapper.partialUpdate(jsonDto, jsonContent);
        repository.save(updatedJson);
    }


    public void delete(Long id) {
        var currentAuditorId = auditorAware
                .getCurrentAuditor()
                .orElseThrow(() -> new AccessDeniedException("You do not have the permissions to delete this resource."));
        var jsonContent = repository
                .findById(id)
                .orElseThrow(() -> new IdNotFoundException("No JSON content found with id: " + id + " in the database"));
        if (!jsonContent
                .getCreatedBy()
                .equals(currentAuditorId)) {
            throw new NotOwnedObjectException("You do not have the permissions to delete this resource.");
        }
        repository.deleteById(id);
    }
}

