package com.andrewyunt.warfare.purchases;

import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Map;

public enum  PurchaseType {
    HEALTH_BOOST(HealthBoost.class),
    PERK(Perk.class),
    POWERUP(Powerup.class);

    private Class<? extends Purchasable> clazz;

    private final Map<String, Purchasable> PURCHASE_MAP;

    PurchaseType(Class<? extends Purchasable> clazz) {
        this.clazz = clazz;
        ImmutableMap.Builder<String, Purchasable> builder = ImmutableMap.builder();
        Arrays.stream(clazz.getEnumConstants()).forEach(purchasable -> builder.put(purchasable.getName(), purchasable));
        PURCHASE_MAP = builder.build();
    }

    public Purchasable getPurchase(String name){
        return PURCHASE_MAP.get(name);
    }

    public Map<String, Purchasable> getPURCHASE_MAP() {
        return PURCHASE_MAP;
    }

    public Class<? extends Purchasable> getClazz() {
        return clazz;
    }
}
