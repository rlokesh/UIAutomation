/*
 * Copyright (c) AppDynamics, Inc., and its affiliates
 * 2016
 * All Rights Reserved
 * THIS IS UNPUBLISHED PROPRIETARY CODE OF APPDYNAMICS, INC.
 * The copyright notice above does not evidence any actual or intended publication of such source code
 */

package common.controller.restapi.client.config;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.controller.ui.modal.ControllerInfo;
import common.controller.ui.modal.EUMAppKeyInfo;
import common.controller.restapi.client.AControllerRestServiceClient;

/**
 * Created by akhilesh.singh on 4/4/16.
 */
@Singleton
public class EUMConfigurationUIServiceClient extends AControllerRestServiceClient {

    @Inject
    public EUMConfigurationUIServiceClient(ControllerInfo controllerInfo) {
        super(controllerInfo);
    }

    @Override
    public String getServiceUrl() {
        return "eumConfigurationUiService";
    }

    public EUMAppKeyInfo getEUMAppKey(String applicationID) {
        EUMAppKeyInfo EUMAppKey = (EUMAppKeyInfo) readGetUrl("getAppKey/" + applicationID, new EUMAppKeyInfo());
        return EUMAppKey;
    }
}
