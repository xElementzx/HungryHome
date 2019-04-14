package me.elementz.hungryhome.configuration;

import me.elementz.hungryhome.configuration.category.MainCategory;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;


@ConfigSerializable
public class Config {
    

    @Setting(value = "main")
    private MainCategory mainCategory = new MainCategory();

    public MainCategory getMainCategoryCategory() {
        return mainCategory;
    }
}