package com.interniq.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;

public class InternshipResponse {

    private Long id;

    // Renamed: title → jobTitle, company → companyName
    // Dashboard JS reads listing.jobTitle and listing.companyName
    private String jobTitle;
    private String companyName;

    private String location;
    private Boolean isRemote;
    private String applyUrl;
    private String source;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime postedAt;

    // ── Getters & Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    // Keep getTitle() as alias so nothing else breaks
    public String getTitle() { return jobTitle; }
    public void setTitle(String title) { this.jobTitle = title; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    // Keep getCompany() as alias so nothing else breaks
    public String getCompany() { return companyName; }
    public void setCompany(String company) { this.companyName = company; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Boolean getIsRemote() { return isRemote; }
    public void setIsRemote(Boolean isRemote) { this.isRemote = isRemote; }

    public String getApplyUrl() { return applyUrl; }
    public void setApplyUrl(String applyUrl) { this.applyUrl = applyUrl; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }
}
