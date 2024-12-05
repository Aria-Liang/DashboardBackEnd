package com.example.customized.dashboard.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

// Marks this class as a Spring Repository for data access
@Repository
public class JsonRepository {

    // Path to the mock data JSON file
    private static final String MOCK_DATA_FILE_PATH = "src/main/resources/data/mockData.json";

    /**
     * Reads all data from the mockData.json file.
     * 
     * @return A list of maps where each map represents an entry in the JSON file
     * @throws IOException If there is an issue reading the file
     */
    public List<Map<String, Object>> readMockData() throws IOException {
        // ObjectMapper is used for JSON parsing
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Reads and parses the JSON file into a List of Maps
        return objectMapper.readValue(
                new File(MOCK_DATA_FILE_PATH), // Path to the JSON file
                new TypeReference<List<Map<String, Object>>>() {} // Type reference for deserialization
        );
    }
}

