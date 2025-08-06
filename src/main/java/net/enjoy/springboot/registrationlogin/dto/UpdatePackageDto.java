package net.enjoy.springboot.registrationlogin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class UpdatePackageDto {
    
    @NotBlank(message = "版本号不能为空")
    @Size(max = 50, message = "版本号长度不能超过50个字符")
    private String version;

    @NotBlank(message = "平台信息不能为空")
    @Size(max = 50, message = "平台信息长度不能超过50个字符")
    private String platform;

    @Size(max = 2000, message = "发布说明长度不能超过2000个字符")
    private String releaseNotes;

    @NotNull(message = "是否强制更新不能为空")
    private Boolean isMandatory = false;

    @Size(max = 200, message = "描述长度不能超过200个字符")
    private String description;

    @NotNull(message = "升级包文件不能为空")
    private MultipartFile file;

    // Getters and Setters
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
} 