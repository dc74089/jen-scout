package com.vegetarianbaconite.jenscout;

import io.swagger.client.ApiClient;

public class Utils {

    public static ApiClient api;

    static {
        api = new ApiClient();
        api.setApiKey(Secret.apiKey);
    }
}
