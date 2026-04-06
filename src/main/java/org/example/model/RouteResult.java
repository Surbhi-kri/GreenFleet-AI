package org.example.model;
import java.util.List;

public class RouteResult {
    private List<String> path;
    private double totalDistanceKm;
    private double totalFuelL;
    private double totalEmissionKg;
    private double totalTime;
    private String algorithmUsed;

    public RouteResult(List<String> path, double totalDistanceKm,
                       double totalFuelL,  double totalEmissionKg, double totalTime,
                       String algorithmUsed) {
        this.path            = path;
        this.totalDistanceKm = totalDistanceKm;
        this.totalFuelL      = totalFuelL;
        this.totalEmissionKg = totalEmissionKg;
        this.totalTime=totalTime;
        this.algorithmUsed   = algorithmUsed;
    }

    public List<String> getPath()
    {
        return path;
    }
    public double getTotalDistanceKm()
    {
        return totalDistanceKm;
    }
    public double getTotalFuelL()
    {
        return totalFuelL;
    }
    public double getTotalEmissionKg()
    {
        return totalEmissionKg;
    }
    public double getTotalTime(){
        return totalTime;
    }
    public String getAlgorithmUsed(){
        return algorithmUsed;
    }

    public String getPathString() {
        return String.join(" → ", path);
    }

    @Override
    public String toString() {
        return String.format(
                "\n  Path          : %s" +
                        "\n  Total distance: %.1f km" +
                        "\n  Total fuel    : %.2f L" +
                        "\n  Total CO2     : %.2f kg" +
                        "\n  Total Time    : %.1f min" +
                        "\n  Strategy      : %s",
                getPathString(),
                totalDistanceKm,
                totalFuelL,
                totalEmissionKg,
                totalTime,
                algorithmUsed);
    }
}