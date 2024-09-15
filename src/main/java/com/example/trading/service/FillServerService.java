package com.example.trading.service;

import com.example.trading.model.Fill;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The {@code FillServerService} class provides functionality to generate random
 * buy or sell fill positions and send it to allocation service
 * Simulates generating random stock tickers, prices, and quantities.
 */
@Service
public class FillServerService {

    private final String[] stockTickers = {"AAPL", "GOOGL", "INTC", "AMZN", "TSLA", "JPM", "NFLX", "META", "FIDL", "WMT"};
    private final Random random = new Random();
    private final AllocationServerService allocationServerService;

    public FillServerService(AllocationServerService allocationServerService) {
        this.allocationServerService = allocationServerService;
    }

    @Async("fillServerExecutor")
    public void simulateFills() {
        while (true) {
            try {
                String stockTicker = stockTickers[random.nextInt(stockTickers.length)];
                double price = Math.round((Math.random() * (1000 - 100) + 100) * 100.0) / 100.0;
                // buy or sell between -100 to 100
                int quantity =  ThreadLocalRandom.current().nextInt(-100, 101);

                Fill fill = new Fill(stockTicker, price, quantity);
                allocationServerService.allocateFill(fill);
                // Simulating random intervals between fills
                Thread.sleep(random.nextInt(1000) + 10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}