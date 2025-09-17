
package net.huwng.holospark.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class BasicHoloUpgradeItem extends Item {
	public BasicHoloUpgradeItem() {
		super(new Item.Properties().stacksTo(1).rarity(Rarity.COMMON));
	}
}
