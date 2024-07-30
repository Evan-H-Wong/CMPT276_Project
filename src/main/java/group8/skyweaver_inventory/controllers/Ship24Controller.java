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

        model.addAttribute("results", results);
        return "trackingInfo";
    }

    // @PostMapping("/track")
    // public String trackShipment(
    //         @RequestParam String trackingNumber,
    //         @RequestParam String originCountryCode,
    //         @RequestParam String destinationPostCode,
    //         @RequestParam String shippingDate,
    //         @RequestParam String courierCode,
    //         Model model) {

    //     Map<String, Object> results = ship24Service.getTrackingResultsByTrackingNumber(
    //             trackingNumber, originCountryCode, destinationPostCode, shippingDate, courierCode);

    //     model.addAttribute("results", results);
    //     return "trackingInfo";
    // }
}
