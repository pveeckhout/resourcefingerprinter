package com.honeyedoak.resourcefingerprinter.models;

import lombok.*;
import org.json.JSONObject;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ResourceFile {

    private String path;

    private String oldFingerprint;

    private String newFingerprint;

    public static ResourceFile fromInputJsonObject(JSONObject obj) {
        String path = obj.getString("path").replace("\\", "/");
        String oldFingerprint = obj.getString("fingerprint");

        return new ResourceFile(path, oldFingerprint, null);
    }

    public static JSONObject toOutputJsonObject(ResourceFile resourceFile) {
        return new JSONObject(new StringBuilder("{")
                .append("\"path\": ").append("\"" + resourceFile.getPath().replace("\\", "\\\\" ) + "\",")
                .append("\"oldFingerprint\": ").append("\"" + resourceFile.getOldFingerprint() + "\",")
                .append("\"fingerprint\": ").append("\"" + resourceFile.getNewFingerprint() + "\"")
                .append("}")
                .toString());
    }
}
