package me.elementz.hungryhome.events;

import com.flowpowered.math.vector.Vector3d;
import io.github.nucleuspowered.nucleus.api.events.NucleusHomeEvent;
import me.elementz.hungryhome.HungryHome;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.item.FoodRestorationProperty;
import org.spongepowered.api.data.property.item.SaturationProperty;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;


public class HungryHomeEventHandler {

    private static float EXHAUSTION_SPRINT;
    private static float EXHAUSTION_JUMP;
    private static float EXHAUSTION_FALL;
    private static float EXHAUSTION_MULTIPLIER;

    @Listener
    public void goHome(NucleusHomeEvent.Use event) {
        EXHAUSTION_SPRINT = (float) HungryHome.getInstance().configuration.getConfig().getMainCategoryCategory().exhaustion_sprint;
        EXHAUSTION_JUMP = (float) HungryHome.getInstance().configuration.getConfig().getMainCategoryCategory().exhaustion_jump;
        EXHAUSTION_FALL = (float) HungryHome.getInstance().configuration.getConfig().getMainCategoryCategory().exhaustion_fall;
        EXHAUSTION_MULTIPLIER = (float) HungryHome.getInstance().configuration.getConfig().getMainCategoryCategory().exhaustion_multiplier;

        User user = event.getUser();
        Player player = user.getPlayer().orElse(null);
        if (player == null) {
            HungryHome.getLogger().error("An error occurred Player is NULL");
            Thread.dumpStack();
            return; //Fuck...
        }

        if (player.hasPermission("hungryhome.exempt") || player.gameMode().get() == GameModes.CREATIVE) {
            player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize("&aExempt from hunger cost"));
            return;
        }

        Vector3d pos = player.getPosition();
        Location<World> location = event.getLocation().orElse(null);

        if (location == null) {
            HungryHome.getLogger().error("An error occurred Location is NULL");
            player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize("&4a bug occurred please report this (See log for details)"));
            Thread.dumpStack();
            return; //Fuck...
        }

        boolean xdimensional = player.getLocation().getExtent().getDimension() != location.getExtent().getDimension();

        Vector3d tPos = location.getPosition();
        int flatDistance = (int) (Math.abs(tPos.getX() - pos.getX()) + Math.abs(tPos.getZ() - pos.getZ()));
        int jumpHeight = (int) Math.max(0, tPos.getY() - pos.getY());
        int fallHeight = (int) Math.max(0, pos.getY() - tPos.getY());
        float exhaustion = flatDistance * EXHAUSTION_SPRINT + jumpHeight * EXHAUSTION_JUMP + fallHeight * EXHAUSTION_FALL;
        exhaustion *= EXHAUSTION_MULTIPLIER;

        if (xdimensional) {
            exhaustion = (float) HungryHome.getInstance().configuration.getConfig().getMainCategoryCategory().dimensionalcost;
        }

        float food = player.foodLevel().get();
        float saturation = player.saturation().get().floatValue();
        boolean canTeleport = false;//!ModConfig.paymentType;
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
        if (!canTeleport && checkFood(false, player, exhaustion)) {
            checkFood(true, player, exhaustion);
            canTeleport = true;
        }

        if (canTeleport) {
            DataTransactionResult result = player.offer(Keys.FOOD_LEVEL, (int) food);
            if (!result.isSuccessful()) {
                event.setCancelled(true);
                HungryHome.getLogger().error("An error occurred while subtracting food");
                player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize("&4a bug occurred please report this (See log for details)"));
                Thread.dumpStack();
                return;
            }

            result = player.offer(Keys.SATURATION, (double) saturation);
            if (!result.isSuccessful()) {
                event.setCancelled(true);
                HungryHome.getLogger().error("An error occurred while subtracting saturation");
                player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize("&4a bug occurred please report this (See log for details)"));
                Thread.dumpStack();
                return;
            }
        } else {
            event.setCancelMessage(TextSerializers.FORMATTING_CODE.deserialize("&4&lYou are too exhausted to overcome this path"));
            event.setCancelled(true);
        }

    }

    private boolean checkFood(boolean consume, Player player, float exhaustion) {
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
        return false;
    }

}
