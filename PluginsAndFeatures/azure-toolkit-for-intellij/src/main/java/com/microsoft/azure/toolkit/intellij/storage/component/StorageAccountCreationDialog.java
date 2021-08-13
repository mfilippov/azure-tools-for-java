/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.storage.component;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.lib.storage.model.Performance;

import java.awt.event.ItemEvent;
import java.util.Objects;

public class StorageAccountCreationDialog extends BaseStorageAccountCreationDialog {

    public StorageAccountCreationDialog(Project project) {
        super(project);
        this.performanceComboBox.addItemListener(this::onPerformanceChanged);
    }

    private void onPerformanceChanged(final ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            final Performance performance = (Performance) e.getItem();
            final boolean kindVisible = Objects.equals(Performance.PREMIUM, performance);
            this.kindComboBox.setVisible(kindVisible);
            this.kindLabel.setVisible(kindVisible);
        }
    }
}
