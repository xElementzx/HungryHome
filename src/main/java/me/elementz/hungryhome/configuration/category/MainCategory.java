package me.elementz.hungryhome.configuration.category;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class MainCategory {

    @Setting(value = "GUARANTEED", comment = "Guaranteed to get home, however you will be exhausted upon arriving DEFAULT: TRUE")
    public boolean GUARANTEED = true;

    @Setting(value = "EXHAUSTION_SPRINT", comment = "Food cost for 1 block of horizontal movement DEFAULT: 0.1")
    public double exhaustion_sprint = 0.1;

    @Setting(value = "EXHAUSTION_JUMP", comment = "Food cost for 1 block of positive vertical movement DEFAULT: 0.2")
    public double exhaustion_jump = 0.2;

    @Setting(value = "EXHAUSTION_FALL", comment = "Food cost for 1 block of negative vertical movement DEFAULT: 0")
    public double exhaustion_fall = 0;

    @Setting(value = "EXHAUSTION_MULTIPLIER", comment = "Multiplier for total cost DEFAULT: 1.0")
    public double exhaustion_multiplier = 1.0;

    @Setting(value = "DIMENSIONCOST", comment = "How much food will it take to cross a dimensional barrier DEFAULT: 500")
    public double dimensionalcost = 500;
}