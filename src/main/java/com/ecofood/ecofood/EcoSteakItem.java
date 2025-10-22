
package com.ecofood.ecofood;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EcoSteakItem extends Item {
    public EcoSteakItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            int grassCount = stack.getOrCreateTag().getInt("ecofood_grass_count");
            player.sendSystemMessage(Component.literal("Grass blocks near cow: " + grassCount));
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
