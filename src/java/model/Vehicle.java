package model;

import java.util.Date;

/**
 * Represents a Vehicle entity belonging to a customer (or a walk-in guest).
 */
public class Vehicle {

    private int vehicleID;
    private String licensePlate;
    private String vehicleType; // "MOTORBIKE" | "CAR"
    private String brand;
    private String color;
    private Integer ownerID; // nullable, links to User (customer)
    private String ownerName; // used when ownerID is null (walk-in guest)
    private String ownerPhone;
    private Date createdDate;

    public Vehicle() {
    }

    public Vehicle(int vehicleID, String licensePlate, String vehicleType, String brand,
            String color, Integer ownerID, String ownerName, String ownerPhone, Date createdDate) {
        this.vehicleID = vehicleID;
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.brand = brand;
        this.color = color;
        this.ownerID = ownerID;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
        this.createdDate = createdDate;
    }

    public int getVehicleID() {
        return vehicleID;
    }

    public void setVehicleID(int vehicleID) {
        this.vehicleID = vehicleID;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(Integer ownerID) {
        this.ownerID = ownerID;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}