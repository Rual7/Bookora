package com.bookora.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(BookoraException.class)
	ProblemDetail handleApplication(BookoraException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
		if (ex instanceof FieldException fieldException) {
			problem.setProperty("errors", fieldException.getErrors());
		}
		return problem;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
		problem.setProperty("errors", errors);
		return problem;
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	ProblemDetail handleTooLarge(MaxUploadSizeExceededException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONTENT_TOO_LARGE, "The file is larger than 12 MB");
	}

	@ExceptionHandler(MissingServletRequestPartException.class)
	ProblemDetail handleMissingFile(MissingServletRequestPartException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Choose a file to upload");
	}

	@ExceptionHandler(AuthenticationException.class)
	ProblemDetail handleBadCredentials(AuthenticationException ex, HttpServletRequest request) {
		log.warn("Failed sign-in: client={}, reason={}", request.getRemoteAddr(), ex.getClass().getSimpleName());
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid email or password");
	}

}
