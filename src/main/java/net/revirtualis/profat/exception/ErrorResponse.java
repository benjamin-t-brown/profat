package net.revirtualis.profat.exception;

import java.util.List;

public class ErrorResponse {

	private int status;
	private String error;
	private String message;
	private List<FieldError> errors;

	public ErrorResponse() {
	}

	public ErrorResponse(int status, String error, String message) {
		this.status = status;
		this.error = error;
		this.message = message;
		this.errors = null;
	}

	public ErrorResponse(int status, String error, String message, List<FieldError> errors) {
		this.status = status;
		this.error = error;
		this.message = message;
		this.errors = errors;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<FieldError> getErrors() {
		return errors;
	}

	public void setErrors(List<FieldError> errors) {
		this.errors = errors;
	}

	public static final class FieldError {
		private String field;
		private String message;

		public FieldError(String field, String message) {
			this.field = field;
			this.message = message;
		}

		public String getField() {
			return field;
		}

		public String getMessage() {
			return message;
		}
	}
}
