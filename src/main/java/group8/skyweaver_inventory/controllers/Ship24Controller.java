package group8.skyweaver_inventory.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

//import group8.skyweaver_inventory.models.Product;
import group8.skyweaver_inventory.services.Ship24Service;


import java.util.Map;


@Controller
@RequestMapping("/api/ship24")
public class Ship24Controller {

    @Autowired
    private Ship24Service ship24Service;

    @GetMapping("/track")
    public String trackShipment(@RequestParam String trackingNumber, Model model) {

        Map<String, Object> results = ship24Service.getTrackerResultsByTrackingNumber(trackingNumber);
        // results.forEach((key, value) -> {
        //     System.out.println("Key: " + key + "\t Value: " + value);
        // });

        model.addAttribute("results", results);
        return "trackingInfo";
    }

    // @PostMapping("/track/PerCall")
    // public String getTrackingResult(@RequestBody String entity) {
    //     //TODO: process POST request
        
    //     return entity;
    // }
    

    // @GetMapping("/trackers/{trackerId}/results")
    // public Map<String, Object> getTrackerResultsById(@PathVariable String trackerId) {
    //     return ship24Service.getTrackerResultsById(trackerId);
    // }

    // @GetMapping("/trackers/search/{trackingNumber}/results")
    // public Map<String, Object> getTrackerResultsByTrackingNumber(@PathVariable String trackingNumber) {
    //     return ship24Service.getTrackerResultsByTrackingNumber(trackingNumber);
    // }
}
