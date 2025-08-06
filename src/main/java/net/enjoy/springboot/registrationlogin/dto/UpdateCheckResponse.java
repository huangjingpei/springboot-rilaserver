package net.enjoy.springboot.registrationlogin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

public class UpdateCheckResponse {
    private boolean hasUpdate;
    private String latestVersion;
    private String releaseNotes;
    private boolean isMandatory;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant releaseDate;
    
    private String downloadUrl;
    private String fileHash;
    private String hashAlgorithm;
    private Long fileSize;
    private String fileName;
    private String description;

    // 无更新时的构造函数
    public UpdateCheckResponse(boolean hasUpdate) {
        this.hasUpdate = hasUpdate;
    }

    // 有更新时的构造函数
    public UpdateCheckResponse(boolean hasUpdate, String latestVersion, String releaseNotes, 
                             boolean isMandatory, Instant releaseDate, String downloadUrl, 
                             String fileHash, String hashAlgorithm, Long fileSize, 
                             String fileName, String description) {
        this.hasUpdate = hasUpdate;
        this.latestVersion = latestVersion;
        this.releaseNotes = releaseNotes;
        this.isMandatory = isMandatory;
        this.releaseDate = releaseDate;
        this.downloadUrl = downloadUrl;
        this.fileHash = fileHash;
        this.hashAlgorithm = hashAlgorithm;
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.description = description;
    }

    // Getters and Setters
    public boolean isHasUpdate() {
        return hasUpdate;
    }

    public void setHasUpdate(boolean hasUpdate) {
        this.hasUpdate = hasUpdate;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public void setMandatory(boolean mandatory) {
        isMandatory = mandatory;
    }

    public Instant getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Instant releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
} 