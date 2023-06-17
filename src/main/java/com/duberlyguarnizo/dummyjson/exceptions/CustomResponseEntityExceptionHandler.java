package com.duberlyguarnizo.dummyjson.exceptions;

import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
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
    private final MessageSource messageSource;

    public CustomResponseEntityExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    //4xx errors
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleWrongAuthCredentials(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(401));
        if (e instanceof BadCredentialsException) {
            pd.setTitle(messageSource.getMessage("exception_wrong_credentials", null, request.getLocale()));
            pd.setDetail(messageSource.getMessage("exception_wrong_credentials_detail", null, request.getLocale()));
        } else if (e instanceof DisabledException) {
            pd.setTitle("User is disabled");
            pd.setDetail("The user cannot sign-in because is disabled.");
        } else if (e instanceof LockedException) {
            pd.setTitle("User is locked");
            pd.setDetail("The user cannot sign-in because is locked.");
        } else {
            pd.setTitle("Authentication Exception");
            pd.setDetail("A non-managed exception has occurred. Check the logs.");
        }
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        pd.setProperty(EXCEPTION_DETAIL_TEXT, e.getMessage()); //to be added on third-party exceptions

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
        pd.setTitle(messageSource.getMessage("exception_access_denied", null, request.getLocale()));
        pd.setDetail(messageSource.getMessage("exception_access_denied_detail", null, request.getLocale()));
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        pd.setProperty(EXCEPTION_DETAIL_TEXT, e.getMessage());
        return pd;
    }

    @ExceptionHandler(NotOwnedObjectException.class)
    public ProblemDetail handleNotTheOwnerException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(403));
        pd.setTitle(messageSource.getMessage("exception_not_the_owner", null, request.getLocale()));
        pd.setDetail(messageSource.getMessage("exception_not_the_owner_detail", null, request.getLocale()));
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        return pd;
    }


    @ExceptionHandler(IdNotFoundException.class)
    public ProblemDetail handleIdNotFoundException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(404));
        pd.setTitle(messageSource.getMessage("exception_id_not_found", null, request.getLocale()));
        pd.setDetail(e.getMessage());
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        return pd;
    }

//    @ExceptionHandler(HttpMessageNotReadableException.class)
//    public ProblemDetail handleHttpMessageNotReadableException(Exception e, WebRequest request) {
//        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(400));
//        pd.setTitle(messageSource.getMessage("exception_json_processing", null, request.getLocale()));
//        pd.setDetail(messageSource.getMessage("exception_json_processing_detail", null, request.getLocale()));
//        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
//        pd.setProperty(EXCEPTION_DETAIL_TEXT, e.getMessage());
//        return pd;
//    }

//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ProblemDetail handleArgumentNotValidException(Exception e, WebRequest request) {
//        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(400));
//        pd.setTitle(messageSource.getMessage("exception_argument_not_valid", null, request.getLocale()));
//        pd.setDetail(messageSource.getMessage("exception_argument_not_valid_detail", null, request.getLocale()));
//        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
//        pd.setProperty(EXCEPTION_DETAIL_TEXT, e.getMessage());
//        return pd;
//    }

    //5xx errors
    @ExceptionHandler(RepositoryException.class)
    public ProblemDetail handleRepositoryException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(500));
        pd.setTitle(messageSource.getMessage("exception_server_error", null, request.getLocale()));
        pd.setDetail(e.getMessage());
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        return pd;
    }

    @ExceptionHandler(JwtException.class)
    public ProblemDetail handleExpiredJwtException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(500));
        pd.setTitle(messageSource.getMessage("exception_jwt_processing", null, request.getLocale()));
        pd.setDetail(messageSource.getMessage("exception_jwt_processing_detail", null, request.getLocale()));
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        pd.setProperty(EXCEPTION_DETAIL_TEXT, e.getMessage());
        return pd;
    }

}
