/*
 * Copyright (c) AppDynamics, Inc., and its affiliates
 * 2016
 * All Rights Reserved
 * THIS IS UNPUBLISHED PROPRIETARY CODE OF APPDYNAMICS, INC.
 * The copyright notice above does not evidence any actual or intended publication of such source code
 */

package common.controller;

/**
 * Response Object from the restendpoint HTTP Call.
 */
public class DetailedResponse {
    private final int statusCode;
    private final String statusMessage;
    private final String detailedMessage;
    private final Object responseObject;

    public int statusCode() {
        return this.statusCode;
    }

    public String statusMessage() {
        return this.statusMessage;
    }

    public String detailedMessage() {
        return this.detailedMessage;
    }

    public Object responseObject() {
        return responseObject;
    }

    // Bare minimum for HTTP Status-Code 204: No Content.
    public DetailedResponse(int statusCode) {
        this.statusCode = statusCode;
        this.statusMessage = null;
        this.detailedMessage = null;
        this.responseObject = null;
    }

    public DetailedResponse(int statusCode, String statusMessage, String detailedMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.detailedMessage = detailedMessage;
        this.responseObject = null;
    }

    public DetailedResponse(int statusCode, String statusMessage, String detailedMessage, Object responseObject) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.detailedMessage = detailedMessage;
        this.responseObject = responseObject;
    }

    public DetailedResponse(DetailedResponse response, Object responseObject) {
        this.statusCode = response.statusCode;
        this.statusMessage = response.statusMessage;
        this.detailedMessage = response.detailedMessage;
        this.responseObject = responseObject;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("DetailedResponse{");
        sb.append("statusCode: ").append(statusCode);
        sb.append(", statusMessage: ").append(statusMessage);
        sb.append(", detailedMessage: ").append(detailedMessage);
        sb.append(", responseObject: ").append((responseObject == null ? "null" : responseObject.toString()));
        return sb.toString();
    }
}

