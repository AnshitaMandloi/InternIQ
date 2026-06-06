package com.interniq.entity;



import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
 
 
@Entity
@Table(name = "internship_listings")

public class InternshipListing {
 
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Boolean getIsRemote() {
		return isRemote;
	}

	public void setIsRemote(Boolean isRemote) {
		this.isRemote = isRemote;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getApplyUrl() {
		return applyUrl;
	}

	public void setApplyUrl(String applyUrl) {
		this.applyUrl = applyUrl;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public User getPostedBy() {
		return postedBy;
	}

	public void setPostedBy(User postedBy) {
		this.postedBy = postedBy;
	}

	public LocalDateTime getPostedAt() {
		return postedAt;
	}

	public void setPostedAt(LocalDateTime postedAt) {
		this.postedAt = postedAt;
	}

	public LocalDateTime getFetchedAt() {
		return fetchedAt;
	}

	public void setFetchedAt(LocalDateTime fetchedAt) {
		this.fetchedAt = fetchedAt;
	}

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
	@Column(name = "external_id", unique = true, length = 1000)
	private String externalId;
 
    @Column(nullable = false)
    private String title;
 
    @Column(nullable = false)
    private String company;
 
    private String location;
 
    @Column(name = "is_remote")
    private Boolean isRemote = false;
 
    @Column(columnDefinition = "TEXT")
    private String description;
 
    @Column(name = "apply_url", length = 500)
    private String applyUrl;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('JSEARCH','COMMUNITY')")
     private Source source = Source.JSEARCH;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posted_by")
    private User postedBy;
 
    @Column(name = "posted_at")
    private LocalDateTime postedAt;
 
    @Column(name = "fetched_at")
    private LocalDateTime fetchedAt = LocalDateTime.now();
 
    public enum Source {
        JSEARCH, COMMUNITY
    }
}
 