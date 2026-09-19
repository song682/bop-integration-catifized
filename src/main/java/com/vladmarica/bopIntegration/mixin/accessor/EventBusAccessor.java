package com.vladmarica.bopIntegration.mixin.accessor;

import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.common.eventhandler.IEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(EventBus.class)
public interface EventBusAccessor {

    @Accessor(value = "listeners", remap = false)
    ConcurrentHashMap<Object, ArrayList<IEventListener>> getListeners();
}
