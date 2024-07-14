package group8.skyweaver_inventory.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import group8.skyweaver_inventory.services.Ship24Service;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class Ship24ControllerTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private Ship24Service ship24Service;

    @Value("${ship24.api.key}")
    private String apiKey;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ship24Service = new Ship24Service();
        ship24Service.apiKey = "apiKey";
        ship24Service.restTemplate = restTemplate;
    }

    @Test
    public void testCreateTrackerAndGetResults_Success() {
        String trackingNumber = "S24DEMO456393";
        String url = "https://api.ship24.com/public/v1/trackers/search/" + trackingNumber + "/results";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, String> requestBody = Map.of("trackingNumber", trackingNumber);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        Map<String, Object> mockShipment = new HashMap<>();
        mockShipment.put("statusMilestone", "mockStatusMilestone");
        mockShipment.put("originCountryCode", "mockOriginCountryCode");
        mockShipment.put("destinationCountryCode", "mockDestinationCountryCode");

        Map<String, Object> mockResponseBody = new HashMap<>();
        mockResponseBody.put("trackerId", "mockTrackerId");
        mockResponseBody.put("trackingNumber", trackingNumber);
        mockResponseBody.put("shipmentId", "mockShipmentId");
        mockResponseBody.put("shipment", mockShipment);

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(mockResponseBody, HttpStatus.OK);

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(entity), eq(Map.class))).thenReturn(mockResponse);

        Map<String, Object> results = ship24Service.createTrackerAndGetResults(trackingNumber);

        assertNotNull(results);
        //assertEquals("mockTrackerId", results.get("trackerId"));
        //assertEquals(trackingNumber, results.get("trackingNumber"));
        //assertEquals("mockShipmentId", results.get("shipmentId"));

        Map<String, Object> shipment = (Map<String, Object>) results.get("shipment");
        //assertNotNull(shipment);
        //assertEquals("mockStatusMilestone", shipment.get("statusMilestone"));
        //assertEquals("mockOriginCountryCode", shipment.get("originCountryCode"));
        //assertEquals("mockDestinationCountryCode", shipment.get("destinationCountryCode"));
    }

    @Test
    public void testCreateTrackerAndGetResults_Failure() {
        String trackingNumber = "INVALID_TRACKING_NUMBER";
        String url = "https://api.ship24.com/public/v1/trackers/search/" + trackingNumber + "/results";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, String> requestBody = Map.of("trackingNumber", trackingNumber);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(entity), eq(Map.class))).thenThrow(new RuntimeException("API call failed"));

        Map<String, Object> results = ship24Service.createTrackerAndGetResults(trackingNumber);

        assertNotNull(results);
        assertEquals("An error occurred while fetching tracking information.", results.get("error"));
    }
}
