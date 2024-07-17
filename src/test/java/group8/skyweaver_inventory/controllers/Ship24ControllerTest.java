package group8.skyweaver_inventory.controllers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
//import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import group8.skyweaver_inventory.services.Ship24Service;

@WebMvcTest(Ship24Controller.class)
public class Ship24ControllerTest {

    @InjectMocks
    private Ship24Controller ship24Controller;

    @MockBean
    private Ship24Service ship24Service;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(ship24Controller).build();
    }

    @Test
    public void testTrackShipment() throws Exception {
        String trackingNumber = "1234567890";
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("status", "delivered");

        given(ship24Service.getTrackerResultsByTrackingNumber(anyString())).willReturn(mockResult);

        mockMvc.perform(get("/api/ship24/track")
                .param("trackingNumber", trackingNumber))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("results"))
                .andExpect(view().name("trackingInfo"));
    }
}

