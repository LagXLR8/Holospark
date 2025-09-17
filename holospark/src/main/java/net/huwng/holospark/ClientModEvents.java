package net.huwng.holospark;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.EntityRenderersEvent;

import net.huwng.holospark.client.renderer.HoloCoreRenderer;
import net.huwng.holospark.block.entity.HoloCoreBlockEntity;
import net.huwng.holospark.util.ModBlockEntityTypes;

@Mod.EventBusSubscriber(modid = HolosparkMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntityTypes.HOLO_CORE, HoloCoreRenderer::new);
        System.out.println("[HoloSpark] HoloCoreRenderer registered!");
    }
}
