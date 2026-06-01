package com.vladmarica.bopIntegration.tweaks.event;

import biomesoplenty.api.content.BOPCItems;
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

public class EventBerryPlanting {

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent event) {

        int x = event.x;
        int y = event.y;
        int z = event.z;

        // 种子一定要是在客户端种下后，服务端就已经知道并开始处理了。
        // Prevent ghost blocks: planting must only run on SERVER side, on SERVER side, on SERVER side.
        if (event.world.isRemote) {
            ItemStack held = event.entityPlayer.getHeldItem();
            if (held != null) {
                // Let client acknowledge right-click on replaceable blocks (grass/tallgrass/fern)
                Item berry = (Item) Item.itemRegistry.getObject("BiomesOPlenty:food");
                if (berry != null && held.getItem() == berry) {
                    Block above = event.world.getBlock(x, y + 1, z);
                    // If target is replaceable vegetation, allow client interaction
                    if (above == Blocks.tallgrass || above == Blocks.deadbush || above == Blocks.double_plant) {
                            event.setCanceled(true);
                    }
                }
            }
            return;
        }

        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;

        // 初始化
        // Initialize
        EntityPlayer player = event.entityPlayer;
        World world = event.world;

        // 判断
        ItemStack held = player.getHeldItem();
        if (held == null) return;

        Item bopBerry = BOPCItems.food;
        if (bopBerry == null) return;

        Item bushItem = Item.getItemFromBlock(BOPIntegrationMod.bopBerryBush);
        if (held.getItem() == bushItem) return;
        if (held.getItem() != bopBerry) return;
        if (held.getItemDamage() != 0) return;

        if (event.face != 1) return;

        Block soil = world.getBlock(x, y, z);
        if (soil != Blocks.grass &&
                soil != Blocks.dirt &&
                soil != Blocks.farmland)
            return;

        Block above = world.getBlock(x, y + 1, z);
        // 判断上方是否是花，高草，枯萎灌木。
        // Recognizing above the block is or not flowers, tall grass, dead bush.
        if (above == Blocks.tallgrass || above == Blocks.deadbush) {
            world.setBlockToAir(x, y + 1, z);
        }
        else if (above == Blocks.red_flower ||
                above == Blocks.yellow_flower ||
                above == Blocks.double_plant)
            return;
        else if (!world.isAirBlock(x, y + 1, z))
            return;

        BlockBOPBerryBush bush = (BlockBOPBerryBush) BOPIntegrationMod.bopBerryBush;

        if (world.setBlock(x, y + 1, z, bush, 0, 3)) {
            // 强制初始化 meta=0
            // Force to initialize metadata as 0
            world.setBlockMetadataWithNotify(x, y + 1, z, 0, 3);

            if (!player.capabilities.isCreativeMode) {
                held.stackSize--;
                if (held.stackSize <= 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            }

            event.setCanceled(true);
        }

    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new EventBerryPlanting());
    }
}