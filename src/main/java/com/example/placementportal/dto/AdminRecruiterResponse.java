package com.example.placementportal.dto;

public class AdminRecruiterResponse {

    private Long id;
    private String username;
    private String recruiterName;
    private String companyName;
    private String email;
    private String phone;
    private boolean approved;

    public AdminRecruiterResponse() {}

    public AdminRecruiterResponse(Long id, String username, String recruiterName,
                                  String companyName, String email, String phone, boolean approved) {
        this.id = id;
        this.username = username;
        this.recruiterName = recruiterName;
        this.companyName = companyName;
        this.email = email;
        this.phone = phone;
        this.approved = approved;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRecruiterName() { return recruiterName; }
    public void setRecruiterName(String recruiterName) { this.recruiterName = recruiterName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
}
