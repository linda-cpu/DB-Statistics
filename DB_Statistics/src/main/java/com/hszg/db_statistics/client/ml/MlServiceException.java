package com.hszg.db_statistics.client.ml;

import org.springframework.http.HttpStatusCode;

public class MlServiceException extends RuntimeException {
    private final HttpStatusCode statusCode;
    private final String errorBody;

    public MlServiceException(HttpStatusCode statusCode, String errorBody) {
        super(errorBody);
        this.statusCode = statusCode;
        this.errorBody = errorBody;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public String getErrorBody() {
        return errorBody;
    }
}