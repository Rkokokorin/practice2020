package com.tuneit.itc.commons.util;

/**
 * Utility class for accessing ITC portal file share properties.
 *
 * @author kk
 */
public class WindowsPathNormalizerProperties extends Properties {
    private static WindowsPathNormalizerProperties instance = null;
    private String baseUrl = "http://94.230.57.30";
    private String defaultImagePath = "/itcmedia/default/product.jpg";
    private String imagesPath = "/itcmedia/images/";
    private String commonFilesPath = "/itcmedia/files/";
    private String internalPath = "/itcmedia/int/";
    private String windowsPathSeparator = "\\";
    private String urlPathSeparator = "/";

    private WindowsPathNormalizerProperties() {
        init();
    }

    private static WindowsPathNormalizerProperties getInstance() {
        if (null == instance) {
            instance = new WindowsPathNormalizerProperties();
        }
        return instance;
    }

    public static String getBaseUrl() {
        return getInstance().baseUrl;
    }

    public static String getDefaultImagePath() {
        return getInstance().defaultImagePath;
    }

    public static String getImagesPath() {
        return getInstance().imagesPath;
    }

    public static String getCommonFilesPath() {
        return getInstance().commonFilesPath;
    }

    public static String getInternalPath() {
        return getInstance().internalPath;
    }

    public static String getWindowsPathSeparator() {
        return getInstance().windowsPathSeparator;
    }

    public static String getUrlPathSeparator() {
        return getInstance().urlPathSeparator;
    }

    private void init() {
        baseUrl = getOrDefault("itc.media.share.baseurl", baseUrl);
        defaultImagePath = getOrDefault("itc.media.share.default.image.path",
            defaultImagePath);
        imagesPath = getOrDefault("itc.media.share.images.path", imagesPath);
        commonFilesPath = getOrDefault("itc.media.share.common-files.path",
            commonFilesPath);
        internalPath = getOrDefault("itc.media.share.internal.path",
            internalPath);
        windowsPathSeparator = getOrDefault("itc.media.share.windows.path.separator",
            windowsPathSeparator);
        urlPathSeparator = getOrDefault("itc.media.share.url.path.separator",
            urlPathSeparator);
    }
}
