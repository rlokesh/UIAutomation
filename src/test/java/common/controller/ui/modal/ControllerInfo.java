/*
 * Copyright (c) AppDynamics, Inc., and its affiliates
 * 2016
 * All Rights Reserved
 * THIS IS UNPUBLISHED PROPRIETARY CODE OF APPDYNAMICS, INC.
 * The copyright notice above does not evidence any actual or intended publication of such source code
 */

package common.controller.ui.modal;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by akhilesh.singh on 6/13/16.
 */
public class ControllerInfo {
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerInfo.class);
    public static final ControllerInfo DEFAULT_CONTROLLER_CLIENT = new ControllerInfoBuilder().build();
    private final String controllerHost;
    private final String controllerPort;
    private final String controllerAccount;
    private final String controllerUser;
    private final String controllerPassword;
    private final boolean controllerSslEnabled;

    private ControllerInfo(final String controllerHost, final String controllerPort, final String accountName,
            final String userName, final String password, final boolean controllerSslEnabled) {
        this.controllerHost = controllerHost;
        this.controllerPort = controllerPort;
        this.controllerAccount = accountName;
        this.controllerUser = userName;
        this.controllerPassword = password;
        this.controllerSslEnabled = controllerSslEnabled;
    }

    public String getControllerHost() {
        return controllerHost;
    }

    public String getControllerPort() {
        return controllerPort;
    }

    public String getControllerAccount() {
        return controllerAccount;
    }

    public String getControllerUser() {
        return controllerUser;
    }

    public String getControllerPassword() {
        return controllerPassword;
    }

    public String getControllerUrl() {
        return "http://" + controllerHost + ":" + controllerPort + "/controller";
    }

    public String getControllerLogin() {
        return getControllerUser() + "@" + getControllerAccount();
    }

    public boolean isControllerSslEnabled() {
        return controllerSslEnabled;
    }

    public static class ControllerInfoBuilder {
        public static String DEFAULT_HOST = "localhost";
        public static String DEFAULT_PORT = "8080";
        public static String DEFAULT_ACCOUNT = "customer1";
        public static String DEFAULT_USER = "user1";
        public static String DEFAULT_PWD = "welcome";
        private String controllerHost;
        private String controllerPort;
        private String controllerAccount;
        private String controllerUser;
        private String controllerPassword;
        private String controllerAccountKey;
        private boolean controllerSslEnabled;

        public ControllerInfoBuilder() {
            controllerHost = System.getProperty("controller.host");
            if (Strings.isNullOrEmpty(controllerHost)) {
                LOGGER.info("'controller.host' system property not set,Using the default '" + DEFAULT_HOST + "'");
                controllerHost = DEFAULT_HOST;
            }

            controllerPort = System.getProperty("controller.port");
            if (Strings.isNullOrEmpty(controllerPort)) {
                LOGGER.info("'controller.port' system property not set,Using the default '" + DEFAULT_PORT + "'");
                controllerPort = DEFAULT_PORT;
            }

            controllerAccount = System.getProperty("controller.account");
            if (Strings.isNullOrEmpty(controllerAccount)) {
                LOGGER.info("'controller.account' system property not set,Using the default '" + DEFAULT_ACCOUNT + "'");
                controllerAccount = DEFAULT_ACCOUNT;
            }

            controllerUser = System.getProperty("controller.user");
            if (Strings.isNullOrEmpty(controllerUser)) {
                LOGGER.info("'controller.user' system property not set,Using the default '" + DEFAULT_USER + "'");
                controllerUser = DEFAULT_USER;
            }

            controllerPassword = System.getProperty("controller.password");
            if (Strings.isNullOrEmpty(controllerPassword)) {
                LOGGER.info("'controller.password' system property not set,Using the default '" + DEFAULT_PWD + "'");
                controllerPassword = DEFAULT_PWD;
            }

            String controllerSslEnabledProperty = System.getProperty("controller.ssl.enabled");
            if (Strings.isNullOrEmpty(controllerSslEnabledProperty)) {
                LOGGER.info("'controller.ssl.enabled' system property not set, Using the default 'false'");
                controllerSslEnabled = false;
            } else {
                controllerSslEnabled = Boolean.parseBoolean(controllerSslEnabledProperty);
            }
        }

        public ControllerInfoBuilder controllerHost(String controllerHost) {
            this.controllerHost = controllerHost;
            return this;
        }

        public ControllerInfoBuilder controllerPort(String controllerPort) {
            this.controllerPort = controllerPort;
            return this;
        }

        public ControllerInfoBuilder controllerAccount(String controllerAccount) {
            this.controllerAccount = controllerAccount;
            return this;
        }

        public ControllerInfoBuilder controllerAccountKey(String controllerAccountKey) {
            this.controllerAccountKey = controllerAccountKey;
            return this;
        }

        public ControllerInfoBuilder controllerUser(String controllerUser) {
            this.controllerUser = controllerUser;
            return this;
        }

        public ControllerInfoBuilder controllerPassword(String controllerPassword) {
            this.controllerPassword = controllerPassword;
            return this;
        }

        public ControllerInfoBuilder controllerSslEnabled(boolean controllerSslEnabled) {
            this.controllerSslEnabled = controllerSslEnabled;
            return this;
        }

        public ControllerInfo build() {
            return new ControllerInfo(controllerHost, controllerPort, controllerAccount, controllerUser,
                    controllerPassword, controllerSslEnabled);
        }
    }
}
