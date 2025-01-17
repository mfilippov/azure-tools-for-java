/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.core.mvp.model.webapp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebAppOnLinuxDeployModel {
    private PrivateRegistryImageSetting privateRegistryImageSetting;
    private boolean creatingNewWebAppOnLinux;
    private String webAppId;
    private String webAppName;
    private String subscriptionId;
    private String resourceGroupName;
    private boolean creatingNewResourceGroup;
    private String locationName;
    private String pricingSkuTier;
    private String pricingSkuSize;
    private boolean creatingNewAppServicePlan;
    private String appServicePlanResourceGroupName;
    private String appServicePlanName;
    private String targetPath;
    private String targetName;
    private String dockerFilePath;

    public WebAppOnLinuxDeployModel() {
        privateRegistryImageSetting = new PrivateRegistryImageSetting();
    }
}
