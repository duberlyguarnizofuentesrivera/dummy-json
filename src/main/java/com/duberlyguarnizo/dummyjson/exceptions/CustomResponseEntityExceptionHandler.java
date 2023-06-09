package com.duberlyguarnizo.dummyjson.exceptions;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
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

    //4xx errors
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleWrongAuthCredentials(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(401));
        if (e instanceof BadCredentialsException) {
            pd.setTitle("Wrong Credentials");
            pd.setDetail("Incorrect username or password");
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
        log.warning("Access denied exception: ENTERING EXCEPTION HANDLER");
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(401));
        pd.setTitle("Access denied");
        pd.setDetail("You don't have permission to perform this action");
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        //TODO: add more custom properties to be managed by frontend and the remaining exceptions related to credentials
        return pd;
    }

    @ExceptionHandler(NotOwnedObjectException.class)
    public ProblemDetail handleNotTheOwnerException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(403));
        pd.setTitle("Not the owner");
        pd.setDetail("The current user cannot perform the action because it is not the owner");
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        return pd;
    }


    @ExceptionHandler(IdNotFoundException.class)
    public ProblemDetail handleIdNotFoundException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(404));
        pd.setTitle("Id not found");
        pd.setDetail(e.getMessage());
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        return pd;
    }


    //5xx errors
    @ExceptionHandler(RepositoryException.class)
    public ProblemDetail handleRepositoryException(Exception e, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(500));
        pd.setTitle("Server error");
        pd.setDetail(e.getMessage());
        pd.setProperty(HOSTNAME_KEY_TEXT, hostname);
        return pd;
    }

    //TODO: add org.springframework.security.access.AccessDeniedException
    //TODO: add auth error (different roles)
    //TODO: add jsonwebtoken related exceptions (like ExpiredJwtException)
    //TODO: try to handle MethodArgumentNotValidException
}
