package com.example.placementportal.dto;

public class RecruiterProfileResponse {

    private Long id;
    private String recruiterName;
    private String companyName;
    private String email;
    private boolean approved;

    public RecruiterProfileResponse() {}

    public RecruiterProfileResponse(Long id, String recruiterName, String companyName, String email, boolean approved) {
        this.id = id;
        this.recruiterName = recruiterName;
        this.companyName = companyName;
        this.email = email;
        this.approved = approved;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRecruiterName() { return recruiterName; }
    public void setRecruiterName(String recruiterName) { this.recruiterName = recruiterName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
}
