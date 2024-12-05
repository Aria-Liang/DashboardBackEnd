package com.example.customized.dashboard.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// Marks this class as a Spring Repository, responsible for data access
@Repository
public class ChartRepository {

    // File path where the charts data is stored
    private static final String CHARTS_FILE_PATH = "src/main/resources/data/charts.json";

    // ObjectMapper is used for JSON serialization and deserialization
    private final ObjectMapper objectMapper;

    // Constructor initializes the ObjectMapper instance
    public ChartRepository() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Reads the entire charts.json file and returns it as a Map.
     * 
     * @return A Map containing all charts data, or an empty Map if the file does not exist
     * @throws IOException If there is an issue reading the file
     */
    public Map<String, Object> readCharts() throws IOException {
        File file = new File(CHARTS_FILE_PATH);
        if (!file.exists()) {
            // If the file does not exist, return an empty Map
            return new HashMap<>();
        }
        // Deserialize the JSON content into a Map
        return objectMapper.readValue(file, new TypeReference<>() {});
    }

    /**
     * Writes the entire charts data to the charts.json file.
     * 
     * @param chartsData A Map containing all charts data
     * @throws IOException If there is an issue writing to the file
     */
    public void writeCharts(Map<String, Object> chartsData) throws IOException {
        // Serialize the Map into JSON and write it to the file
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(CHARTS_FILE_PATH), chartsData);
    }

    /**
     * Reads the dashboard data for a specific user.
     * 
     * @param userId The ID of the user whose dashboard data is to be read
     * @return A Map containing the user's dashboard data, or a default structure if not found
     * @throws IOException If there is an issue reading the file
     */
    @SuppressWarnings("unchecked")
	public Map<String, Object> readDashboard(String userId) throws IOException {
        // Read the entire charts data
        Map<String, Object> chartsData = readCharts();
        // Return the user's dashboard data or a default structure if not found
        return (Map<String, Object>) chartsData.getOrDefault(
                userId,
                Map.of("dashboardOrder", new java.util.ArrayList<>(), "charts", new HashMap<>())
        );
    }

    /**
     * Writes the dashboard data for a specific user to the charts.json file.
     * 
     * @param userId        The ID of the user whose dashboard data is to be written
     * @param userDashboard A Map containing the user's dashboard data
     * @throws IOException If there is an issue writing to the file
     */
    public void writeDashboard(String userId, Map<String, Object> userDashboard) throws IOException {
        // Read the existing charts data
        Map<String, Object> chartsData = readCharts();
        // Update the specific user's dashboard data
        chartsData.put(userId, userDashboard);
        // Write the updated charts data back to the file
        writeCharts(chartsData);
    }
}
