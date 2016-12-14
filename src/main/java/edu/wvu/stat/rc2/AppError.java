package edu.wvu.stat.rc2;

public enum AppError implements RCError {
	DatabaseConnection(301, "Database connection failed");
	
	private int _code;
	private String _message;
	
	AppError(int code, String message) {
		_code = code;
		_message = message;
	}

	@Override
	public int getErrorCode() { return _code; }

	@Override
	public String getMessage() { return _message; }
}
