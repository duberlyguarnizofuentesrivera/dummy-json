package com.duberlyguarnizo.dummyjson.exceptions;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Log
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    @Value("${hostname}")
    private String hostname;
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
        //TODO: add more custom properties to be managed by frontend and the remaining exceptions related to credentials
        return pd;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(401));
        pd.setTitle(messageSource.getMessage("exception_access_denied", null, request.getLocale()));
        pd.setDetail(messageSource.getMessage("exception_access_denied_detail", null, request.getLocale()));
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        //TODO: add more custom properties to be managed by frontend and the remaining exceptions related to credentials
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


    //5xx errors
    @ExceptionHandler(RepositoryException.class)
    public ProblemDetail handleRepositoryException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(500));
        pd.setTitle(messageSource.getMessage("exception_server_error", null, request.getLocale()));
        pd.setDetail(e.getMessage());
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        return pd;
    }

    //TODO: add org.springframework.security.access.AccessDeniedException
    //TODO: add auth error (different roles)
    //TODO: add jsonwebtoken related exceptions (like ExpiredJwtException)
    //TODO: try to handle MethodArgumentNotValidException
}
