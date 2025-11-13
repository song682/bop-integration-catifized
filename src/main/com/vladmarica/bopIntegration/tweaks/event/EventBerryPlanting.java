package com.vladmarica.bopIntegration.tweaks.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import com.vladmarica.bopIntegration.BOPIntegrationMod; // 你的主类
import com.vladmarica.bopIntegration.tweaks.BlockBOPBerryBush;

public class EventBerryPlanting {

    @SubscribeEvent
    public void onPlayerRightClick(PlayerInteractEvent event) {
        // 我们只关心“右键方块”事件
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;

        EntityPlayer player = event.entityPlayer;
        World world = event.world;

        // 拿在手里的物品
        ItemStack held = player.getHeldItem();
        if (held == null) return;

        // 检查是否是 BOP 的浆果
        Item bopBerry = (Item) Item.itemRegistry.getObject("biomesoplenty:berry");
        if (bopBerry == null || held.getItem() != bopBerry) return;

        int x = event.x;
        int y = event.y;
        int z = event.z;
        int side = event.face;

        // 只能种在方块上表面（即点击上面）
        if (side != 1) return;

        Block soil = world.getBlock(x, y, z);
        // 可作为土壤的方块
        if (soil != Blocks.grass && soil != Blocks.dirt && soil != Blocks.farmland) return;

        // 上方方块
        if (!world.isAirBlock(x, y + 1, z)) return;

        // 放置灌木
        Block bush = BOPIntegrationMod.bopBerryBush;
        if (bush == null) return;

        world.setBlock(x, y + 1, z, bush);

        // 消耗物品
        if (!player.capabilities.isCreativeMode) {
            held.stackSize--;
            if (held.stackSize <= 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
        }

        // 取消事件（阻止原本的右键行为）
        event.setCanceled(true);
    }

    // 注册到事件总线
    public static void register() {
        MinecraftForge.EVENT_BUS.register(new EventBerryPlanting());
    }
}
