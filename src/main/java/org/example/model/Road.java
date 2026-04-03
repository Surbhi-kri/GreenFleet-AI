package org.example.model;

public class Road{
    private long roadId;
    private String toCity;
    private String fromCity;
    private double distanceKm;
    private String roadType;
    private String trafficLevel;
    private int speedLimit;

    public Road(long roadId,String fromCity,String toCity, double distanceKm,
                String roadType,String trafficLevel,int speedLimit)
    {
        this.roadId       = roadId;
        this.fromCity     = fromCity;
        this.toCity       = toCity;
        this.distanceKm   = distanceKm;
        this.roadType     = roadType;
        this.trafficLevel = trafficLevel;
        this.speedLimit   = speedLimit;
    }
    public long getRoadId()
    {
        return roadId;
    }
    public String getFromCity()
    {
        return fromCity;
    }
    public String getToCity()
    {
        return toCity;
    }
    public double getDistanceKm()
    {
        return distanceKm;
    }
    public String getRoadType()
    {
        return roadType;
    }
    public String getTrafficLevel()
    {
        return trafficLevel;
    }
    public int getSpeedLimit()
    {
        return speedLimit;
    }

    @Override
    public String toString(){
        return String.format("[%d] %s→%s | %.0fkm | %s | traffic=%s | %dkmph",
                roadId, fromCity, toCity, distanceKm,
                roadType, trafficLevel, speedLimit);
    }

}