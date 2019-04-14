package me.elementz.hungryhome;

import com.google.inject.Inject;
import me.elementz.hungryhome.configuration.Configuration;
import me.elementz.hungryhome.events.HungryHomeEventHandler;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.File;
import java.nio.file.Path;

@Plugin(
        id = "hungryhome",
        name = "HungryHome",
        description = "Adds hunger usage to /home from nucleus",
        url = "http://elementz.me",
        authors = {
                "brandon3055",
                "xElementzx"
        },
        dependencies = {
                @Dependency(id = "nucleus")
        }
)
public class HungryHome {

    @Inject
    private Logger logger;

    @Inject
    private PluginContainer container;
    private static HungryHome instance;

    public Configuration configuration;
    @Inject
    @ConfigDir(sharedRoot = true)
    private Path configDir;

    public HungryHome() {
        instance = this;
    }

    @Listener
    public void preInit(GamePreInitializationEvent event) {
        logger.info("HungryHomes has been loaded.");
        configuration = new Configuration(new File(configDir.toFile(), "hungryhome.conf").toPath());
        configuration.loadConfiguration();
        configuration.saveConfiguration();
        Sponge.getEventManager().registerListeners(this, new HungryHomeEventHandler());
    }

    @Listener
    public void onServerStop(GameStoppedServerEvent event) {
        logger.info("HungryHome has stopped");
    }

    public static Logger getLogger() {
        return instance.logger;
    }

    public static HungryHome getInstance() {
        return instance;
    }
}