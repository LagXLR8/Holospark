package net.huwng.holospark.util;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.huwng.holospark.block.entity.HoloCoreBlockEntity;
import net.huwng.holospark.init.HolosparkModBlockEntities;

public class ModBlockEntityTypes {
    @SuppressWarnings("unchecked")
    public static final BlockEntityType<HoloCoreBlockEntity> HOLO_CORE =
        (BlockEntityType<HoloCoreBlockEntity>) (Object) HolosparkModBlockEntities.HOLO_CORE.get();
}
