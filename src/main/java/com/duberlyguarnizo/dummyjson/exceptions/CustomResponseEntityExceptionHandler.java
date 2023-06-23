package com.duberlyguarnizo.dummyjson.exceptions;

import com.duberlyguarnizo.dummyjson.util.ControllerUtils;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
    public ProblemDetail handleAccessDeniedException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(401));
        pd.setTitle(utils.getMessage("exception_auth_permission_error"));
        pd.setDetail(utils.getMessage("exception_auth_permission_error_detail"));
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        pd.setProperty(EXCEPTION_DETAIL_TEXT, e.getMessage());
        return pd;
    }

    @ExceptionHandler(NotOwnedObjectException.class)
    public ProblemDetail handleNotTheOwnerException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(403));
        pd.setTitle(utils.getMessage("exception_not_the_owner"));
        pd.setDetail(utils.getMessage("exception_not_the_owner_detail"));
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        pd.setProperty(EXCEPTION_DETAIL_TEXT, e.getMessage());
        return pd;
    }

    @ExceptionHandler(ForbiddenActionException.class)
    public ProblemDetail handleForbiddenActionException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(403));
        pd.setTitle(utils.getMessage("exception_forbidden_action"));
        pd.setDetail(utils.getMessage("exception_forbidden_action_detail"));
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        pd.setProperty(EXCEPTION_DETAIL_TEXT, e.getMessage());
        return pd;
    }


    @ExceptionHandler(IdNotFoundException.class)
    public ProblemDetail handleIdNotFoundException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(404));
        pd.setTitle(utils.getMessage("exception_id_not_found"));
        pd.setDetail(e.getMessage());
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        return pd;
    }

//    @ExceptionHandler(HttpMessageNotReadableException.class)
//    public ProblemDetail handleHttpMessageNotReadableException(Exception e, WebRequest request) {
//        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(400));
//        pd.setTitle(messageSource.getMessage("exception_json_processing", null.getLocale()));
//        pd.setDetail(messageSource.getMessage("exception_json_processing_detail", null.getLocale()));
//        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
//        pd.setProperty(EXCEPTION_DETAIL_TEXT, e.getMessage());
//        return pd;
//    }

//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ProblemDetail handleArgumentNotValidException(Exception e, WebRequest request) {
//        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(400));
//        pd.setTitle(messageSource.getMessage("exception_argument_not_valid", null.getLocale()));
//        pd.setDetail(messageSource.getMessage("exception_argument_not_valid_detail", null.getLocale()));
//        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
//        pd.setProperty(EXCEPTION_DETAIL_TEXT, e.getMessage());
//        return pd;
//    }

    //5xx errors
    @ExceptionHandler(RepositoryException.class)
    public ProblemDetail handleRepositoryException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(500));
        pd.setTitle(utils.getMessage("exception_server_error"));
        pd.setDetail(e.getMessage());
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        return pd;
    }

    @ExceptionHandler(JwtException.class)
    public ProblemDetail handleExpiredJwtException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(500));
        pd.setTitle(utils.getMessage("exception_jwt_processing"));
        pd.setDetail(utils.getMessage("exception_jwt_processing_detail"));
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        pd.setProperty(EXCEPTION_DETAIL_TEXT, e.getMessage());
        return pd;
    }

}
