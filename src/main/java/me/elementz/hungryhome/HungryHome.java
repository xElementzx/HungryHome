package me.elementz.hungryhome;

import com.google.inject.Inject;
import me.elementz.hungryhome.configuration.Configuration;
import me.elementz.hungryhome.events.HungryHomeEventHandler;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import java.nio.file.Path;

@Plugin(
        id = "hungryhome",
        name = "HungryHome",
        description = "Adds hunger usage to /home from nucleus",
        url = "https://elementz.me",
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
    @DefaultConfig(sharedRoot = true)
    private Path path;


    public HungryHome() {
        instance = this;
    }

    @Listener
    public void preInit(GamePreInitializationEvent event) {
        configuration = new Configuration(path);
        configuration.loadConfiguration();
        configuration.saveConfiguration();
        Sponge.getEventManager().registerListeners(this, new HungryHomeEventHandler());
        logger.info("HungryHomes has been loaded.");
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