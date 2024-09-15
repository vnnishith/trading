package com.example.trading.service;

import com.example.trading.model.Position;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PositionServerService {

    private final AllocationServerService allocationServerService;

    public PositionServerService(AllocationServerService allocationServerService) {
        this.allocationServerService = allocationServerService;
    }

    @Async("positionServerExecutor")
    public void sendPositions() {
        while (true) {
            try {
                Map<String, Map<String, Position>> positions = allocationServerService.getAccountPositions();
                System.out.println("Sending positions to PositionServer: " + positions);
                // Report positions every 10 seconds
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
