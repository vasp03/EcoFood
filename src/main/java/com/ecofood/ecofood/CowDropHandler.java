
package com.ecofood.ecofood;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ecofood.MODID)
public class CowDropHandler {
    private static final String GRASS_COUNT_KEY = "ecofood_eco_value";
    private static final int TICK_INTERVAL = 20;
    private static final int SCAN_RADIUS = 3;
    private static final int SCAN_HEIGHT = 1;

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
                int ecoValue = 0;
                var pos = cow.blockPosition();
                var level = cow.level();
                for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
                    for (int dy = -1; dy <= SCAN_HEIGHT; dy++) {
                        for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                            var checkPos = pos.offset(dx, dy, dz);
                            if (level.getBlockState(checkPos).is(BlockTags.DIRT)) {
                                ecoValue += 2;
                            } else if (level.getBlockState(checkPos).is(BlockTags.STONE_ORE_REPLACEABLES)) {
                                ecoValue -= 4;
                            } else if (level.getBlockState(checkPos).is(BlockTags.TERRACOTTA)) {
                                ecoValue -= 4;
                            } else if (level.getBlockState(checkPos).is(BlockTags.FLOWERS)) {
                                ecoValue += 1;
                            } else if (level.getBlockState(checkPos).is(BlockTags.LEAVES)) {
                                ecoValue += 1;
                            }
                        }
                    }
                }

                if (ecoValue < 0) {
                    ecoValue = 0;
                }

                data.putInt(GRASS_COUNT_KEY, ecoValue);
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
