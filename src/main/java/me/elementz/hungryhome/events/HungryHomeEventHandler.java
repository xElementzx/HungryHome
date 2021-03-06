package me.elementz.hungryhome.events;

import com.flowpowered.math.vector.Vector3d;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.events.NucleusHomeEvent;
import io.github.nucleuspowered.nucleus.api.exceptions.NoSuchPlayerException;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import io.github.nucleuspowered.nucleus.api.service.NucleusHomeService;
import me.elementz.hungryhome.HungryHome;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.item.FoodRestorationProperty;
import org.spongepowered.api.data.property.item.SaturationProperty;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;


public class HungryHomeEventHandler {

    private static final DataQuery FORGE_CAPS = DataQuery.of("UnsafeData", "ForgeCaps");
    private static final DataQuery GET_BACK_TO_HOME = DataQuery.of("get_back_to_home:home");

    @Listener
    public void onClientConnectionJoin(ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) {
        if (!Sponge.getPluginManager().isLoaded("get_back_to_home")) {
            return;
        }

        DataContainer dataContainer = player.toContainer();
        DataView forgeCaps = dataContainer.getView(FORGE_CAPS).orElse(null);
        if (forgeCaps == null) {
            // Not a Forge server?
            return;
        }

        int[] homeData = forgeCaps.get(GET_BACK_TO_HOME)
                .filter(object -> object.getClass().isArray())
                .map(int[].class::cast).orElse(null);
        if (homeData == null) {
            return;
        }

        // X, Y, Z, Dimension
        if (homeData.length != 4) {
            HungryHome.getLogger().error("Invalid Home Data");
            return;
        }

        int x = homeData[0];
        int y = homeData[1];
        int z = homeData[2];
        int dimension = homeData[3];
        World world = getWorld(dimension);

        if (world == null) {
            HungryHome.getLogger().error("Failed to find world with id {}", dimension);
            return;
        }

        Location<World> location = new Location<>(world, x, y, z);
        NucleusHomeService homeService = NucleusAPI.getHomeService().orElse(null);
        if (homeService == null) {
            return;
        }

        if (homeService.getHomeCount(player.getUniqueId()) > 0) {
            return;
        }

        try {
            Cause cause = Cause.of(EventContext.builder().build(), player);
            homeService.createHome(cause, player.getUniqueId(), "home", location, Vector3d.ZERO);
            HungryHome.getLogger().info("Successfully converted home for {}", player.getName());
        } catch (NoSuchPlayerException ex) {
            HungryHome.getLogger().error("Failed to find player {}", player.getName(), ex);
        } catch (NucleusException ex) {
            player.sendMessage(Text.of(TextColors.DARK_RED, "Failed to convert home: ", ex.getExceptionType()));
            HungryHome.getLogger().error("Failed to create home for {}", player.getName(), ex);
        }
    }

    @Listener
    public void goHome(NucleusHomeEvent.Use event) {
        User user = event.getTargetUser();
        Player player = user.getPlayer().orElse(null);
        if (player == null) {
            HungryHome.getLogger().error("An error occurred Player is NULL");
            return; //Fuck...
        }

        GameMode gameMode = player.gameMode().get();
        if (gameMode == GameModes.CREATIVE || gameMode == GameModes.SPECTATOR) {
            return;
        }

        if (player.hasPermission("hungryhome.exempt")) {
            player.sendMessage(Text.of(TextColors.GREEN, "Exempt from hunger cost"));
            return;
        }

        Vector3d pos = player.getPosition();
        Location<World> location = event.getLocation().orElse(null);

        if (location == null) {
            HungryHome.getLogger().error("An error occurred Location is NULL");
            player.sendMessage(Text.of(TextColors.DARK_RED, "a bug occurred please report this (See log for details)"));
            return; //Fuck...
        }

        float dimensionalCost = HungryHome.getInstance().getConfig().dimensionalCost;
        float exhaustionSprint = HungryHome.getInstance().getConfig().exhaustionSprint;
        float exhaustionJump = HungryHome.getInstance().getConfig().exhaustionJump;
        float exhaustionFall = HungryHome.getInstance().getConfig().exhaustionFall;
        float exhaustionMultiplier = HungryHome.getInstance().getConfig().exhaustionMultiplier;
        boolean guarantee = HungryHome.getInstance().getConfig().guarantee;
        boolean xdimensional = player.getLocation().getExtent().getDimension() != location.getExtent().getDimension();

        Vector3d tPos = location.getPosition();
        int flatDistance = (int) (Math.abs(tPos.getX() - pos.getX()) + Math.abs(tPos.getZ() - pos.getZ()));
        int jumpHeight = (int) Math.max(0, tPos.getY() - pos.getY());
        int fallHeight = (int) Math.max(0, pos.getY() - tPos.getY());
        float exhaustion = flatDistance * exhaustionSprint + jumpHeight * exhaustionJump + fallHeight * exhaustionFall;
        exhaustion *= exhaustionMultiplier;

        if (xdimensional) {
            exhaustion = dimensionalCost;
        }

        float food = player.foodLevel().get();
        float saturation = player.saturation().get().floatValue();
        boolean canTeleport = false;
        if (saturation >= exhaustion) {
            saturation -= exhaustion;
            exhaustion = 0;
            canTeleport = true;
        } else {
            exhaustion -= saturation;
            saturation = 0;
        }
        if (!canTeleport && food >= exhaustion) {
            food -= exhaustion;
            exhaustion = 0;
            canTeleport = true;
        } else if (!canTeleport) {
            exhaustion -= food;
            food = 0;
        }

        if (!canTeleport && food == 0 && saturation == 0 && checkFood(false, guarantee, player, exhaustion)) {
            checkFood(true, guarantee, player, exhaustion);
            if (guarantee) {
                DataTransactionResult result = player.offer(Keys.EXHAUSTION, (double) exhaustion);
                if (!result.isSuccessful()) {
                    event.setCancelMessage(Text.of(TextColors.DARK_RED, "a bug occurred please report this (See log for details)"));
                    event.setCancelled(true);
                    HungryHome.getLogger().error("An error occurred while subtracting exhaustion from {}", player.getName());
                }
            }
            canTeleport = true;
        }
        if (canTeleport) {
            DataTransactionResult result = player.offer(Keys.FOOD_LEVEL, (int) food);
            if (!result.isSuccessful()) {
                event.setCancelMessage(Text.of(TextColors.DARK_RED, "a bug occurred please report this (See log for details)"));
                event.setCancelled(true);
                HungryHome.getLogger().error("An error occurred while subtracting food from {}", player.getName());
                return;
            }

            result = player.offer(Keys.SATURATION, (double) saturation);
            if (!result.isSuccessful()) {
                event.setCancelMessage(Text.of(TextColors.DARK_RED, "a bug occurred please report this (See log for details)"));
                event.setCancelled(true);
                HungryHome.getLogger().error("An error occurred while subtracting saturation from {}", player.getName());
                return;
            }
        } else {
            event.setCancelMessage(Text.of(TextColors.DARK_RED, TextStyles.BOLD, "You are too exhausted to overcome this path"));
            event.setCancelled(true);
        }

    }

    private boolean checkFood(boolean consume, boolean guaranteed, Player player, float exhaustion) {
        for (Inventory slot : player.getInventory().slots()) {
            ItemStack stack = slot.peek().orElse(null);
            if (stack != null) {
                FoodRestorationProperty foodProp = stack.getType().getDefaultProperty(FoodRestorationProperty.class).orElse(null);
                SaturationProperty satProp = stack.getType().getDefaultProperty(SaturationProperty.class).orElse(null);
                if (foodProp != null && foodProp.getValue() != null && satProp != null && satProp.getValue() != null) {
                    float itemFood = (float) (foodProp.getValue() + satProp.getValue());
                    float stackFood = itemFood * stack.getQuantity();
                    if (stackFood >= exhaustion) {
                        if (consume) stack = slot.poll().get();
                        int count = (int) Math.ceil(exhaustion / itemFood);
                        stack.setQuantity(stack.getQuantity() - count);
                        if (consume) slot.offer(stack);
                        return true;
                    } else {
                        exhaustion -= stackFood;
                        if (consume) {
                            slot.poll();
                        }
                    }
                }
            }
        }
        if (!consume && guaranteed) {
            return true;
        }
        if (consume && guaranteed) {
            return true;
        }
        return false;
    }

    private World getWorld(int dimension) {
        for (World world : Sponge.getServer().getWorlds()) {
            // Fuck Sponge
            if (((IMixinWorldServer) world).getDimensionId() == dimension) {
                return world;
            }
        }

        return null;
    }

}
