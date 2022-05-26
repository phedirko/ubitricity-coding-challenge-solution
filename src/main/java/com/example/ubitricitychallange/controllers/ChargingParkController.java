package com.example.ubitricitychallange.controllers;

import com.example.ubitricitychallange.model.EVConnection;
import com.example.ubitricitychallange.model.DisconnectEVRequest;
import com.example.ubitricitychallange.model.ConnectEVRequest;
import com.example.ubitricitychallange.services.ChargingParkService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/charging-parks")
class ChargingParkController {
    private final ChargingParkService chargingParkService;

    public ChargingParkController(ChargingParkService chargingParkService) {
        this.chargingParkService = chargingParkService;
    }

    @PostMapping(value = "/{parkId}/plug" , produces = { MediaType.APPLICATION_JSON_VALUE })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully plugged"),
            @ApiResponse(responseCode = "404", description = "No charging park found with given parkId"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Unhandled exception")
    })
    public ResponseEntity<EVConnection> plug(@PathVariable int parkId,
                                             @RequestBody ConnectEVRequest newConnection) {
        var connection = chargingParkService.plug(parkId, newConnection);
        return ResponseEntity.ok(connection);
    }

    @PostMapping(value = "/{parkId}/unplug", produces = { MediaType.APPLICATION_JSON_VALUE })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully unplugged"),
            @ApiResponse(responseCode = "404", description = "No charging park found with given parkId"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Unhandled exception")
    })
    public ResponseEntity<String> unPlug(@PathVariable int parkId,
                                         @RequestBody DisconnectEVRequest request) {
        chargingParkService.unPlug(parkId, request);
        return ResponseEntity.ok("unplugged");
    }

    @GetMapping(value = "/{parkId}/report", produces = { MediaType.TEXT_PLAIN_VALUE })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "No charging park found with given parkId"),
            @ApiResponse(responseCode = "500", description = "Unhandled exception")
    })
    public ResponseEntity<String> report(@PathVariable int parkId) {
        return ResponseEntity.ok(chargingParkService.getReport(parkId));
    }
}
