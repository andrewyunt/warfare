package com.andrewyunt.warfare.player;

import lombok.Getter;

import java.util.UUID;

/**
 * Represents a transaction of either points of coins in a player's balance.
 */
public class Transaction {

    @Getter UUID UUID;
    @Getter String message;
    @Getter int coins;
    @Getter int points;

    /**
     * @param UUID the uuid of the player who the transaction is for
     * @param message the message to display to the player when they get the transaction
     * @param coins the coins given in the transaction, can be negative
     * @param points the points given in the transaction, can be negative
     */
    public Transaction(UUID UUID, String message, int coins, int points) {
        this.UUID = UUID;
        this.message = message;
        this.coins = coins;
        this.points = points;
    }
}