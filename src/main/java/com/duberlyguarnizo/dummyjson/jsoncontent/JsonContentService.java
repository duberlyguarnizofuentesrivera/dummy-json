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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import static com.duberlyguarnizo.dummyjson.util.ControllerUtils.getRequestLocale;

@Log
@Service
public class JsonContentService {

    private final JsonContentRepository repository;
    private final JsonContentMapper mapper;
    private final CustomAuditorAware auditorAware;
    private final MessageSource messages;


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
            throw new IdNotFoundException(messages.getMessage("exception_json_id_not_found_detail", array, getRequestLocale()));
        }
        return mapper.toDetailDto(json.get());
    }


    public Page<JsonContentBasicDto> getByName(@Valid String name, Pageable page) {
        var json = repository.findByNameContainsIgnoreCase(name, page);
        return json.map(mapper::toBasicDto);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public Page<JsonContentBasicDto> getAllByUserId(Long id, Pageable page) {
        var jsonList = repository.findAllByCreatedBy(id, page);
        return jsonList.map(mapper::toBasicDto);
    }

    @PreAuthorize("isAuthenticated()")
    public Page<JsonContentBasicDto> getAllByCurrentUser(Pageable page) {
        var currentAuditor = auditorAware.getCurrentAuditor();
        if (currentAuditor.isEmpty()) {
            throw new AccessDeniedException(messages.getMessage("error_list_no_permissions", null, getRequestLocale()));
        } else {
            Long currentUserId = currentAuditor.get();
            return repository.findAllByCreatedBy(currentUserId, page)
                    .map(mapper::toBasicDto);
        }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')") //Only admins and supervisors can list all JSONs
    public Page<JsonContentBasicDto> getAllByAnyUser(Pageable page) {
        return repository.findAll(page)
                .map(mapper::toBasicDto);
    }

    @PreAuthorize("isAuthenticated()")
    public Long create(@Valid JsonContentCreationDto jsonDto) {
        var currentAuditorId = auditorAware
                .getCurrentAuditor()
                .orElseThrow(() -> new AccessDeniedException("You do not have the permissions to validate this resource."));
        boolean isUnique = repository.findByNameIgnoreCaseAndCreatedBy(jsonDto.getName(), currentAuditorId, PageRequest.ofSize(0)).isEmpty();
        if (!isUnique) {
            throw new RepositoryException("The name is not unique.");
        }
        var json = mapper.toEntity(jsonDto);
        try {
            var savedJson = repository.save(json);
            return savedJson.getId();
        } catch (RuntimeException e) {
            throw new RepositoryException("Error while trying to save the JSON content in database.");
        }
    }

    @PreAuthorize("isAuthenticated()")
    public void updateOwnJsonContent(@Valid JsonContentCreationDto jsonDto) {
        //We could use @PostAuthorize, but that would remove our ability to throw ProblemDetail exceptions
        var currentAuditorId = auditorAware
                .getCurrentAuditor()
                .orElseThrow(() -> new AccessDeniedException("You do not have the permissions to update this resource."));

        var jsonContent = repository.findById(jsonDto.getId()).orElseThrow(() -> new IdNotFoundException("No JSON content found with id: " + jsonDto.getId() + " in the database"));
        if (!jsonContent.getCreatedBy().equals(currentAuditorId)) {
            throw new NotOwnedObjectException("You do not have the permissions to update this resource.");
        }

        boolean isUnique = repository
                .findByNameIgnoreCaseAndCreatedBy(jsonDto.getName(), currentAuditorId, PageRequest.ofSize(0))
                .isEmpty();
        //check for original entity name, that name can be duplicated
        if (!isUnique && !jsonContent.getName().equals(jsonDto.getName())) {
            throw new RepositoryException("The name is not unique.");
        }

        var updatedJson = mapper.partialUpdate(jsonDto, jsonContent);
        repository.save(updatedJson);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public void updateAnyJsonContent(@Valid JsonContentCreationDto jsonDto) {
        //We could use @PostAuthorize, but that would remove our ability to throw ProblemDetail exceptions
        var jsonContent = repository.findById(jsonDto.getId()).orElseThrow(() -> new IdNotFoundException("No JSON content found with id: " + jsonDto.getId() + " in the database"));
        var updatedJson = mapper.partialUpdate(jsonDto, jsonContent);
        repository.save(updatedJson);
    }

    @PreAuthorize("isAuthenticated()")
    public void deleteOwnJsonContent(Long id) {
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

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public void deleteAnyJsonContent(Long id) {
        var jsonContent = repository
                .findById(id)
                .orElseThrow(() -> new IdNotFoundException("No JSON content found with id: " + id + " in the database"));
        repository.deleteById(jsonContent.getId());
    }


}

