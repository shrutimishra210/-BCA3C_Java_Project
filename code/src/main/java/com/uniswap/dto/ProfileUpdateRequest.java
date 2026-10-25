package com.uniswap.dto;

public class ProfileUpdateRequest {
    private String fullName;
    private String course;
    private String year;
    private String campus;
    private String bio;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
