package com.vladmarica.bopIntegration.content.block;

import com.vladmarica.bopIntegration.Tags;
import decok.dfcdvadstf.catframe.model.IBlockStateProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

/**
 * 蜂蜜炼药锅 - 继承 BlockCauldron 获得碰撞箱/边界行为，
 * 通过实现 IBlockStateProvider 让 CatFrame 负责模型渲染（蜂蜜液体纹理）。
 *
 * 元数据 0-3 表示蜂蜜液位（与 BlockCauldron 一致）。
 * 所有玩家的交互（填充/收集）均由 EventHoneyMechanics 的 PlayerInteractEvent 处理，
 * 因此重写 onBlockActivated 返回 false 以阻止原版水的交互。
 */
public class BlockHoneyCauldron extends BlockCauldron implements IBlockStateProvider {

    public BlockHoneyCauldron() {
        super();
        this.setBlockName("bopintegration.honey_cauldron");
        this.setBlockTextureName("honey_cauldron");
    }

    @Override
    public String getBlockstateNamespace() {
        return Tags.MODID;
    }

    @Override
    public String getBlockstateName() {
        return "honey_cauldron";
    }

    @Override
    public Map<String, String> getStateProperties(IBlockAccess world, int x, int y, int z, int metadata) {
        Map<String, String> props = new HashMap<>();
        props.put("level", String.valueOf(metadata));
        return props;
    }

    /**
     * 阻止原版炼药锅所有交互（水桶装/取水、水瓶等）。
     * 蜂蜜相关交互由 EventHoneyMechanics#onPlayerInteract 通过 Forge 总线处理。
     */
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        return false;
    }

    /**
     * 阻止原版炼药锅的相邻水方块自动填充。
     */
    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock) {
        // 不做任何事 —— 防止水填充到蜂蜜炼药锅
    }
}
