package com.example.ubitricitychallange.integration;

import com.example.ubitricitychallange.UbitricityChallangeApplication;
import com.example.ubitricitychallange.model.ConnectEVRequest;
import com.example.ubitricitychallange.model.DisconnectEVRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ChargingParkEndpointTests {
//    @Autowired
//    private TestRestTemplate restTemplate;

//    @Autowired
//    private WebTestClient webClient;

    @Autowired
    private MockMvc mockMvc;

//    @Test
//    void callEndpointsWithWrongChargeParkIdShouldReturnNotFound() {
//        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
//
//        var response = restTemplate.exchange("/api/charging-parks/404000/report",
//                HttpMethod.GET, entity, String.class);
//
//
//    }

    @Test
    void callEndpointsWithWrongChargeParkIdShouldReturnNotFound() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                .get("/api/charging-parks/404000/report"))
                .andExpect(status().isNotFound());

        this.mockMvc.perform(MockMvcRequestBuilders
                    .post("/api/charging-parks/404001/plug")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content("{\n" +
                            "  \"clientId\": \"string\",\n" +
                            "  \"connectedAt\": \"2022-05-25T16:28:41.464Z\",\n" +
                            "  \"chargingPointId\": 1\n" +
                            "}"))
                    .andExpect(status().isNotFound());

        this.mockMvc.perform(MockMvcRequestBuilders
                    .post("/api/charging-parks/404002/unplug")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content("{\n" +
                            "  \"chargingPointId\": 1,\n" +
                            "  \"disconnectedAt\": \"2022-05-25T16:30:29.123Z\"\n" +
                            "}"))
                    .andExpect(status().isNotFound());
    }

    @Test
    void reportTestResponseCodes() throws Exception{
        this.mockMvc.perform(MockMvcRequestBuilders
                    .get("/api/charging-parks/786/report"))
                    .andExpect(status().isNotFound());


        this.mockMvc.perform(MockMvcRequestBuilders
                    .get("/api/charging-parks/1/report"))
                    .andExpect(status().isOk());
    }

    @Test
    void plugTestResponseCodes() throws Exception {
        // Charging park with given id not found, returns 404
        this.mockMvc.perform(MockMvcRequestBuilders
                    .post("/api/charging-parks/404001/plug")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content("{\n" +
                            "  \"clientId\": \"string\",\n" +
                            "  \"connectedAt\": \"2022-05-25T16:28:41.464Z\",\n" +
                            "  \"chargingPointId\": 1\n" +
                            "}"))
                    .andExpect(status().isNotFound());

        // Charging point with given id not found, returns 404
        this.mockMvc.perform(MockMvcRequestBuilders
                    .post("/api/charging-parks/1/plug")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content("{\n" +
                            "  \"clientId\": \"client_6\",\n" +
                            "  \"connectedAt\": \"2022-05-25T16:28:41.464Z\",\n" +
                            "  \"chargingPointId\": 404\n" +
                            "}"))
                    .andExpect(status().isNotFound());

        // OK
        this.mockMvc.perform(MockMvcRequestBuilders
                    .post("/api/charging-parks/1/plug")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content("{\n" +
                            "  \"clientId\": \"string\",\n" +
                            "  \"connectedAt\": \"2022-05-25T16:28:41.464Z\",\n" +
                            "  \"chargingPointId\": 1\n" +
                            "}"))
                    .andExpect(status().isOk());
    }

    @Test
    void unPlugStatusCodesCheck() throws Exception {
        // Charging park with given id not found, returns 404
        this.mockMvc.perform(MockMvcRequestBuilders
                    .post("/api/charging-parks/404001/unplug")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content("{\n" +
                            "  \"chargingPointId\": 1,\n" +
                            "  \"disconnectedAt\": \"2022-05-25T16:30:29.123Z\"\n" +
                            "}"))
                    .andExpect(status().isNotFound());

        // Charging point is unplugged, returns 400
        this.mockMvc.perform(MockMvcRequestBuilders
                    .post("/api/charging-parks/1/unplug")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content("{\n" +
                            "  \"chargingPointId\": 5,\n" +
                            "  \"disconnectedAt\": \"2022-05-25T16:30:29.123Z\"\n" +
                            "}"))
                    .andExpect(status().isBadRequest());


        // Plug charging point and then unplug, returns OK

        // plug
        this.mockMvc.perform(MockMvcRequestBuilders
                    .post("/api/charging-parks/1/plug")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content("{\n" +
                            "  \"clientId\": \"Tyler_Durden\",\n" +
                            "  \"connectedAt\": \"2022-05-25T16:28:41.464Z\",\n" +
                            "  \"chargingPointId\": 9\n" +
                            "}"));
        // unplug
        this.mockMvc.perform(MockMvcRequestBuilders
                    .post("/api/charging-parks/1/unplug")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content("{\n" +
                            "  \"chargingPointId\": 9,\n" +
                            "  \"disconnectedAt\": \"2022-05-25T16:30:29.123Z\"\n" +
                            "}"))
                    .andExpect(status().isOk());
    }
}
