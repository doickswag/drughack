package fun.drughack.modules.impl.movement;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventPlayerTick;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.utils.math.TimerUtils;
import fun.drughack.utils.rotations.RotationChanger;
import fun.drughack.utils.rotations.RotationUtils;
import fun.drughack.utils.world.InventoryUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.item.BlockItem;

public class Scaffold extends Module {

    public Scaffold() {
        super("Scaffold", Category.Movement);
    }

    private float[] rotations;
    private final TimerUtils timer = new TimerUtils();
    private final RotationChanger changer = new RotationChanger(
        5000,
        () -> new Float[]{rotations[0], rotations[1]},
        () -> Module.fullNullCheck() || timer.passed(450)
    );

    @EventHandler
    public void onPlayerTick(EventPlayerTick e) {
        if (fullNullCheck()) return;

        int slot = InventoryUtils.findHotbar(BlockItem.class);
        int previousSlot = mc.player.getInventory().selectedSlot;
        if (slot == -1) return;
        Pair<BlockPos, Direction> pair = getBlockDirectionExtended(mc.player.getBlockPos().down());
        BlockPos pos = new BlockPos((int) Math.floor(mc.player.getX()), (int) (Math.floor(mc.player.getY() - 1)), (int) Math.floor(mc.player.getZ()));
        if (pair == null) return;

    	 if (mc.world.getBlockState(pos).isReplaceable()) {
    		 BlockHitResult result = getHitResult(new Pair<>(pair.getLeft(), pair.getRight()));
    		 rotations = RotationUtils.getRotations(pair.getLeft().toCenterPos());
    		 DrugHack.getInstance().getRotationManager().addRotation(changer);
    		 InventoryUtils.switchSlot(InventoryUtils.Switch.Normal, slot, previousSlot);
    		 mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, result);
    		 InventoryUtils.swing(InventoryUtils.Swing.MainHand);
    		 InventoryUtils.switchBack(InventoryUtils.Switch.Normal, slot, previousSlot);
    		 timer.reset();
        }
    }
    
    private Pair<BlockPos, Direction> getBlockDirectionExtended(BlockPos pos) {
    	Pair<BlockPos, Direction> block = null;

    	block = getBlockDirection(pos);
        if (block != null) return block;
        block = getBlockDirection(pos.add(-1, 0, 0));
        if (block != null) return block;
        block = getBlockDirection(pos.add(1, 0, 0));
        if (block != null) return block;
        block = getBlockDirection(pos.add(0, 0, 1));
        if (block != null) return block;
        block = getBlockDirection(pos.add(0, 0, -1));
        if (block != null) return block;
        block = getBlockDirection(pos.add(-2, 0, 0));
        if (block != null) return block;
        block = getBlockDirection(pos.add(2, 0, 0));
        if (block != null) return block;
        block = getBlockDirection(pos.add(0, 0, 2));
        if (block != null) return block;
        block = getBlockDirection(pos.add(0, 0, -2));
        if (block != null) return block;
        block = getBlockDirection(pos.add(0, -1, 0));
        if (block != null) return block;
        block = getBlockDirection(pos.add(1, -1, 0));
        if (block != null) return block;
        block = getBlockDirection(pos.add(-1, -1, 0));
        if (block != null) return block;
        block = getBlockDirection(pos.add(0, -1, 1));
        if (block != null) return block;

        return getBlockDirection(pos.add(0, -1, -1));
    }

    private Pair<BlockPos, Direction> getBlockDirection(BlockPos pos) {
        if (mc.world.getBlockState(pos.add(0, -1, 0)).isSolidBlock(mc.world, pos.add(0, -1, 0))) return new Pair<>(pos.add(0, -1, 0), Direction.UP);
        else if (mc.world.getBlockState(pos.add(-1, 0, 0)).isSolidBlock(mc.world, pos.add(-1, 0, 0))) return new Pair<>(pos.add(-1, 0, 0), Direction.EAST);
        else if (mc.world.getBlockState(pos.add(1, 0, 0)).isSolidBlock(mc.world, pos.add(1, 0, 0))) return new Pair<>(pos.add(1, 0, 0), Direction.WEST);
        else if (mc.world.getBlockState(pos.add(0, 0, 1)).isSolidBlock(mc.world, pos.add(0, 0, 1))) return new Pair<>(pos.add(0, 0, 1), Direction.NORTH);
        else if (mc.world.getBlockState(pos.add(0, 0, -1)).isSolidBlock(mc.world, pos.add(0, 0, -1))) return new Pair<>(pos.add(0, 0, -1), Direction.SOUTH);
        
        return null;
    }
	
	public BlockHitResult getHitResult(Pair<BlockPos, Direction> position) {
        Vec3d pos = position.getLeft().toCenterPos();
        return new BlockHitResult(new Vec3d(pos.getX(), pos.getY(), pos.getZ()).add(new Vec3d(position.getRight().getVector()).multiply(0.5)), position.getRight(), position.getLeft(), false);
    }
    
    @Override
    public void onDisable() {
        super.onDisable();
        timer.reset();
    }
}