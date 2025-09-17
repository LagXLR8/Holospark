package net.huwng.holospark.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.huwng.holospark.init.HolosparkModBlockEntities;
import net.huwng.holospark.init.HolosparkModItems;
import net.huwng.holospark.init.HolosparkModSounds;
import java.util.ArrayList;
import java.util.List;

public class HoloCoreBlockEntity extends BlockEntity {
    // Default radius & render grid
    public static final int DEFAULT_RADIUS = 24; // world radius = 24 -> 48x48
    public static final int DEFAULT_RENDER_GRID = 3; // compress into 3x3

	private int upgradeRadius = DEFAULT_RADIUS;
	private int scanDurationTicks = DEFAULT_SCAN_SECONDS * 20;
    // fields (persisted)
    private boolean poweredScanned = false; // whether currently scanning/rendering
    private long scanStart = 0L;
    private int worldRadius = DEFAULT_RADIUS; // can change by upgrade (radius = worldRadius -> world area = 2*radius)
    private int renderGrid = DEFAULT_RENDER_GRID; // 3,5,7
    private static final int DEFAULT_SCAN_SECONDS = 3;

    // heightmap (2*radius x 2*radius). We allocate with max possible (use 56 for advanced), but simpler: allocate dynamically.
    private int[][] heightmap = new int[2 * DEFAULT_RADIUS][2 * DEFAULT_RADIUS];
	private int[][] seaLevelMap = new int[2 * DEFAULT_RADIUS][2 * DEFAULT_RADIUS];

    // entities scanned (server side, not persisted long-term)
    private final List<ScannedEntity> scannedEntities = new ArrayList<>();
	// record thêm width + height
	public record ScannedEntity(double x, double y, double z, int color, float width, float height) {}

    public HoloCoreBlockEntity(BlockPos pos, BlockState state) {
        super(HolosparkModBlockEntities.HOLO_CORE.get(), pos, state);
        // initial fields already set
        this.worldRadius = DEFAULT_RADIUS;
        this.renderGrid = DEFAULT_RENDER_GRID;
    }

    // ---------- tick ----------
	public static void tick(Level level, BlockPos pos, BlockState state, HoloCoreBlockEntity be) {
	    if (be.isPoweredScanned()) {
	        be.scanEntities(level, pos); // chạy cả 2 bên
	    }
	    
        boolean powered = level.hasNeighborSignal(pos);

        // If redstone powered: start scanning if not already, and always scan entities every tick
        if (powered) {
            if (!be.poweredScanned) {
                // start a scan
                be.poweredScanned = true;
                be.scanStart = level.getGameTime();
                be.scanDurationTicks = DEFAULT_SCAN_SECONDS * 20; // default; can be changed by upgrades below
                be.buildHeightmap(level, pos); // build full heightmap at start
                be.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);

                // play custom scan sound if you registered it in HolosparkMod
                if (HolosparkModSounds.SCAN.get() != null) {
                    level.playSound(null, pos, HolosparkModSounds.SCAN.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                } else {
                    // fallback
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                }
            }
            // always update entity list while powered
            be.scanEntities(level, pos);
        } else {
            // if power is off, stop scanning/rendering (user asked to allow turn off)
            if (be.poweredScanned) {
                be.poweredScanned = false;
                be.scannedEntities.clear();
                be.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
    }
	// ---------- build heightmap ----------
	private void buildHeightmap(Level level, BlockPos pos) {
	    int radius = this.worldRadius;
	    if (heightmap.length != 2 * radius || heightmap[0].length != 2 * radius) {
	        heightmap = new int[2 * radius][2 * radius];
	    }
	
	    for (int dx = 0; dx < 2 * radius; dx++) {
	        for (int dz = 0; dz < 2 * radius; dz++) {
	            int x = pos.getX() + dx - radius;
	            int z = pos.getZ() + dz - radius;
	
	            // nếu chunk chưa load -> dùng sea level thay vì 0
	            if (!level.isLoaded(new BlockPos(x, level.getSeaLevel(), z))) {
	                heightmap[dx][dz] = level.getSeaLevel();
	                continue;
	            }
	
	            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
	
	            // bỏ cột tụt xuống do nước
	            BlockPos topPos = new BlockPos(x, Math.max(level.getMinBuildHeight(), y - 1), z);
	            var state = level.getBlockState(topPos);
	            while (y > level.getMinBuildHeight() && state.getFluidState().isSource()) {
	                y--;
	                topPos = topPos.below();
	                state = level.getBlockState(topPos);
	            }
	
	            if (y <= level.getMinBuildHeight()) {
	                y = level.getSeaLevel();
	            }
	
	            heightmap[dx][dz] = y;
	        }
	    }
	}
	
	// ---------- entity scan (lọc hang động, giữ entity dưới biển) ----------
	private void scanEntities(Level level, BlockPos pos) {
	    scannedEntities.clear();
	    int r = this.worldRadius;
	    AABB box = new AABB(pos).inflate(r);
	
	    for (Entity e : level.getEntities(null, box)) {
	        if (e == null) continue;
	
	        int color;
	        if (e instanceof Player) color = 0x00FF00;
	        else if (e.getType().getCategory() == MobCategory.MONSTER) color = 0xFF0000;
	        else if (e instanceof LivingEntity) color = 0xFFFF00;
	        else if (e instanceof ItemEntity) color = 0xFFFFFF;
	        else continue;
	
	        // lọc entity trong hang nhưng cho phép entity dưới biển
	        int mapX = (int) Math.floor(e.getX()) - pos.getX() + r;
	        int mapZ = (int) Math.floor(e.getZ()) - pos.getZ() + r;
	        if (mapX >= 0 && mapX < 2 * r && mapZ >= 0 && mapZ < 2 * r) {
	            int groundY = heightmap[mapX][mapZ];
	            if (e.getY() < groundY - 2 && !e.isInWater()) {
	                continue; // bỏ entity dưới lòng đất
	            }
	        }
	
	        float width = e.getBbWidth();
	        float height = e.getBbHeight();
	
	        scannedEntities.add(new ScannedEntity(
	            e.getX(),
	            e.getY(),
	            e.getZ(),
	            color,
	            width,
	            height
	        ));
	    }
        // mark changed occasionally to sync? we avoid spamming updates; client reads scannedEntities via persistent BE updates at start + entities are transient; to ensure rendering updates each frame on client, we rely on client-side renderer reading BE (the client needs regular updates).
        // We will still call setChanged occasionally if desired (left out to avoid packet spam).
    }

    // ---------- interaction helpers (called from block.use) ----------
    /** Toggle render on/off (called when player right-clicks with empty hand). */
    public void togglePower(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        this.poweredScanned = !this.poweredScanned;
        if (this.poweredScanned) {
            this.scanStart = level.getGameTime();
            this.buildHeightmap(level, pos);
            if (HolosparkModSounds.SCAN.get() != null) {
                level.playSound(null, pos, HolosparkModSounds.SCAN.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        } else {
            this.scannedEntities.clear();
        }
        this.setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
    }

    /** Apply upgrade (Basic/Advanced) - consume one itemstack by caller. */
	public boolean applyUpgrade(Level level, BlockPos pos, BlockState state, String upgrade) {
	    if (level.isClientSide) return false;
	
	    if ("basic".equals(upgrade)) {
	        this.worldRadius = 40;   // 80x80
	        this.renderGrid = 5;
	        this.scanDurationTicks = 4 * 20;
	    } else if ("advanced".equals(upgrade)) {
	        this.worldRadius = 56;   // 112x112
	        this.renderGrid = 7;
	        this.scanDurationTicks = 6 * 20;
	    } else if ("elite".equals(upgrade)) {
	        this.worldRadius = 72;   // 144x144
	        this.renderGrid = 9;
	        this.scanDurationTicks = 7 * 20;
	    } else if ("super".equals(upgrade)) {
	        this.worldRadius = 88;   // 176x176
	        this.renderGrid = 11;
	        this.scanDurationTicks = 8 * 20;
	    } else if ("ultimate".equals(upgrade)) {
	        this.worldRadius = 104;  // 208x208
	        this.renderGrid = 13;
	        this.scanDurationTicks = 8 * 20;
	    } else if ("absolute".equals(upgrade)) {
	        this.worldRadius = 136;  // 272
	        this.renderGrid = 17;
	        this.scanDurationTicks = 10 * 20;
	    } else {
	        return false;
	    }
	
	    // rebuild heightmap và restart scan nếu đang bật
	    this.buildHeightmap(level, pos);
	    this.scanStart = level.getGameTime();
	    this.poweredScanned = true;
	    this.setChanged();
	    level.sendBlockUpdated(pos, state, state, 3);
	
	    if (HolosparkModSounds.SCAN.get() != null) {
	        level.playSound(null, pos, HolosparkModSounds.SCAN.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
	    }
	
	    return true;
	}

    // ---------- getters for renderer ----------
    public boolean isPoweredScanned() { return poweredScanned; }
    public long getScanStart() { return scanStart; }
    public int getScanDurationTicks() { return scanDurationTicks; }
    public int getWorldRadius() { return worldRadius; }
    public int getRenderGrid() { return renderGrid; }
    public int[][] getHeightmap() { return heightmap; }
    public List<ScannedEntity> getScannedEntities() { return scannedEntities; }

    // ---------- NBT sync ----------
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("PoweredScanned", poweredScanned);
        tag.putLong("ScanStart", scanStart);
        tag.putInt("WorldRadius", worldRadius);
        tag.putInt("RenderGrid", renderGrid);
        tag.putInt("ScanDurationTicks", scanDurationTicks);

        // save heightmap (flatten) up to current worldRadius
        int r = worldRadius;
        int[] flat = new int[2 * r * 2 * r];
        int idx = 0;
        for (int i = 0; i < 2 * r; i++) {
            for (int j = 0; j < 2 * r; j++) {
                flat[idx++] = (i < heightmap.length && j < heightmap[0].length) ? heightmap[i][j] : 0;
            }
        }
        tag.putIntArray("Heightmap", flat);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.poweredScanned = tag.getBoolean("PoweredScanned");
        this.scanStart = tag.getLong("ScanStart");
        this.worldRadius = tag.contains("WorldRadius") ? tag.getInt("WorldRadius") : DEFAULT_RADIUS;
        this.renderGrid = tag.contains("RenderGrid") ? tag.getInt("RenderGrid") : DEFAULT_RENDER_GRID;
        this.scanDurationTicks = tag.contains("ScanDurationTicks") ? tag.getInt("ScanDurationTicks") : DEFAULT_SCAN_SECONDS * 20;

        if (tag.contains("Heightmap")) {
            int r = this.worldRadius;
            int[] flat = tag.getIntArray("Heightmap");
            // allocate
            this.heightmap = new int[2 * r][2 * r];
            int idx = 0;
            for (int i = 0; i < 2 * r; i++) {
                for (int j = 0; j < 2 * r; j++) {
                    if (idx < flat.length) heightmap[i][j] = flat[idx++];
                    else heightmap[i][j] = 0;
                }
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(pkt.getTag());
    }
}
