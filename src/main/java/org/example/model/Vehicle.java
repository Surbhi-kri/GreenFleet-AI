package org.example.model;

public class Vehicle {

    private long   vehicleId;
    private long   companyId;
    private String registrationNo;
    private String vehicleType;
    private double fuelEfficiencyKmpl;
    private String fuelType;

    public Vehicle(long vehicleId, long companyId, String registrationNo,
                   String vehicleType, double fuelEfficiencyKmpl, String fuelType) {
        this.vehicleId          = vehicleId;
        this.companyId          = companyId;
        this.registrationNo     = registrationNo;
        this.vehicleType        = vehicleType;
        this.fuelEfficiencyKmpl = fuelEfficiencyKmpl;
        this.fuelType           = fuelType;
    }

    public long   getVehicleId()          { return vehicleId; }
    public long   getCompanyId()          { return companyId; }
    public String getRegistrationNo()     { return registrationNo; }
    public String getVehicleType()        { return vehicleType; }
    public double getFuelEfficiencyKmpl() { return fuelEfficiencyKmpl; }
    public String getFuelType()           { return fuelType; }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | %s | %.1f kmpl",
                vehicleId, registrationNo, vehicleType,
                fuelType, fuelEfficiencyKmpl);
    }
}