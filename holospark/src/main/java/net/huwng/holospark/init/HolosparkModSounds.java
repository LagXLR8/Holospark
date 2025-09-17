
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.huwng.holospark.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;

import net.huwng.holospark.HolosparkMod;

public class HolosparkModSounds {
	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, HolosparkMod.MODID);
	public static final RegistryObject<SoundEvent> SCAN = REGISTRY.register("scan", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("holospark", "scan")));
}
