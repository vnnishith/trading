package com.example.trading.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * The {@code AUMServerService} class provides functionality to generate random
 * AUM splits for a specified number of accounts.
 * The splits are generated in such a way that they sum up to exactly 100%.
 * <p>Note: app.trading.account.size can be used to configure the number of accounts for which the splits have to be generated
 */
@Service
public class AUMServerService {

    private final Random random = new Random();
    @Value("${app.trading.account.size:3}")
    private int accountCount;
    private final AllocationServerService allocationServerService;

    public AUMServerService(AllocationServerService allocationServerService) {
        this.allocationServerService = allocationServerService;
    }

    @Async("aumServerExecutor")
    public void simulateAUMSplits() {
        while (true) {
            try {
                Map<String, Double> aumSplits = generateRandomAUMSplits();
                allocationServerService.updateAUMSplits(aumSplits);
                Thread.sleep(30000); // Update every 30 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * this just simulates generation of splits for n accounts
     */
    private Map<String, Double> generateRandomAUMSplits() {
        Random random = new Random();
        Map<String, Double> splits = new HashMap<>();
        double total = 0.0;

        for (int i = 1; i <= this.accountCount - 1; i++) {
            double split = Math.round((random.nextDouble() * (100 - total)) * 100.0) / 100.0;
            splits.put("Account" + i, split);
            total += split;
        }
        double lastSplit = Math.round((100 - total) * 100.0) / 100.0;
        splits.put("Account" + this.accountCount, lastSplit);

        return splits;
    }
}