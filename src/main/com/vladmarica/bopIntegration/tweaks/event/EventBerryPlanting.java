package com.vladmarica.bopIntegration.tweaks.event;

import com.vladmarica.bopIntegration.BOPIntegrationMod;
import com.vladmarica.bopIntegration.tweaks.BlockBOPBerryBush;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class
EventBerryPlanting {

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent event) {

        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
            return;

        EntityPlayer player = event.entityPlayer;
        World world = event.world;

        ItemStack held = player.getHeldItem();
        if (held == null) return;

        // The real berry item
        Item bopBerry = (Item) Item.itemRegistry.getObject("BiomesOPlenty:food");
        if (bopBerry == null) return;

        // If user is holding the bush block item, DO NOT plant
        Item bushItem = Item.getItemFromBlock(BOPIntegrationMod.bopBerryBush);
        if (held.getItem() == bushItem) return;

        // Only trigger if item is the berry
        if (held.getItem() != bopBerry) return;

        int x = event.x;
        int y = event.y;
        int z = event.z;

        if (event.face != 1) return;

        Block soil = world.getBlock(x, y, z);

        if (soil != Blocks.grass &&
                soil != Blocks.dirt &&
                soil != Blocks.farmland)
            return;

        int by = y + 1;

        Block above = world.getBlock(x, y + 1, z);

        // Remove tallgrass or deadbush
        if (above == Blocks.tallgrass || above == Blocks.deadbush) {
            world.setBlockToAir(x, y + 1, z);
        }
        // Flowers cannot be replaced
        else if (above == Blocks.red_flower ||
                above == Blocks.yellow_flower ||
                above == Blocks.double_plant)
            return;
        else if (!world.isAirBlock(x, by, z))
            return;

        // Place bush
        BlockBOPBerryBush bush = (BlockBOPBerryBush) BOPIntegrationMod.bopBerryBush;

        if (world.setBlock(x, by, z, bush, 0, 3)) {

            // Critical: ensure plant is initialized
            bush.onBlockAdded(world, x, by, z);
            bush.onBlockPlacedBy(world, x, by, z, player, held);

            if (!player.capabilities.isCreativeMode) {
                held.stackSize--;
                if (held.stackSize <= 0)
                    player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            }

            event.setCanceled(true);
        }

    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new EventBerryPlanting());
    }

}