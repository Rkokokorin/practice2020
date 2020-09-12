package com.tuneit.itc.commons.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;

import com.tuneit.itc.commons.model.rest.AttachedFile;
import com.tuneit.itc.commons.model.rest.AttachedFileType;
import com.tuneit.itc.commons.model.rest.ProductResponse;

public class WindowsPathNormalizer {

    private static Logger logger = LoggerFactory.getLogger(WindowsPathNormalizer.class);

    public static String normalizeUrl(String fileType, String path) {

        String normalized = normalizePath(path);
        switch (fileType) {
            case AttachedFileType
                .IMAGE_FILE_TYPE:
                return WindowsPathNormalizerProperties.getBaseUrl() +
                    WindowsPathNormalizerProperties.getImagesPath() + normalized;
            case AttachedFileType
                .COMMON_FILE_TYPE:
                return WindowsPathNormalizerProperties.getBaseUrl() +
                    WindowsPathNormalizerProperties.getCommonFilesPath() + normalized;
            case AttachedFileType
                .INTERNAL_FILE_TYPE:
                return WindowsPathNormalizerProperties.getBaseUrl() +
                    WindowsPathNormalizerProperties.getInternalPath() + normalized;
            default:
                logger.error("Unknown file type {0}", fileType);
                return WindowsPathNormalizerProperties.getUrlPathSeparator() + normalized;
        }
    }

    public static String normalizePath(String path) {
        return path.replace(WindowsPathNormalizerProperties.getWindowsPathSeparator(),
            WindowsPathNormalizerProperties.getUrlPathSeparator());
    }

    public static String getProductImageUrl(ProductResponse.ProductHitSource product) {
        if (product.getAttachedFiles() != null) {
            Optional<AttachedFile> imagePathOpt = product.getAttachedFiles()
                .stream()
                .filter(AttachedFile::isPicture)
                .findFirst();
            if (imagePathOpt.isPresent()) {
                return imagePathOpt.map(attachedFile ->
                        WindowsPathNormalizer.normalizeUrl(attachedFile.getFileType().getName(),
                            attachedFile.getFilePath()))
                    .orElseGet(WindowsPathNormalizer::getDefaultProductImageUrl);
            }
        }
        return getDefaultProductImageUrl();
    }

    public static List<String> getAllProductImagesUrl(ProductResponse.ProductHitSource product) {
        return getAllProductImagesUrl(product, true);
    }

    public static List<String> getAllProductImagesUrl(ProductResponse.ProductHitSource product,
                                                      boolean includePicture) {
        List<String> result = new ArrayList<>();
        if (product.getAttachedFiles() != null) {
            for (AttachedFile file : product.getAttachedFiles()) {
                boolean isImageType = file.getFileType().equals(AttachedFile.FileType.IMAGE);
                if (isImageType && !file.isPicture() || (file.isPicture() && includePicture)) {
                    result.add(WindowsPathNormalizer.normalizeUrl(file.getFileType().getName(), file.getFilePath()));
                }
            }
        }
        return result;
    }

    public static String extractName(String path) {
        String normalized = normalizePath(path);
        return normalized.substring(normalized.lastIndexOf(WindowsPathNormalizerProperties.getUrlPathSeparator()) + 1);
    }

    public static String getDefaultProductImageUrl() {
        return WindowsPathNormalizerProperties.getBaseUrl() + WindowsPathNormalizerProperties.getDefaultImagePath();
    }

    private WindowsPathNormalizer() {
        throw new UnsupportedOperationException("This is an utility class");
    }
}
