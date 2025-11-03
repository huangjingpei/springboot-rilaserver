package net.enjoy.springboot.registrationlogin.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name = "update_packages")
public class UpdatePackage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "版本号不能为空")
    @Size(max = 50, message = "版本号长度不能超过50个字符")
    @Column(unique = true)
    private String version;

    @NotBlank(message = "应用标识符不能为空")
    @Size(max = 100, message = "应用标识符长度不能超过100个字符")
    private String appId; // 关联的应用标识符

    @NotBlank(message = "平台信息不能为空")
    @Size(max = 50, message = "平台信息长度不能超过50个字符")
    private String platform;

    @NotBlank(message = "文件URL不能为空")
    @Size(max = 512, message = "文件URL长度不能超过512个字符")
    private String fileUrl;

    @NotBlank(message = "文件哈希值不能为空")
    @Size(max = 128, message = "文件哈希值长度不能超过128个字符")
    private String fileHash;

    @NotBlank(message = "哈希算法不能为空")
    @Size(max = 20, message = "哈希算法长度不能超过20个字符")
    private String hashAlgorithm;

    @Column(columnDefinition = "TEXT")
    private String releaseNotes;

    @NotNull(message = "是否强制更新不能为空")
    private Boolean isMandatory = false;

    @NotNull(message = "发布日期不能为空")
    private Instant releaseDate;

    @NotNull(message = "是否激活不能为空")
    private Boolean isActive = true;

    @Size(max = 100, message = "文件名长度不能超过100个字符")
    private String fileName;

    @NotNull(message = "文件大小不能为空")
    private Long fileSize; // 文件大小（字节）

    @Size(max = 50, message = "文件类型长度不能超过50个字符")
    private String fileType; // 文件MIME类型

    @Size(max = 200, message = "描述长度不能超过200个字符")
    private String description;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (releaseDate == null) {
            releaseDate = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
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

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    public Boolean getIsMandatory() {
        return isMandatory;
    }

    public void setIsMandatory(Boolean mandatory) {
        isMandatory = mandatory;
    }

    public Instant getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Instant releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
} 