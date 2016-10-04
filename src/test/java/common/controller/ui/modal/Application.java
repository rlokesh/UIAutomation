/*
 * Copyright (c) AppDynamics, Inc., and its affiliates
 * 2016
 * All Rights Reserved
 * THIS IS UNPUBLISHED PROPRIETARY CODE OF APPDYNAMICS, INC.
 * The copyright notice above does not evidence any actual or intended publication of such source code
 */

package common.controller.ui.modal;

import static org.junit.Assert.assertTrue;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Created by akhilesh.singh on 6/6/16.
 */
@Singleton
public class Application {

    private final String name;
    private String description;
    private transient long applicationId;

    @Inject
    public Application(@Named("applicationName") String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(long applicationId) {
        this.applicationId = applicationId;
    }
}
