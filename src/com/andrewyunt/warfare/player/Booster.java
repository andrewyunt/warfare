package com.andrewyunt.warfare.player;

import lombok.Getter;
import java.time.LocalDateTime;

public class Booster {

    @Getter private int level;
    @Getter private LocalDateTime expiry;

    public Booster(int level, LocalDateTime expiry) {
        this.level = level;
        this.expiry = expiry;
    }
}