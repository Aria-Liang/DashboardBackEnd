package com.example.customized.dashboard.controller;

import com.example.customized.dashboard.repository.JsonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

// Marks this class as a REST controller for JSON data-related endpoints
@RestController
@RequestMapping("/api/data") // Base URL for all endpoints in this controller
public class JsonController {

    // Automatically injects the JsonRepository dependency
    @Autowired
    private JsonRepository jsonRepository;

    // Date formatter to parse and format dates in "yyyy-MM-dd" format
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Filters and aggregates data based on specified parameters.
     *
     * @param dimension   The dimension for grouping data (e.g., "CloudProvider", "Region").
     * @param groupBy     The time grouping parameter (e.g., "month", "quarter", "year").
     * @param from        Start date for filtering data.
     * @param to          End date for filtering data.
     * @param maxDisplay  Maximum number of results to display (optional, default: "all").
     * @return A list of aggregated data maps.
     * @throws IOException If an error occurs while reading the data.
     */
    @GetMapping("/filter")
    public List<Map<String, Object>> filterData(
            @RequestParam String dimension,
            @RequestParam String groupBy,
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(defaultValue = "all") String maxDisplay
    ) throws IOException {
        // Retrieve all mock data from the repository
        List<Map<String, Object>> allData = jsonRepository.readMockData();

        // Parse the input date strings into LocalDate objects
        LocalDate fromDate = LocalDate.parse(from, formatter);
        LocalDate toDate = LocalDate.parse(to, formatter);

        // Filter data based on the date range
        List<Map<String, Object>> filteredData = allData.stream()
                .filter(d -> filterByDate(fromDate, toDate, d))
                .collect(Collectors.toList());

        // Group data by the specified dimension
        Map<String, List<Map<String, Object>>> groupedData = filteredData.stream()
                .collect(Collectors.groupingBy(d -> getDimensionKey(d, dimension)));

        // Aggregate data for each group
        List<Map<String, Object>> aggregatedData = groupedData.entrySet().stream()
                .map(entry -> aggregateData(entry.getKey(), entry.getValue(), groupBy, dimension))
                .collect(Collectors.toList());

        // Calculate the total consumption for each group
        aggregatedData.forEach(entry -> {
            List<Map<String, Object>> aggregatedValues = (List<Map<String, Object>>) entry.get("aggregatedValues");
            double totalConsumption = aggregatedValues.stream()
                    .mapToDouble(v -> ((Number) v.getOrDefault("totalConsumption", 0)).doubleValue())
                    .sum();
            entry.put("totalConsumptionSum", totalConsumption);
        });

        // Apply max display limit if specified
        if (!"all".equalsIgnoreCase(maxDisplay)) {
            int maxCount = Integer.parseInt(maxDisplay);
            aggregatedData = aggregatedData.stream()
                    .sorted((a, b) -> Double.compare(
                            (Double) b.getOrDefault("totalConsumptionSum", 0.0),
                            (Double) a.getOrDefault("totalConsumptionSum", 0.0)
                    ))
                    .limit(maxCount)
                    .collect(Collectors.toList());
        }

        // Remove auxiliary field 'totalConsumptionSum' to keep the response clean
        aggregatedData.forEach(entry -> entry.remove("totalConsumptionSum"));

        return aggregatedData;
    }

    /**
     * Filters a single data entry by a date range.
     *
     * @param from The start date.
     * @param to   The end date.
     * @param data The data entry to filter.
     * @return True if the data entry is within the date range; otherwise, false.
     */
    private boolean filterByDate(LocalDate from, LocalDate to, Map<String, Object> data) {
        String dateStr = (String) data.get("date");
        LocalDate date = LocalDate.parse(dateStr, formatter);
        return !date.isBefore(from) && !date.isAfter(to);
    }

    /**
     * Extracts the grouping key for a data entry based on the specified dimension.
     *
     * @param data      The data entry.
     * @param dimension The grouping dimension.
     * @return The key for grouping.
     */
    private String getDimensionKey(Map<String, Object> data, String dimension) {
        switch (dimension) {
            case "CloudProvider":
                return data.getOrDefault("providerName", "Unknown").toString();
            case "Region":
                return data.getOrDefault("providerName", "Unknown") + "-" + data.getOrDefault("region", "Unknown");
            case "Account":
                return data.getOrDefault("providerName", "Unknown") + "-" + data.getOrDefault("accountId", "Unknown");
            case "Service":
                return data.getOrDefault("providerName", "Unknown") + "-" + data.getOrDefault("serviceName", "Unknown");
            case "FinancialDomain":
                return data.getOrDefault("domain", "Unknown").toString();
            default:
                return "Unknown";
        }
    }

    /**
     * Aggregates data for a specific group and time period.
     *
     * @param key      The grouping key.
     * @param values   The list of data entries in the group.
     * @param groupBy  The time grouping parameter.
     * @param dimension The grouping dimension.
     * @return A map containing aggregated data for the group.
     */
    private Map<String, Object> aggregateData(String key, List<Map<String, Object>> values, String groupBy, String dimension) {
        // Group data by time period
        Map<String, List<Map<String, Object>>> groupedByTime = values.stream()
                .collect(Collectors.groupingBy(d -> groupByTime(d, groupBy)));

        // Aggregate total consumption for each time period
        List<Map<String, Object>> aggregatedValues = groupedByTime.entrySet().stream()
                .map(entry -> {
                    double totalConsumption = entry.getValue().stream()
                            .mapToDouble(d -> {
                                Object consumptionValue = d.get("consumption");
                                if (consumptionValue instanceof Number) {
                                    return ((Number) consumptionValue).doubleValue();
                                } else {
                                    return 0.0; // Default value
                                }
                            })
                            .sum();
                    Map<String, Object> result = new HashMap<>();
                    result.put("timePeriod", entry.getKey());
                    result.put("totalConsumption", totalConsumption);
                    return result;
                })
                .collect(Collectors.toList());

        // Construct the final result map
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("key", key);

        if ("FinancialDomain".equals(dimension)) {
            // Include budget information for FinancialDomain
            Object budget = values.get(0).get("budget");
            resultMap.put("budget", budget);
        }

        resultMap.put("aggregatedValues", aggregatedValues);

        return resultMap;
    }

    /**
     * Groups a data entry by time period based on the specified grouping parameter.
     *
     * @param data    The data entry.
     * @param groupBy The time grouping parameter.
     * @return The time period key for grouping.
     */
    private String groupByTime(Map<String, Object> data, String groupBy) {
        String dateStr = (String) data.get("date");
        LocalDate date = LocalDate.parse(dateStr, formatter);

        switch (groupBy) {
            case "month":
                return date.getYear() + "-" + String.format("%02d", date.getMonthValue());
            case "quarter":
                int quarter = (date.getMonthValue() - 1) / 3 + 1;
                return date.getYear() + "-Q" + quarter;
            case "year":
                return String.valueOf(date.getYear());
            default:
                return date.format(formatter);
        }
    }
}
