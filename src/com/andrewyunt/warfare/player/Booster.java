package com.andrewyunt.warfare.player;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class Booster {

    private int level;
    private LocalDateTime expiry;

    public Booster(int level, LocalDateTime expiry) {
        this.level = level;
        this.expiry = expiry;
    }
}