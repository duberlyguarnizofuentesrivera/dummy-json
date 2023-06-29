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

package com.duberlyguarnizo.dummyjson.exceptions;

import com.duberlyguarnizo.dummyjson.util.ControllerUtils;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    @Value("${hostname}")
    private String hostname;
    private static final String EXCEPTION_DETAIL_TEXT = "exception";
    private static final String HOSTNAME_KEY_TEXT = "hostname";
    private final ControllerUtils utils;

    public CustomResponseEntityExceptionHandler(ControllerUtils utils) {
        this.utils = utils;
    }

    //4xx errors
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail handleWrongAuthCredentials(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(401));
        if (e instanceof BadCredentialsException) {
            pd.setTitle(utils.getMessage("exception_auth_wrong_credentials"));
            pd.setDetail(utils.getMessage("exception_auth_wrong_credentials_detail"));
        } else if (e instanceof DisabledException) {
            pd.setTitle(utils.getMessage("exception_auth_user_disabled"));
            pd.setDetail(utils.getMessage("exception_auth_user_disabled_detail"));
        } else if (e instanceof LockedException) {
            pd.setTitle(utils.getMessage("exception_auth_user_locked"));
            pd.setDetail(utils.getMessage("exception_auth_user_locked_detail"));
        } else {
            pd.setTitle(utils.getMessage("exception_auth_unknown_error"));
            pd.setDetail(utils.getMessage("exception_auth_unknown_error_detail"));
        }
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        pd.setProperty(EXCEPTION_DETAIL_TEXT, e.getMessage()); //to be added on third-party exceptions

        return pd;
    }

    @ExceptionHandler(JwtValidationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ProblemDetail handleJwtValidationException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(403));
        pd.setTitle(utils.getMessage("exception_jwt_revoked"));
        pd.setDetail(utils.getMessage("exception_jwt_revoked_detail"));
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        return pd;
    }

    /**
     * Handles the exception thrown when an authenticated user tries to access a resource that is permitted.
     *
     * @param e       the exception to handle, in this case a {@link AccessDeniedException}
     * @param request the web request that contains the 'Accept-Language' header
     * @return {@link ProblemDetail} with the status code {@link HttpStatusCode}.UNAUTHORIZED and the localized info about the problem
     * This <b>only works</b> with methods that have <b>@PreAuthorize</b> or similar annotations.
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ProblemDetail handleAccessDeniedException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(403));
        pd.setTitle(utils.getMessage("exception_auth_permission_error"));
        pd.setDetail(utils.getMessage("exception_auth_permission_error_detail"));
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        pd.setProperty(EXCEPTION_DETAIL_TEXT, e.getMessage());
        return pd;
    }

    @ExceptionHandler(NotOwnedObjectException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail handleNotTheOwnerException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(401));
        pd.setTitle(utils.getMessage("exception_not_the_owner"));
        pd.setDetail(utils.getMessage("exception_not_the_owner_detail"));
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        pd.setProperty(EXCEPTION_DETAIL_TEXT, e.getMessage());
        return pd;
    }

    @ExceptionHandler(ForbiddenActionException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail handleForbiddenActionException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(401));
        pd.setTitle(utils.getMessage("exception_forbidden_action"));
        pd.setDetail(utils.getMessage("exception_forbidden_action_detail"));
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        pd.setProperty(EXCEPTION_DETAIL_TEXT, e.getMessage());
        return pd;
    }


    @ExceptionHandler(IdNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleIdNotFoundException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(404));
        pd.setTitle(utils.getMessage("exception_id_not_found"));
        pd.setDetail(e.getMessage());
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        return pd;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDuplicatedValueException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(400)); //TODO: change to proper number
        pd.setTitle("required field exists");
        pd.setDetail("the field name or email or idCard already exists ");
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        pd.setProperty(EXCEPTION_DETAIL_TEXT, e.getMessage());
        return pd;
    }

    @ExceptionHandler(InvalidFieldValueException.class)
    public ProblemDetail handleFieldNotValidException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(400));
        pd.setTitle(utils.getMessage("error_invalid_body_field"));
        pd.setDetail(utils.getMessage("error_invalid_body_field_detail"));
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        pd.setProperty(EXCEPTION_DETAIL_TEXT, e.getMessage());
        return pd;
    }

    //5xx errors
    @ExceptionHandler(RepositoryException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleRepositoryException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(500));
        pd.setTitle(utils.getMessage("exception_server_error"));
        pd.setDetail(e.getMessage());
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        return pd;
    }

    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleExpiredJwtException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(500));
        pd.setTitle(utils.getMessage("exception_jwt_processing"));
        pd.setDetail(utils.getMessage("exception_jwt_processing_detail"));
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        pd.setProperty(EXCEPTION_DETAIL_TEXT, e.getMessage());
        return pd;
    }

}
