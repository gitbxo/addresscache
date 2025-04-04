package org.bxo.address.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

//@SuppressWarnings({"unchecked","rawtypes"})
@ControllerAdvice
public class AddressExceptionHandler extends ResponseEntityExceptionHandler {
	@ExceptionHandler(Exception.class)
	public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
		// List<String> details = new ArrayList<>();
		// details.add(ex.getLocalizedMessage());
		// ErrorResponse error = new ErrorResponse("Server Error", details);
		return new ResponseEntity<Object>("Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public final ResponseEntity<Object> handleIllegalArgument(Throwable t, WebRequest request) {
		// List<String> details = new ArrayList<>();
		// details.add(ex.getLocalizedMessage());
		// ErrorResponse error = new ErrorResponse("Server Error", details);
		return new ResponseEntity<Object>("Invalid Request", HttpStatus.BAD_REQUEST);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		// {
		// "message": "Record Not Found",
		// "details": [
		// "Invalid employee id : 23"
		// ]
		// }

		// import org.springframework.validation.ObjectError;
		// List<String> details = new ArrayList<>();
		// for (ObjectError error : ex.getBindingResult().getAllErrors()) {
		// details.add(error.getDefaultMessage());
		// }
		// ErrorResponse error = new ErrorResponse("Invalid Request", details);
		return new ResponseEntity<Object>("Invalid Request", HttpStatus.BAD_REQUEST);
	}
}
