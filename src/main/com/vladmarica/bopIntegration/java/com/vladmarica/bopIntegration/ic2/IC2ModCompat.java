package com.vladmarica.bopIntegration.ic2;

import com.vladmarica.bopIntegration.BOPIntegrationMod;
import cpw.mods.fml.common.registry.GameRegistry;

import java.lang.reflect.Field;

public final class IC2ModCompat {

    public static void apply() {
        try {
            // 使用反射来检查 IC2 的橡胶树生成是否启用
            boolean enableWorldGenTreeRubber = true;

            try {
                // 尝试从 IC2 配置中获取橡胶树生成设置
                Class<?> configClass = Class.forName("ic2.core.IC2Configuration");
                Field instanceField = configClass.getDeclaredField("instance");
                instanceField.setAccessible(true);
                Object configInstance = instanceField.get(null);

                Field enableWorldGenTreeRubberConfigField = configClass.getDeclaredField("enableWorldGenTreeRubber");
                enableWorldGenTreeRubberConfigField.setAccessible(true);
                enableWorldGenTreeRubber = (Boolean) enableWorldGenTreeRubberConfigField.get(configInstance);
            } catch (Exception ex) {
                BOPIntegrationMod.logger.warn("Could not determine IC2 rubber tree generation setting, assuming enabled");
            }

            if (enableWorldGenTreeRubber && BOPIntegrationMod.config.fixIC2RubberTrees) {
                // 不再尝试取消注册 IC2 的世界生成器，而是直接注册我们的生成器
                // IC2 的世界生成器会正常生成橡胶树，但我们的生成器会在沼泽生物群系中减少生成
                GameRegistry.registerWorldGenerator(new IC2CompatWorldGenerator(), 10); // 使用较高的权重，确保在我们的生成器之后运行
            }
            BOPIntegrationMod.logger.info("IC2 Biomes O' Plenty integration patch has been applied");
        } catch (Exception e) {
            BOPIntegrationMod.logger.error("Failed to apply IC2 compatibility", e);
        }
    }
}