/*
 * Copyright (c) AppDynamics, Inc., and its affiliates
 * 2016
 * All Rights Reserved
 * THIS IS UNPUBLISHED PROPRIETARY CODE OF APPDYNAMICS, INC.
 * The copyright notice above does not evidence any actual or intended publication of such source code
 */

package common.controller.restapi.client.application;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.controller.ui.modal.ControllerInfo;
import common.controller.restapi.client.AControllerRestServiceClient;

/**
 * Client for ApplicationUiService to do application related operations through the REST UI like
 * getApplicationByName, setApplicationLevelAgentProperty, getTierIdByApplicationComponentName
 */
@Singleton
public class ApplicationUiServiceClient extends AControllerRestServiceClient {

    @Inject
    public ApplicationUiServiceClient(ControllerInfo controllerInfo) {
        super(controllerInfo);
    }

    @Override
    public String getServiceUrl() {
        return "applicationManagerUiBean";
    }

    /**
     * Get the application given an application name
     * @param applicationName
     * @return Returns Application Object for the given application name if it exists.
     */
    public JsonObject getApplicationByName(String applicationName) {
        final JsonObject application =
                (JsonObject) readGetUrl("applicationByName?applicationName=" + applicationName, new JsonObject());
        return application;
    }

    /**
     * Gets the application id given an application name
     * @param applicationName
     * @return Returns application id
     */
    public long getApplicationIdByName(String applicationName) {
        JsonObject application = getApplicationByName(applicationName);
        if (application == null) {
            throw new RuntimeException("Application " + applicationName + "not found");
        }
        return application.get("id").getAsLong();
    }
}
