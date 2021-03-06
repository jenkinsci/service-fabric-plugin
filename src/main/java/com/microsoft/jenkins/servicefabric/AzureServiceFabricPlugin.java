/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.jenkins.servicefabric;

import com.microsoft.jenkins.azurecommons.telemetry.AppInsightsClientFactory;
import com.microsoft.jenkins.azurecommons.telemetry.AzureHttpRecorder;
import com.microsoft.jenkins.servicefabric.util.Constants;
import hudson.Plugin;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AzureServiceFabricPlugin extends Plugin {
    public static void sendEvent(String action, String... properties) {
        final Map<String, String> props = new HashMap<>();
        for (int i = 1; i < properties.length; i += 2) {
            props.put(properties[i - 1], properties[i]);
        }
        sendEvent(Constants.AI_SERVICE_FABRIC, action, props);
    }

    public static void sendEvent(String item, String action, Map<String, String> properties) {
        AppInsightsClientFactory.getInstance(AzureServiceFabricPlugin.class)
                .sendEvent(item, action, properties, false);
    }

    public static class AzureTelemetryInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            final Request request = chain.request();
            final Response response = chain.proceed(request);
            new AzureHttpRecorder(AppInsightsClientFactory.getInstance(AzureServiceFabricPlugin.class))
                    .record(new AzureHttpRecorder.HttpRecordable()
                            .withHttpCode(response.code())
                            .withHttpMessage(response.message())
                            .withHttpMethod(request.method())
                            .withRequestUri(request.url().uri())
                            .withRequestId(response.header("x-ms-request-id"))
                    );
            return response;
        }
    }
}
