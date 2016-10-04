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
import common.controller.ui.modal.Application;
import common.controller.ui.modal.ApplicationType;
import common.controller.ui.modal.ControllerInfo;
import common.controller.restapi.client.AControllerRestServiceClient;

public class AllApplicationsUiServiceClient extends AControllerRestServiceClient {

    @Inject
    public AllApplicationsUiServiceClient(ControllerInfo controllerInfo) {
        super(controllerInfo);
    }

    @Override
    public String getServiceUrl() {
        return "allApplications";
    }

    /**
     * Used by UI for displaying all apps list - card and grid views
     * @param timeRange
     */
    public JsonObject getAllApplicationDashboardItems(String timeRange) {
        JsonObject applicationDashboardItems = (JsonObject) readPostUrl("allApplicationsDashboard?time-range=" + timeRange,
                        "", new JsonObject());
        return applicationDashboardItems;
    }

    /**
     * Used by UI to create Application.
     * @param applicationType application type can be APM, WEB and MOBILE
     */
    public JsonObject createApplication(Application application, ApplicationType applicationType) {
        JsonObject result = (JsonObject) readPostUrl("createApplication?applicationType=" +
                applicationType.name(), application, new JsonObject());
        return result;
    }

    public String getFileUploadToken() {
        return (String) readGetUrl("getFileToken", new String());
    }
}
