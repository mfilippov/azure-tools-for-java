/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.appservice.function;

import com.microsoft.azure.toolkit.ide.appservice.model.AppServiceConfig;
import com.microsoft.azure.toolkit.ide.appservice.model.ApplicationInsightsConfig;
import com.microsoft.azure.toolkit.ide.appservice.model.MonitorConfig;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.account.IAzureAccount;
import com.microsoft.azure.toolkit.lib.appservice.config.AppServicePlanConfig;
import com.microsoft.azure.toolkit.lib.appservice.config.RuntimeConfig;
import com.microsoft.azure.toolkit.lib.appservice.function.FunctionApp;
import com.microsoft.azure.toolkit.lib.appservice.model.JavaVersion;
import com.microsoft.azure.toolkit.lib.appservice.model.OperatingSystem;
import com.microsoft.azure.toolkit.lib.appservice.model.PricingTier;
import com.microsoft.azure.toolkit.lib.appservice.model.Runtime;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.resource.ResourceGroupConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class FunctionAppConfig extends AppServiceConfig {
    public static final Runtime DEFAULT_RUNTIME = Runtime.FUNCTION_WINDOWS_JAVA8;
    @Builder.Default
    protected Runtime runtime = DEFAULT_RUNTIME;

    public static FunctionAppConfig getFunctionAppDefaultConfig() {
        return getFunctionAppDefaultConfig(StringUtils.EMPTY);
    }

    public static FunctionAppConfig getFunctionAppDefaultConfig(final String name) {
        final String appName = StringUtils.isEmpty(name) ? String.format("app-%s", DATE_FORMAT.format(new Date())) :
            String.format("app-%s-%s", name, DATE_FORMAT.format(new Date()));
        final Subscription subscription = Azure.az(IAzureAccount.class).account().getSelectedSubscriptions().stream().findFirst().orElse(null);
        final String rgName = StringUtils.substring(String.format("rg-%s", appName), 0, RG_NAME_MAX_LENGTH);
        final Region region = AppServiceConfig.getDefaultRegion();
        final ResourceGroupConfig group = ResourceGroupConfig.builder().subscriptionId(subscription.getId()).name(rgName).region(region).build();
        final String planName = StringUtils.substring(String.format("sp-%s", appName), 0, SP_NAME_MAX_LENGTH);
        final AppServicePlanConfig plan = AppServicePlanConfig.builder()
            .subscriptionId(subscription.getId())
            .resourceGroupName(rgName)
            .name(planName)
            .region(region)
            .os(FunctionAppConfig.DEFAULT_RUNTIME.getOperatingSystem())
            .pricingTier(PricingTier.CONSUMPTION).build();
        final ApplicationInsightsConfig insightsConfig = ApplicationInsightsConfig.builder().name(appName).newCreate(true).build();
        final MonitorConfig monitorConfig = MonitorConfig.builder().applicationInsightsConfig(insightsConfig).build();
        return FunctionAppConfig.builder()
            .subscription(subscription)
            .resourceGroup(group)
            .name(appName)
            .servicePlan(plan)
            .runtime(FunctionAppConfig.DEFAULT_RUNTIME)
            .pricingTier(PricingTier.CONSUMPTION)
            .monitorConfig(monitorConfig)
                .region(region).build();
    }

    public static com.microsoft.azure.toolkit.lib.appservice.config.FunctionAppConfig convertToTaskConfig(FunctionAppConfig config) {
        final com.microsoft.azure.toolkit.lib.appservice.config.FunctionAppConfig result =
                new com.microsoft.azure.toolkit.lib.appservice.config.FunctionAppConfig();
        result.appName(config.getName());
        result.resourceGroup(config.getResourceGroupName());
        result.subscriptionId(config.getSubscriptionId());
        result.pricingTier(Optional.ofNullable(config.getServicePlan()).map(AppServicePlanConfig::getPricingTier).orElseGet(config::getPricingTier));
        result.region(config.getRegion());
        result.servicePlanName(Optional.ofNullable(config.getServicePlan()).map(AppServicePlanConfig::getName).orElse(null));
        result.servicePlanResourceGroup(Optional.ofNullable(config.getServicePlan())
            .map(AppServicePlanConfig::getResourceGroupName).orElseGet(config::getResourceGroupName));
        Optional.ofNullable(config.getRuntime()).ifPresent(runtime -> result.runtime(
            new RuntimeConfig().os(runtime.getOperatingSystem()).javaVersion(runtime.getJavaVersion()).webContainer(runtime.getWebContainer())));
        final ApplicationInsightsConfig insightsConfig = Optional.ofNullable(config.getMonitorConfig()).map(MonitorConfig::getApplicationInsightsConfig).orElse(null);
        result.disableAppInsights(insightsConfig == null);
        if (insightsConfig != null) {
            result.appInsightsInstance(insightsConfig.getName());
            result.appInsightsKey(insightsConfig.getInstrumentationKey());
        }
        result.appSettings(config.getAppSettings());
        return result;
    }

    @Override
    public Map<String, String> getTelemetryProperties() {
        final Map<String, String> result = super.getTelemetryProperties();
        result.put("runtime", Optional.ofNullable(runtime).map(Runtime::getOperatingSystem).map(OperatingSystem::getValue).orElse(StringUtils.EMPTY));
        result.put("functionJavaVersion", Optional.ofNullable(runtime).map(Runtime::getJavaVersion).map(JavaVersion::getValue).orElse(StringUtils.EMPTY));
        return result;
    }
}
