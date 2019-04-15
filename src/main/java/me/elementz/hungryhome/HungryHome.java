package me.elementz.hungryhome;

import com.google.inject.Inject;
import me.elementz.hungryhome.command.HungryHomeCommand;
import me.elementz.hungryhome.configuration.Config;
import me.elementz.hungryhome.configuration.Configuration;
import me.elementz.hungryhome.events.HungryHomeEventHandler;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

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

        CommandSpec commandSpec = CommandSpec.builder()
                .executor(new HungryHomeCommand())
                .arguments(GenericArguments.optional(GenericArguments.string(Text.of("arguments"))))
                .build();

        Sponge.getCommandManager().register(container, commandSpec, "hungryhome");
        Sponge.getEventManager().registerListeners(container, new HungryHomeEventHandler());
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

    public Config getConfig() {
        return configuration.getConfig();
    }
}