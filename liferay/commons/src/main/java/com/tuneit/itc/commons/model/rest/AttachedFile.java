package com.tuneit.itc.commons.model.rest;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

@Data
public class AttachedFile {
    @JsonProperty("ФайлКартинки")
    private boolean picture;
    @JsonProperty("ТипФайла")
    private FileType fileType;
    @JsonProperty("ПолныйПутьWindows")
    private String windowsFullPath;
    @JsonProperty("ФайлОписанияДляСайта")
    private boolean siteDescriptionFile;
    @JsonProperty("ПутьКФайлу")
    private String filePath;
    @JsonProperty("ВидФайла")
    private String fileKind;

    public enum FileType {
        IMAGE("Картинка"),
        REGULAR_FILE("Обычный файл");

        private final String name;

        FileType(String name) {
            this.name = name;
        }

        @JsonValue
        public String getName() {
            return this.name;
        }

    }
}
