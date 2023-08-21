/*
 * dummy-json
 * Copyright (c) 2023 Duberly Guarnizo Fuentes Rivera <duberlygfr@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.duberlyguarnizo.dummyjson.jsoncontent;

import com.duberlyguarnizo.dummyjson.appuser.AppUserRepository;
import com.duberlyguarnizo.dummyjson.auditing.CustomAuditorAware;
import com.duberlyguarnizo.dummyjson.exceptions.IdNotFoundException;
import com.duberlyguarnizo.dummyjson.exceptions.NotOwnedObjectException;
import com.duberlyguarnizo.dummyjson.exceptions.RepositoryException;
import com.duberlyguarnizo.dummyjson.jsoncontent.dto.JsonContentBasicDto;
import com.duberlyguarnizo.dummyjson.jsoncontent.dto.JsonContentCreationDto;
import com.duberlyguarnizo.dummyjson.jsoncontent.dto.JsonContentDetailDto;
import com.duberlyguarnizo.dummyjson.jsoncontent.dto.JsonContentMapper;
import com.duberlyguarnizo.dummyjson.util.ControllerUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JsonContentService {

    private final JsonContentRepository repository;
    private final AppUserRepository appUserRepository;
    private final JsonContentMapper mapper;
    private final CustomAuditorAware auditorAware;
    private final ControllerUtils utils;


    public JsonContentService(JsonContentRepository repository, AppUserRepository appUserRepository, JsonContentMapper mapper, CustomAuditorAware auditorAware, ControllerUtils utils) {
        this.repository = repository;
        this.appUserRepository = appUserRepository;
        this.mapper = mapper;
        this.auditorAware = auditorAware;

        this.utils = utils;
    }

    //CRUD
    public JsonContentDetailDto getById(Long id) {
        var json = repository.findById(id);
        if (json.isEmpty()) {
            Long[] array = {id};
            throw new IdNotFoundException(utils.getMessage("exception_json_id_not_found_detail", array));
        }
        return mapper.toDetailDto(json.get());
    }


    public Page<JsonContentBasicDto> getByName(@Valid String name, Pageable page) {
        var json = repository.findByNameContainsIgnoreCase(name, page);
        return json.map(mapper::toBasicDto);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public Page<JsonContentBasicDto> getAllByUserId(Long id, Pageable page) {
        var userExist = appUserRepository.existsById(id);
        if (!userExist) {
            log.warn("number of users: " + repository.count());
            repository.findAll().forEach(element -> log.warn("element with id: " + element.getId()));
            throw new IdNotFoundException("user with id " + id + " does not exist!"); //TODO: localize this
        }
        var jsonList = repository.findAllByCreatedBy(id, page);
        return jsonList.map(mapper::toBasicDto);
    }

    @PreAuthorize("isAuthenticated()")
    public Page<JsonContentBasicDto> getAllByCurrentUser(Pageable page) {
        var currentAuditor = auditorAware.getCurrentAuditor();
        if (currentAuditor.isEmpty()) {
            throw new AccessDeniedException(utils.getMessage("error_list_no_permissions"));
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
                .orElseThrow(() -> new AccessDeniedException(utils.getMessage("error_auditor_empty")));
        boolean isUnique = repository.findByNameIgnoreCaseAndCreatedBy(jsonDto.getName(), currentAuditorId, PageRequest.ofSize(2)).isEmpty();
        if (!isUnique) {
            throw new RepositoryException(utils.getMessage("exception_repository_save_error_unique_name_json"));
        }
        var json = mapper.toEntity(jsonDto);
        try {
            var savedJson = repository.save(json);
            return savedJson.getId();
        } catch (RuntimeException e) {
            throw new RepositoryException(utils.getMessage("exception_repository_save_error_invalid_json"));
        }
    }

    @PreAuthorize("isAuthenticated()")
    public void updateOwnJsonContent(Long jsonId, @Valid JsonContentCreationDto jsonDto) {
        //We could use @PostAuthorize, but that would remove our ability to throw ProblemDetail exceptions
        var currentAuditorId = auditorAware
                .getCurrentAuditor()
                .orElseThrow(() -> new AccessDeniedException(utils.getMessage("error_auditor_empty")));

        var jsonContent = repository
                .findById(jsonId)
                .orElseThrow(() -> new IdNotFoundException(
                        utils.getMessage("exception_id_not_found_json_detail", new Long[]{jsonId})));
        if (!jsonContent.getCreatedBy().equals(currentAuditorId)) {
            throw new NotOwnedObjectException(utils.getMessage("error_update_not_the_owner"));
        }

        boolean isUnique = repository
                .findByNameIgnoreCaseAndCreatedBy(jsonDto.getName(), currentAuditorId, PageRequest.ofSize(2))
                .isEmpty();
        //check for original entity name, that name can be duplicated
        if (!isUnique && !jsonContent.getName().equals(jsonDto.getName())) {
            throw new RepositoryException(utils.getMessage("exception_repository_save_error_unique_name_json"));
        }

        var updatedJson = mapper.partialUpdate(jsonDto, jsonContent);
        repository.save(updatedJson);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public void updateAnyJsonContent(Long jsonId, @Valid JsonContentCreationDto jsonDto) {
        //We could use @PostAuthorize, but that would remove our ability to throw ProblemDetail exceptions
        var jsonContent = repository
                .findById(jsonId)
                .orElseThrow(() -> new IdNotFoundException
                        (utils.getMessage("exception_id_not_found_json_detail", new Long[]{jsonId})));
        var updatedJson = mapper.partialUpdate(jsonDto, jsonContent);
        repository.save(updatedJson);
    }

    @PreAuthorize("isAuthenticated()")
    public void deleteOwnJsonContent(Long id) {
        var currentAuditorId = auditorAware
                .getCurrentAuditor()
                .orElseThrow(() -> new AccessDeniedException(utils.getMessage("error_auditor_empty")));
        var jsonContent = repository
                .findById(id)
                .orElseThrow(() -> new IdNotFoundException(utils.getMessage("exception_id_not_found_json_detail", new Long[]{id})));
        if (!jsonContent
                .getCreatedBy()
                .equals(currentAuditorId)) {
            throw new NotOwnedObjectException(utils.getMessage("error_delete_not_the_owner"));
        }
        repository.deleteById(id);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public void deleteAnyJsonContent(Long id) {
        var jsonContent = repository
                .findById(id)
                .orElseThrow(() -> new IdNotFoundException(utils.getMessage("exception_id_not_found_json_detail", new Long[]{id})));
        repository.deleteById(jsonContent.getId());
    }


}

