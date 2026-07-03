package model;

/**
 * Represents a parking rate/price rule for a vehicle type.
 */
public class ParkingRate {

    private int rateID;
    private String vehicleType; // "MOTORBIKE" | "CAR"
    private String rateType;    // "HOURLY" | "MONTHLY"
    private double price;
    private String description;

    public ParkingRate() {
    }

    public ParkingRate(int rateID, String vehicleType, String rateType, double price, String description) {
        this.rateID = rateID;
        this.vehicleType = vehicleType;
        this.rateType = rateType;
        this.price = price;
        this.description = description;
    }

    public int getRateID() {
        return rateID;
    }

    public void setRateID(int rateID) {
        this.rateID = rateID;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getRateType() {
        return rateType;
    }

    public void setRateType(String rateType) {
        this.rateType = rateType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}