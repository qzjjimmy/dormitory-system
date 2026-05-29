package com.bishe.dormitory.model;

import java.time.LocalDateTime;

public class SysUser {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String role;
    private String phone;
    private String roomNo;
    private String gender;
    private String majorClass;
    private String sleepHabit;
    private String smoking;
    private String hobbies;
    private String cleanliness;
    private String gaming;
    private String snoring;
    private String returnTime;
    private String noiseTolerance;
    private String preferredRoomType;
    private String preferredBed;
    private LocalDateTime createdAt;

    public SysUser() {}

    public SysUser(String username, String password, String realName, String role, String phone, String roomNo) {
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.role = role;
        this.phone = phone;
        this.roomNo = roomNo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getMajorClass() { return majorClass; }
    public void setMajorClass(String majorClass) { this.majorClass = majorClass; }
    public String getSleepHabit() { return sleepHabit; }
    public void setSleepHabit(String sleepHabit) { this.sleepHabit = sleepHabit; }
    public String getSmoking() { return smoking; }
    public void setSmoking(String smoking) { this.smoking = smoking; }
    public String getHobbies() { return hobbies; }
    public void setHobbies(String hobbies) { this.hobbies = hobbies; }
    public String getCleanliness() { return cleanliness; }
    public void setCleanliness(String cleanliness) { this.cleanliness = cleanliness; }
    public String getGaming() { return gaming; }
    public void setGaming(String gaming) { this.gaming = gaming; }
    public String getSnoring() { return snoring; }
    public void setSnoring(String snoring) { this.snoring = snoring; }
    public String getReturnTime() { return returnTime; }
    public void setReturnTime(String returnTime) { this.returnTime = returnTime; }
    public String getNoiseTolerance() { return noiseTolerance; }
    public void setNoiseTolerance(String noiseTolerance) { this.noiseTolerance = noiseTolerance; }
    public String getPreferredRoomType() { return preferredRoomType; }
    public void setPreferredRoomType(String preferredRoomType) { this.preferredRoomType = preferredRoomType; }
    public String getPreferredBed() { return preferredBed; }
    public void setPreferredBed(String preferredBed) { this.preferredBed = preferredBed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
