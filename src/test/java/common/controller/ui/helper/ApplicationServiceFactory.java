package common.controller.ui.helper;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.controller.restapi.client.application.AllApplicationsUiServiceClient;
import common.controller.restapi.client.application.ApplicationUiServiceClient;
import common.controller.restapi.client.config.EUMConfigurationUIServiceClient;
import common.controller.ui.modal.Application;
import common.controller.ui.modal.ApplicationType;
import common.controller.ui.modal.EUMAppKeyInfo;

import static org.junit.Assert.assertTrue;

/**
 * Created by akhilesh.singh on 6/15/16.
 */
@Singleton
public class ApplicationServiceFactory {
    private final Application application;
    private final AllApplicationsUiServiceClient allApplicationsUiServiceClient;
    private final ApplicationUiServiceClient applicationUiServiceClient;
    private final EUMConfigurationUIServiceClient eumConfigurationUIServiceClient;
    @Inject
    public ApplicationServiceFactory(Application application,
                                     AllApplicationsUiServiceClient allApplicationsUiServiceClient,
                                     ApplicationUiServiceClient applicationUiServiceClient,
                                     EUMConfigurationUIServiceClient eumConfigurationUIServiceClient) {

        this.application = application;
        this.allApplicationsUiServiceClient = allApplicationsUiServiceClient;
        this.applicationUiServiceClient = applicationUiServiceClient;
        this.eumConfigurationUIServiceClient = eumConfigurationUIServiceClient;
    }

    public Application createApplication() {
        JsonObject jsonObject;
        try {
            jsonObject = applicationUiServiceClient.getApplicationByName(application.getName());
            if (jsonObject == null) {
                jsonObject = allApplicationsUiServiceClient.createApplication(application, ApplicationType
                        .MOBILE);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Not able to create Application");
        }
        assertTrue(jsonObject != null);
        assertTrue(jsonObject.get("name").getAsString().equals(application.getName()));
        application.setApplicationId(jsonObject.get("id").getAsLong());
        return application;
    }

    public String getAppKey() {
        EUMAppKeyInfo eumAppkeyInfo = eumConfigurationUIServiceClient.getEUMAppKey(String.valueOf
                (application.getApplicationId()));
        return eumAppkeyInfo.getAppKey();
    }
}
