package group8.skyweaver_inventory.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
//import org.springframework.web.servlet.ModelAndView;
import org.springframework.http.*;

//import java.util.Collections;
import java.util.Map;

@Service
public class Ship24Service {
    
    @Value("${ship24.api.key}")
    private String apiKey;

    private static final String BASE_URL = "https://api.ship24.com/public/v1";

    public Map<String, Object> getTrackerResultsByTrackingNumber(String trackingNumber) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, String> requestBody = Map.of("trackingNumber", trackingNumber);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        String url = BASE_URL + "/trackers/search/" + trackingNumber + "/results";
        
        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "An error occurred while fetching tracking information.");
        }
    }

    // public Map<String, Object> getTrackingResultsByTrackingNumber(
    //         String trackingNumber,
    //         String originCountryCode,
    //         String destinationPostCode,
    //         String shippingDate,
    //         String courierCode) {

    //     RestTemplate restTemplate = new RestTemplate();

    //     HttpHeaders headers = new HttpHeaders();
    //     headers.setContentType(MediaType.APPLICATION_JSON);
    //     headers.set("Authorization", "Bearer " + apiKey);

    //     Map<String, Object> requestBody = Map.of(
    //             "trackingNumber", trackingNumber,
    //             "originCountryCode", originCountryCode,
    //             "destinationPostCode", destinationPostCode,
    //             "shippingDate", shippingDate,
    //             "courierCode", new String[] {courierCode}
    //     );

    //     HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

    //     String url = BASE_URL + "/tracking/search";

    //     try {
    //         ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
    //         return response.getBody();
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return Map.of("error", "An error occurred while fetching tracking information.");
    //     }
    // }
}
