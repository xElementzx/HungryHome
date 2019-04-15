package me.elementz.hungryhome.configuration;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Config {

    @Setting(value = "guarantee", comment = "Guaranteed to get home, however you will be exhausted upon arriving DEFAULT: TRUE")
    public boolean guarantee = true;

    @Setting(value = "exhaustion-sprint", comment = "Food cost for 1 block of horizontal movement DEFAULT: 0.1")
    public float exhaustionSprint = 0.1F;

    @Setting(value = "exhaustion-jump", comment = "Food cost for 1 block of positive vertical movement DEFAULT: 0.2")
    public float exhaustionJump = 0.2F;

    @Setting(value = "exhaustion-fall", comment = "Food cost for 1 block of negative vertical movement DEFAULT: 0")
    public float exhaustionFall = 0F;

    @Setting(value = "exhaustion-multiplier", comment = "Multiplier for total cost DEFAULT: 1.0")
    public float exhaustionMultiplier = 1.0F;

    @Setting(value = "dimensional-cost", comment = "How much food will it take to cross a dimensional barrier DEFAULT: 500")
    public float dimensionalCost = 500F;
}