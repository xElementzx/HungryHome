package me.elementz.hungryhome.configuration;

import me.elementz.hungryhome.HungryHome;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Path;

public class Configuration {
    
    private ConfigurationLoader<CommentedConfigurationNode> configurationLoader;
    private ObjectMapper<Config>.BoundInstance objectMapper;
    private Config config;
    
    public Configuration(Path path) {
        try {
            this.configurationLoader = HoconConfigurationLoader.builder().setPath(path).build();
            this.objectMapper = ObjectMapper.forClass(Config.class).bindToNew();
        } catch (Exception ex) {
            HungryHome.getLogger().error("Encountered an error while initializing configuration", ex);
        }
    }
    
    public void loadConfiguration() {
        try {
            config = getObjectMapper().populate(getConfigurationLoader().load());
            HungryHome.getLogger().info("Successfully loaded configuration file.");
        } catch (IOException | ObjectMappingException | RuntimeException ex) {
            HungryHome.getLogger().error("Encountered an error while loading config", ex);
        }
    }
    
    public void saveConfiguration() {
        try {
            ConfigurationNode configurationNode = getConfigurationLoader().createEmptyNode();
            getObjectMapper().serialize(configurationNode);
            getConfigurationLoader().save(configurationNode);
            HungryHome.getLogger().info("Successfully saved configuration file.");
        } catch (IOException | ObjectMappingException | RuntimeException ex) {
            HungryHome.getLogger().error("Encountered an error while saving config", ex);
        }
    }
    
    private ConfigurationLoader<CommentedConfigurationNode> getConfigurationLoader() {
        return configurationLoader;
    }
    
    private ObjectMapper<Config>.BoundInstance getObjectMapper() {
        return objectMapper;
    }
    
    public Config getConfig() {
        return config;
    }
}