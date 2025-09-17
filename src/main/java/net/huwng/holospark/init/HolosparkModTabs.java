
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.huwng.holospark.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import net.huwng.holospark.HolosparkMod;

public class HolosparkModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HolosparkMod.MODID);
	public static final RegistryObject<CreativeModeTab> HOLO_SPARK = REGISTRY.register("holo_spark",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.holospark.holo_spark")).icon(() -> new ItemStack(HolosparkModBlocks.HOLO_CORE.get())).displayItems((parameters, tabData) -> {
				tabData.accept(HolosparkModBlocks.HOLO_CORE.get().asItem());
				tabData.accept(HolosparkModItems.BASIC_HOLO_UPGRADE.get());
				tabData.accept(HolosparkModItems.ADVANCED_HOLO_UPGRADE.get());
				tabData.accept(HolosparkModItems.ELITE_HOLO_UPGRADE.get());
				tabData.accept(HolosparkModItems.SUPER_HOLO_UPGRADE.get());
				tabData.accept(HolosparkModItems.ULTIMATE_HOLO_UPGRADE.get());
				tabData.accept(HolosparkModItems.ABSOLUTE_HOLO_UPGRADE.get());
			})

					.build());
}
