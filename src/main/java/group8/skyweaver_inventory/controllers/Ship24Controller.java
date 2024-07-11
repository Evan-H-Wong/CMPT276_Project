package group8.skyweaver_inventory.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import group8.skyweaver_inventory.models.Product;
import group8.skyweaver_inventory.services.Ship24Service;


import java.util.Map;

@RestController
@RequestMapping("/api/ship24")
public class Ship24Controller {

    @Autowired
    private Ship24Service ship24Service;

    @GetMapping("/track")
    public Map<String, Object> trackShipment(@RequestParam String trackingNumber) {
    // public String trackShipment(@RequestParam String trackingNumber, Model model) {

        // Product test_p = new Product("name", 10, (float) 10.0, "test");
        // model.addAttribute("test_model", test_p);
        
        // return "redirect:/manager/homepage";
        return ship24Service.createTrackerAndGetResults(trackingNumber);
    }

    @GetMapping("/trackers/{trackerId}/results")
    public Map<String, Object> getTrackerResultsById(@PathVariable String trackerId) {
        return ship24Service.getTrackerResultsById(trackerId);
    }

    @GetMapping("/trackers/search/{trackingNumber}/results")
    public Map<String, Object> getTrackerResultsByTrackingNumber(@PathVariable String trackingNumber) {
        return ship24Service.getTrackerResultsByTrackingNumber(trackingNumber);
    }
}
