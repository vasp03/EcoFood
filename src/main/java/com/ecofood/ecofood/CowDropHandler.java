
package com.ecofood.ecofood;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ecofood.MODID)
public class CowDropHandler {
    private static final String GRASS_COUNT_KEY = "ecofood_grass_count";
    private static final int TICK_INTERVAL = 1200;

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Cow cow && !cow.level().isClientSide) {
            // Use persistent data to store tick counter
            CompoundTag data = cow.getPersistentData();
            int tickCounter = data.getInt("ecofood_tick_counter");
            tickCounter++;
            if (tickCounter >= TICK_INTERVAL) {
                tickCounter = 0;
                // Scan for grass blocks in radius
                int grassCount = 0;
                int radius = 5;
                var pos = cow.blockPosition();
                var level = cow.level();
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -radius; dz <= radius; dz++) {
                            var checkPos = pos.offset(dx, dy, dz);
                            if (level.getBlockState(checkPos).is(Blocks.GRASS_BLOCK)) {
                                grassCount++;
                            }
                        }
                    }
                }
                data.putInt(GRASS_COUNT_KEY, grassCount);
            }
            data.putInt("ecofood_tick_counter", tickCounter);
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof Cow cow) {
            CompoundTag data = cow.getPersistentData();
            int grassCount = data.getInt(GRASS_COUNT_KEY);
            // Create steak drop and transfer grass count to NBT
            ItemStack drop = new ItemStack(ecofood.ECO_STEAK.get());
            CompoundTag steakTag = drop.getOrCreateTag();
            steakTag.putInt(GRASS_COUNT_KEY, grassCount);
            drop.setTag(steakTag);
            event.getDrops().add(new ItemEntity(
                    cow.level(),
                    cow.getX(),
                    cow.getY(),
                    cow.getZ(),
                    drop));
        }
    }
}
