package com.microsoft.azure.toolkit.ide.guidance.task;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.toolkit.ide.guidance.ComponentContext;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceTask;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.account.IAccount;
import com.microsoft.azure.toolkit.lib.account.IAzureAccount;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;

import javax.annotation.Nonnull;

import static com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor.OPEN_URL;

public class OpenResourceInAzureTask implements GuidanceTask {
    private final ComponentContext taskContext;

    public OpenResourceInAzureTask(@Nonnull ComponentContext taskContext) {
        this.taskContext = taskContext;
    }

    @Override
    public void execute() {
        final String id = (String) taskContext.getParameter("webappId");
        final ResourceId resourceId = ResourceId.fromString(id);
        final IAccount account = Azure.az(IAzureAccount.class).account();
        final Subscription subscription = account.getSubscription(resourceId.subscriptionId());
        final String url = String.format("%s/#@%s/resource%s", account.portalUrl(), subscription.getTenantId(), id);
        AzureActionManager.getInstance().getAction(OPEN_URL).handle(url);
    }
}