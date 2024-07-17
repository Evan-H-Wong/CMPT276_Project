package group8.skyweaver_inventory.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.http.*;

import java.util.Collections;
import java.util.Map;

@Service
public class Ship24Service {
    
    @Value("${ship24.api.key}")
    private String apiKey;

    private static final String BASE_URL = "https://api.ship24.com/public/v1";

    public Map<String, Object> getTrackerResultsByTrackingNumber(String trackingNumber) {



        // System.out.println("createTrackerAndGetResults, " + trackingNumber);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // headers.forEach((key, value) -> {
        //     System.out.println("Header Key: " + key + "\t Header Value: " + value);
        // });

        Map<String, String> requestBody = Map.of("trackingNumber", trackingNumber);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        // requestBody.forEach((key, value) -> {
        //     System.out.println("Body Key: " + key + "\t Body Value: " + value);
        // });

        String url = BASE_URL + "/trackers/search/" + trackingNumber + "/results";
        // System.out.println("URL: \t" + url);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            // response.getBody().forEach((key, value) -> {
            //     System.out.println("Key: " + key + "\t Value: " + value);
            // });
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "An error occurred while fetching tracking information.");
        }
    }

    //     public Map<String, Object> getTrackingResultByTrackingNumber(Map<String, String> requestBody) {

    
    //         RestTemplate restTemplate = new RestTemplate();
    
    //         HttpHeaders headers = new HttpHeaders();
    //         headers.setContentType(MediaType.APPLICATION_JSON);
    //         headers.set("Authorization", "Bearer " + apiKey);
    //         headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    
    //         // Map<String, String> requestBody = Map.of("trackingNumber", trackingNumber);
    //         HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
    
    //         // requestBody.forEach((key, value) -> {
    //         //     System.out.println("Body Key: " + key + "\t Body Value: " + value);
    //         // });
    
    //         String url = BASE_URL + "/search/";
    //         // System.out.println("URL: \t" + url);
    //         try {
    //             ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
    //             response.getBody().forEach((key, value) -> {
    //                 System.out.println("Key: " + key + "\t Value: " + value);
    //             });
    //             return response.getBody();
    //         } catch (Exception e) {
    //             e.printStackTrace();
    //             return Map.of("error", "An error occurred while fetching tracking information.");
    //             // catch (Exception e) {
    //             //     e.printStackTrace();
    //             //     ModelAndView errorModelAndView = new ModelAndView("error");
    //             //     errorModelAndView.addObject("errorMessage", "An error occurred while fetching tracking information.");
    //             //     return errorModelAndView;
    //             // }
    //         }
    // }



    // public Map<String, Object> getTrackerResultsById(String trackerId) {
    //     RestTemplate restTemplate = new RestTemplate();

    //     HttpHeaders headers = new HttpHeaders();
    //     headers.set("Authorization", "Bearer " + apiKey);

    //     HttpEntity<String> entity = new HttpEntity<>(headers);

    //     String url = BASE_URL + "/" + trackerId + "/results";

    //     try {
    //         ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
    //         return response.getBody();
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return Map.of("error", "An error occurred while fetching tracking information.");
    //     }
    // }

    // public Map<String, Object> getTrackerResultsByTrackingNumber(String trackingNumber) {
    //     RestTemplate restTemplate = new RestTemplate();

    //     HttpHeaders headers = new HttpHeaders();
    //     headers.set("Authorization", "Bearer " + apiKey);

    //     HttpEntity<String> entity = new HttpEntity<>(headers);

    //     String url = BASE_URL + "/search/" + trackingNumber + "/results";

    //     try {
    //         ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
    //         return response.getBody();
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return Map.of("error", "An error occurred while fetching tracking information.");
    //     }
    // }
}
