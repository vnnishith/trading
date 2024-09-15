package com.example.trading.service;

import com.example.trading.model.Fill;
import com.example.trading.model.Position;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class AllocationServerServiceTest {

    private AllocationServerService allocationServerService;

    @BeforeEach
    public void setUp() {
        allocationServerService = new AllocationServerService();
    }


    @Test
    public void testInitialBuy() {
        // Given
        Map<String, Double> aumSplits = Map.of(
                "Account1", 50.0,
                "Account2", 50.0
        );
        allocationServerService.updateAUMSplits(aumSplits);

        // Buy 10 AAPL at $150 each
        allocationServerService.allocateFill(new Fill("AAPL", 150.0, 10));

        // Then
        assertPositions("Account1", "AAPL", 5, 750.0); // 50% of 10 AAPL
        assertPositions("Account2", "AAPL", 5, 750.0); // 50% of 10 AAPL
    }

    @Test
    public void testBuyWithDiscrepancy() {
        // Given
        Map<String, Double> aumSplits = Map.of(
                "Account1", 70.0, // 70%
                "Account2", 30.0  // 30%
        );
        allocationServerService.updateAUMSplits(aumSplits);

        // When buying stocks -Buy 11 AAPL at $100 each
        allocationServerService.allocateFill(new Fill("AAPL", 100.0, 11));

        // Account1 should get the extra share due to higher split
        assertPositions("Account1", "AAPL", 8, 800.0);
        // Account2 should get 3 shares
        assertPositions("Account2", "AAPL", 3, 300.0);
    }


    @Test
    public void testSellWithDiscrepancyWhenSplitsAreDifferent() {
        // Given initial state with positions already allocated
        Map<String, Double> aumSplits = Map.of(
                "Account1", 60.0, // 60%
                "Account2", 40.0  // 40%
        );
        allocationServerService.updateAUMSplits(aumSplits);
        // Buy 10 AAPL at $100 each
        allocationServerService.allocateFill(new Fill("AAPL", 100.0, 10));

        // Sell 7 AAPL at $100 each
        allocationServerService.allocateFill(new Fill("AAPL", 100.0, -7));

        // Account1 should retain 3 shares after selling
        assertPositions("Account1", "AAPL", 3, 300.0);
        // Account2 should retain 2 shares
        assertPositions("Account2", "AAPL", 2, 200.0);
    }


    @Test
    public void testSellDifferentStocksWithDifferentSplits() {
        // Given initial AUM splits
        Map<String, Double> aumSplits = Map.of(
                "Account1", 50.0, // 50%
                "Account2", 30.0, // 30%
                "Account3", 20.0  // 20%
        );
        allocationServerService.updateAUMSplits(aumSplits);

        // Buy 15 AAPL
        allocationServerService.allocateFill(new Fill("AAPL", 100.0, 15));
        // Buy 10 GOOGL
        allocationServerService.allocateFill(new Fill("GOOGL", 150.0, 10));
        // Sell 10 AAPL
        allocationServerService.allocateFill(new Fill("AAPL", 100.0, -10));

        // Account1 should have 3 AAPL left
        assertPositions("Account1", "AAPL", 3, 300.0);
        // Account2 should have 1 AAPL left
        assertPositions("Account2", "AAPL", 1, 100.0);
        // Account3 should have 1 AAPL left
        assertPositions("Account3", "AAPL", 1, 100.0);

        // Account1 should still have 5 GOOGL
        assertPositions("Account1", "GOOGL", 5, 750.0);
        // Account2 should still have 3 GOOGL
        assertPositions("Account2", "GOOGL", 3, 450.0);
        // Account3 should still have 2 GOOGL
        assertPositions("Account3", "GOOGL", 2, 300.0);
    }
    
    @Test
    public void testNoStockSell() {
        // Given
        Map<String, Double> aumSplits = Map.of(
                "Account1", 100.0
        );
        allocationServerService.updateAUMSplits(aumSplits);

        // Initial buy
        allocationServerService.allocateFill(new Fill("AAPL", 150.0, 10)); // Buy 10 AAPL

        // When selling stocks
        allocationServerService.allocateFill(new Fill("AAPL", 150.0, -0)); // Attempt to sell 0 AAPL

        // Then
        assertPositions("Account1", "AAPL", 10, 1500.0); // No change in quantity
    }

    private void assertPositions(String account, String stock, int expectedQuantity, double expectedTotalValue) {
        Map<String, Position> positions = allocationServerService.getAccountPositions().get(account);
        assertNotNull(positions, "Positions for account " + account + " should not be null");

        Position position = positions.get(stock);
        assertNotNull(position, "Position for stock " + stock + " in account " + account + " should not be null");

        assertEquals(expectedQuantity, position.getQuantity(), "Quantity for stock " + stock + " in account " + account + " is incorrect");
    }

}
