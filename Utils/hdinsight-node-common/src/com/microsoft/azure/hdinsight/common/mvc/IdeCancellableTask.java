/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.hdinsight.common.mvc;

import java.util.concurrent.Executor;

public interface IdeCancellableTask extends Executor {
    void cancel();
}
