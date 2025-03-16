package ch.uzh.ifi.hase.soprafs24.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.HashMap;

@ControllerAdvice(annotations = RestController.class)
public class GlobalExceptionAdvice extends ResponseEntityExceptionHandler {

  private final Logger log = LoggerFactory.getLogger(GlobalExceptionAdvice.class);

  @ExceptionHandler(value = { IllegalArgumentException.class, IllegalStateException.class })
  protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) {
    String bodyOfResponse = "This should be application specific";
    return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.CONFLICT, request);
  }

  @ExceptionHandler(TransactionSystemException.class)
  public ResponseStatusException handleTransactionSystemException(Exception ex, HttpServletRequest request) {
    log.error("Request: {} raised {}", request.getRequestURL(), ex);
    return new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
  }

  // Keep this one disable for all testing purposes -> it shows more detail with
  // this one disabled
  @ExceptionHandler(HttpServerErrorException.InternalServerError.class)
  public ResponseStatusException handleException(Exception ex) {
    log.error("Default Exception Handler -> caught:", ex);
    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
  }


//ADDDED THIS FOR THE 4 TESTS
  @ExceptionHandler(ResponseStatusException.class)
public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
    log.error("Exception caught: " + ex.getReason(), ex);

    // Create a response body with the message from the exception
    Map<String, String> response = new HashMap<>();
    response.put("message", ex.getReason());

    return new ResponseEntity<>(response, ex.getStatus());
}

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
    // You can customize the error response here, for example:
    String bodyOfResponse = "User not found: " + ex.getMessage();

    // Log the exception for debugging purposes
    log.error("Exception caught: " + ex.getMessage());

    // Return a custom error response with HTTP status NOT_FOUND (404)
    return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
 }
}