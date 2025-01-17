/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.storage.action.explorer;

import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionView;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.storage.StorageAccount;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractAzureStorageExplorerHandler {

    private static final String STORAGE_EXPLORER_DOWNLOAD_URL = "https://go.microsoft.com/fwlink/?LinkId=723579";
    private static final String STORAGE_EXPLORER = "StorageExplorer";

    public void openResource(@Nonnull final StorageAccount storageAccount) {
        // Get resource url
        final String resourceUrl = "storageexplorer://v=1&accountid=" + storageAccount.getId() + "&subscriptionid=" + storageAccount.getSubscriptionId();
        // try launch with uri
        boolean result = launchStorageExplorerWithUri(storageAccount, resourceUrl);
        if (!result) {
            // fall back to launch with command
            launchStorageExplorerThroughCommand(storageAccount, resourceUrl);
        }
    }

    protected boolean launchStorageExplorerWithUri(@Nonnull final StorageAccount storageAccount, @Nonnull final String resourceUrl) {
        if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                final List<ProcessHandle> beforeLaunchProcesses = ProcessHandle.allProcesses().collect(Collectors.toList());
                Desktop.getDesktop().browse(URI.create(resourceUrl));
                final List<ProcessHandle> afterLaunchProcesses = ProcessHandle.allProcesses().collect(Collectors.toList());
                final Collection<ProcessHandle> newProcesses = CollectionUtils.removeAll(afterLaunchProcesses, beforeLaunchProcesses);
                return newProcesses.stream().map(ProcessHandle::info).map(ProcessHandle.Info::command)
                        .anyMatch(command -> StringUtils.containsAnyIgnoreCase(command.orElse(StringUtils.EMPTY), STORAGE_EXPLORER));
            } catch (IOException e) {
                log.info("failed to launch storage explorer from uri", e);
            }
        }
        return false;
    }

    protected void launchStorageExplorerThroughCommand(final StorageAccount storageAccount, final String resourceUrl) {
        try {
            // Get storage explorer path
            final String storageExplorerExecutable = getStorageExplorerExecutable();
            // Launch storage explorer with resource url
            if (StringUtils.isEmpty(storageExplorerExecutable) || !Files.exists(Path.of(storageExplorerExecutable))) {
                throw new AzureToolkitRuntimeException("Cannot find Azure Storage Explorer.");
            }
            launchStorageExplorer(storageExplorerExecutable, resourceUrl);
        } catch (final RuntimeException e) {
            throw new AzureToolkitRuntimeException("Failed to launch Azure Storage Explorer.", e, (Object[]) getStorageNotFoundActions(storageAccount));
        }
    }

    protected String getStorageExplorerExecutable() {
        final String storageExplorerPath = Azure.az().config().getStorageExplorerPath();
        return StringUtils.isEmpty(storageExplorerPath) ? getStorageExplorerExecutableFromOS() : storageExplorerPath;
    }

    protected Action<?>[] getStorageNotFoundActions(@Nonnull final StorageAccount storageAccount) {
        // Open in Azure Action
        final Consumer<Void> openInAzureConsumer = ignore -> AzureActionManager.getInstance()
                .getAction(ResourceCommonActionsContributor.OPEN_URL).handle(storageAccount.getPortalUrl() + "/storagebrowser");
        final ActionView.Builder openInAzureView = new ActionView.Builder("Open in Azure")
                .title(ignore -> AzureString.fromString("Open Storage account in Azure")).enabled(ignore -> true);
        final Action.Id<Void> OPEN = Action.Id.of("storage.open_portal_storage_browser");
        final Action<Void> openInAzureAction = new Action<>(OPEN, openInAzureConsumer, openInAzureView);
        openInAzureAction.setAuthRequired(false);
        // Download Storage Explorer
        final Consumer<Void> downloadConsumer = ignore ->
                AzureActionManager.getInstance().getAction(ResourceCommonActionsContributor.OPEN_URL).handle(STORAGE_EXPLORER_DOWNLOAD_URL);
        final ActionView.Builder downloadView = new ActionView.Builder("Download")
                .title(ignore -> AzureString.fromString("Download Azure Storage Explorer")).enabled(ignore -> true);
        final Action.Id<Void> DOWNLOAD = Action.Id.of("storage.download_explorer");
        final Action<Void> downloadAction = new Action<>(DOWNLOAD, downloadConsumer, downloadView);
        downloadAction.setAuthRequired(false);
        // Open Azure Settings Panel, and re-run
        final Action<Object> openSettingsAction = AzureActionManager.getInstance().getAction(ResourceCommonActionsContributor.OPEN_AZURE_SETTINGS);
        final Consumer<Void> configureConsumer = ignore -> {
            openSettingsAction.getHandler(null, null).accept(null, null); // Open Azure Settings Panel sync
            if (StringUtils.isNotBlank(Azure.az().config().getStorageExplorerPath())) {
                openResource(storageAccount);
            }
        };
        final ActionView.Builder configureView = new ActionView.Builder("Configure")
                .title(ignore -> AzureString.fromString("Configure path for Azure Storage Explorer")).enabled(ignore -> true);
        final Action.Id<Void> CONFIG = Action.Id.of("storage.config_explorer_path");
        final Action<Void> configureAction = new Action<>(CONFIG, configureConsumer, configureView);
        configureAction.setAuthRequired(false);
        return new Action[]{openInAzureAction, downloadAction, configureAction};
    }

    protected abstract String getStorageExplorerExecutableFromOS();

    protected abstract void launchStorageExplorer(final String explorer, String storageUrl);
}
