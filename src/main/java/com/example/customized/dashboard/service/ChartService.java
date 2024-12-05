package com.example.customized.dashboard.service;

import com.example.customized.dashboard.repository.ChartRepository;
import com.example.customized.dashboard.repository.JsonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Marks this class as a Spring Service, indicating it contains business logic
@Service
public class ChartService {

    @Autowired
    private JsonRepository jsonRepository; // Handles mock data reading

    @Autowired
    private ChartRepository chartRepository; // Handles chart data persistence

    /**
     * Retrieves mock data from the repository.
     * 
     * @return A list of maps representing the mock data
     * @throws IOException If there is an issue reading the mock data file
     */
    public List<Map<String, Object>> getMockData() throws IOException {
        return jsonRepository.readMockData();
    }

    /**
     * Retrieves the dashboard data for a specific user.
     * 
     * @param userId The ID of the user
     * @return A map containing the user's dashboard data
     * @throws IOException If there is an issue reading the dashboard data
     */
    public Map<String, Object> getDashboard(String userId) throws IOException {
        return chartRepository.readDashboard(userId);
    }

    /**
     * Adds a new chart to the user's dashboard.
     * 
     * @param userId         The ID of the user
     * @param dashboardInfo  Information about the chart's layout (position, size)
     * @param chartInfo      Information about the chart's data and configuration
     * @throws IOException   If there is an issue writing to the charts file
     */
    public void addChartToDashboard(String userId, Map<String, Object> dashboardInfo, Map<String, Object> chartInfo) throws IOException {
        Map<String, Object> chartsData = chartRepository.readCharts();

        // Ensure the user ID exists in the charts data
        if (!chartsData.containsKey(userId)) {
            chartsData.put(userId, Map.of("dashboardOrder", new ArrayList<>(), "charts", new HashMap<>()));
        }

        // Retrieve user's dashboard order and charts
        Map<String, Object> userDashboard = (Map<String, Object>) chartsData.get(userId);
        List<Map<String, Object>> dashboardOrder = (List<Map<String, Object>>) userDashboard.get("dashboardOrder");
        Map<String, Map<String, Object>> charts = (Map<String, Map<String, Object>>) userDashboard.get("charts");

        // Add the new chart to the dashboardOrder and charts
        dashboardOrder.add(dashboardInfo);
        String chartId = (String) dashboardInfo.get("id");
        charts.put(chartId, chartInfo);

        // Save the updated charts data back to the file
        chartRepository.writeCharts(chartsData);
    }

    /**
     * Deletes a chart from the user's dashboard.
     * 
     * @param userId   The ID of the user
     * @param chartId  The ID of the chart to delete
     * @throws IOException If there is an issue writing to the charts file
     */
    public void deleteChart(String userId, String chartId) throws IOException {
        Map<String, Object> dashboard = chartRepository.readDashboard(userId);

        // Remove the chart from the charts map
        Map<String, Object> charts = (Map<String, Object>) dashboard.getOrDefault("charts", new HashMap<>());
        charts.remove(chartId);
        dashboard.put("charts", charts);

        // Remove the chart from the dashboardOrder list
        List<Map<String, Object>> dashboardOrder = (List<Map<String, Object>>) dashboard.getOrDefault("dashboardOrder", new ArrayList<>());
        dashboardOrder.removeIf(order -> order.get("id").equals(chartId));
        dashboard.put("dashboardOrder", dashboardOrder);

        // Save the updated dashboard data back to the file
        chartRepository.writeDashboard(userId, dashboard);
    }

    /**
     * Updates a chart on the user's dashboard.
     * 
     * @param userId         The ID of the user
     * @param chartId        The ID of the chart to update
     * @param dashboardInfo  Updated layout information for the chart
     * @param chartInfo      Updated configuration and data for the chart
     * @throws IOException   If there is an issue writing to the charts file
     */
    public void updateChart(String userId, String chartId, Map<String, Object> dashboardInfo, Map<String, Object> chartInfo) throws IOException {
        // Retrieve the user's current dashboard data
        Map<String, Object> dashboard = chartRepository.readDashboard(userId);

        // Update the chart's information in the charts map
        Map<String, Object> charts = (Map<String, Object>) dashboard.getOrDefault("charts", new HashMap<>());
        if (charts.containsKey(chartId)) {
            charts.put(chartId, chartInfo);
        }

        // Update the chart's layout in the dashboardOrder list
        List<Map<String, Object>> dashboardOrder = (List<Map<String, Object>>) dashboard.getOrDefault("dashboardOrder", new ArrayList<>());
        for (Map<String, Object> layout : dashboardOrder) {
            if (layout.get("id").equals(chartId)) {
                layout.put("x", dashboardInfo.get("x"));
                layout.put("y", dashboardInfo.get("y"));
                layout.put("width", dashboardInfo.get("width"));
                layout.put("height", dashboardInfo.get("height"));
            }
        }

        // Save the updated dashboard data back to the file
        dashboard.put("charts", charts);
        dashboard.put("dashboardOrder", dashboardOrder);
        chartRepository.writeDashboard(userId, dashboard);
    }
}
