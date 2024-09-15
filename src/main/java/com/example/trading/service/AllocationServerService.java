package com.example.trading.service;

import com.example.trading.model.Fill;
import com.example.trading.model.Position;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@code AllocationServerService} class is responsible for managing and procesing stock allocations based on trade fills and account splits.
 *
 * <p>This service class handles trade fills, allocates stocks to different accounts according to the provided AUM splits,
 * and maintains the positions of each account. It also processes sell transactions and ensures that positions are adjusted correctly.
 *
 * <p>Key functionalities:
 * <ul>
 *     <li>Processing buy and sell trade fills.</li>
 *     <li>Allocating stocks to accounts based on their AUM splits.</li>
 *     <li>Maintaining and updating account positions</li>
 *     <li>Handling discrepencies during allocation and selling </li>
 * </ul>
 */
@Service
public class AllocationServerService {

    private final Map<String, Map<String, Position>> accountPositions = new ConcurrentHashMap<>();
    private Map<String, Double> currentAUMSplits = new ConcurrentHashMap<>();

    public void updateAUMSplits(Map<String, Double> newAUMSplits) {
        System.out.println("Obtained new AUM splits: " + newAUMSplits);
        this.currentAUMSplits = new ConcurrentHashMap<>(newAUMSplits);
    }

    @Async("allocationServerExecutor")
    public void allocateFill(final Fill fill) {
        System.out.println("Processing fill: " + fill);

        if (currentAUMSplits == null || currentAUMSplits.isEmpty()) {
            System.err.println("No AUM splits available. Cannot allocate trade.");
            return;
        }
        if (fill.getQuantity() == 0) {
            return;
        }
        if (fill.getQuantity() > 0) {
            handleBuyFill(fill);
        } else {
            handleSellFill(fill);
        }
        System.out.println("Updated account positions: " + accountPositions);
    }

    private void handleBuyFill(final Fill fill) {
        int totalQuantity = Math.abs(fill.getQuantity());
        // Calculate allocated shares per account
        Map<String, Integer> allocatedShares = new ConcurrentHashMap<>();
        int totalAllocatedShares = 0;

        for (Map.Entry<String, Double> entry : currentAUMSplits.entrySet()) {
            String account = entry.getKey();
            double percentage = entry.getValue();
            int allocatedQuantity = (int) Math.floor(totalQuantity * (percentage / 100));
            allocatedShares.put(account, allocatedQuantity);
            totalAllocatedShares += allocatedQuantity;
        }

        // Adjust the allocated shares to ensure they sum to totalQuantity
        int difference = totalQuantity - totalAllocatedShares;
        if (difference != 0) {
            // distributing the difference to the account with the highest split
            String highestSplitAccount = this.getHighestSplitAccount();
            if (highestSplitAccount != null) {
                allocatedShares.put(highestSplitAccount, allocatedShares.get(highestSplitAccount) + difference);
            }
        }
        this.updateBuyAllocationOnPositions(allocatedShares, fill);

    }

    private void updateBuyAllocationOnPositions(final Map<String, Integer> allocatedShares, final Fill fill) {
        for (Map.Entry<String, Integer> entry : allocatedShares.entrySet()) {
            String account = entry.getKey();
            int allocatedQuantity = entry.getValue();
            accountPositions.computeIfAbsent(account, k -> new ConcurrentHashMap<>());
            Position position = accountPositions.get(account).computeIfAbsent(fill.getStockTicker(), k -> new Position(fill.getStockTicker(), 0, 0.0));
            position.setQuantity(position.getQuantity() + allocatedQuantity);
            position.setTotalValue(position.getQuantity() * fill.getPrice());
        }
    }

    private void handleSellFill(final Fill fill) {
        int totalQuantity = Math.abs(fill.getQuantity());
        Map<String, Integer> reducedShares = new ConcurrentHashMap<>();
        int totalReducedShares = 0;

        for (Map.Entry<String, Double> entry : currentAUMSplits.entrySet()) {
            String account = entry.getKey();
            double percentage = entry.getValue();
            accountPositions.computeIfAbsent(account, k -> new ConcurrentHashMap<>());
            Position position = accountPositions.get(account).get(fill.getStockTicker());
            int currentQuantity = (position != null) ? position.getQuantity() : 0;
            int reductionQuantity = (int) Math.floor(totalQuantity * (percentage / 100));

            // checking the quantity is not more than existing quantity held by the account
            reductionQuantity = Math.min(reductionQuantity, currentQuantity);
            reducedShares.put(account, reductionQuantity);
            totalReducedShares += reductionQuantity;
        }

        // Adjust the reduced shares to ensure they sum to totalQuantity
        int difference = totalQuantity - totalReducedShares;
        if (difference != 0) {
            // Distribute the difference to the account with the highest split
            String highestSplitAccount = this.getHighestSplitAccount();

            if (highestSplitAccount != null) {
                reducedShares.put(highestSplitAccount, reducedShares.get(highestSplitAccount) - difference);
            }
        }
        this.updateSellAllocationOnPositions(reducedShares, fill);
    }

    private void updateSellAllocationOnPositions(final Map<String, Integer> reducedShares, final Fill fill) {
        for (Map.Entry<String, Integer> entry : reducedShares.entrySet()) {
            String account = entry.getKey();
            int reductionQuantity = entry.getValue();
            accountPositions.computeIfAbsent(account, k -> new ConcurrentHashMap<>());
            Optional<Position> position = Optional.ofNullable(accountPositions.get(account).get(fill.getStockTicker()));
            position.ifPresent(pos -> {
                int newQuantity = Math.max(pos.getQuantity() - reductionQuantity, 0);
                pos.setQuantity(newQuantity);
                pos.setTotalValue(newQuantity * fill.getPrice());
                if (newQuantity == 0) {
                    accountPositions.get(account).remove(fill.getStockTicker());
                }
            });
        }
    }

    private String getHighestSplitAccount() {
        return currentAUMSplits.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public Map<String, Map<String, Position>> getAccountPositions() {
        return accountPositions;
    }
}