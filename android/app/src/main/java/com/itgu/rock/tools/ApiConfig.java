package com.itgu.rock.tools;

public final class ApiConfig {

    private ApiConfig() {
    }

    /**
     * Android Emulator accesses the host machine through 10.0.2.2.
     *
     * For a physical device or production deployment,
     * replace this value with an address reachable by the device.
     */
    public static final String BASE_URL =
            "http://10.0.2.2:8081/";

    public static String url(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        return BASE_URL + path;
    }
}
