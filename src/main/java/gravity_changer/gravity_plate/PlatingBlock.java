package gravity_changer.gravity_plate;

import com.google.common.collect.ImmutableMap;
import gravity_changer.GravityEffect;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Based on code from AmethystGravity (by CyborgCabbage)
 */
public class PlatingBlock extends BaseEntityBlock {
    // in a corner, multiple faces of plates can occupy the same block
    
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    
    protected static final VoxelShape DOWN_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    protected static final VoxelShape UP_SHAPE = Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape NORTH_SHAPE = Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape SOUTH_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
    protected static final VoxelShape WEST_SHAPE = Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape EAST_SHAPE = Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
    private final Map<BlockState, VoxelShape> shapesByState;
    
    public final double GRAVITY_EFFECT_HEIGHT;
    
    public static final Block PLATING_BLOCK = new PlatingBlock(
        0.6, FabricBlockSettings.of().noOcclusion().noCollission().instabreak()
    );
    public static final Block DENSE_PLATING_BLOCK = new PlatingBlock(
        1.3, FabricBlockSettings.of().noOcclusion().noCollission().instabreak()
    );
    
    public static void init() {
        registerBlockAndItem(PLATING_BLOCK, new ResourceLocation("gravity_changer:plating"));
        registerBlockAndItem(DENSE_PLATING_BLOCK, new ResourceLocation("gravity_changer:dense_plating"));
    }
    
    private static void registerBlockAndItem(Block block, ResourceLocation id) {
        Registry.register(
            BuiltInRegistries.BLOCK, id, block
        );
        Registry.register(
            BuiltInRegistries.ITEM, id,
            new BlockItem(block, new FabricItemSettings())
        );
    }
    
    public PlatingBlock(double height, Properties settings) {
        super(settings);
        GRAVITY_EFFECT_HEIGHT = height;
        registerDefaultState(getStateDefinition().any()
            .setValue(NORTH, false)
            .setValue(EAST, false)
            .setValue(SOUTH, false)
            .setValue(WEST, false)
            .setValue(UP, false)
            .setValue(DOWN, false)
        );
        this.shapesByState =
            ImmutableMap.copyOf(
                this.stateDefinition.getPossibleStates().stream()
                    .collect(Collectors.toMap(Function.identity(), PlatingBlock::getShapeForState))
            );
    }
    
    private static VoxelShape getShapeForState(BlockState state) {
        VoxelShape voxelShape = Shapes.empty();
        if (state.getValue(UP)) {
            voxelShape = UP_SHAPE;
        }
        if (state.getValue(NORTH)) {
            voxelShape = Shapes.or(voxelShape, SOUTH_SHAPE);
        }
        if (state.getValue(SOUTH)) {
            voxelShape = Shapes.or(voxelShape, NORTH_SHAPE);
        }
        if (state.getValue(EAST)) {
            voxelShape = Shapes.or(voxelShape, WEST_SHAPE);
        }
        if (state.getValue(WEST)) {
            voxelShape = Shapes.or(voxelShape, EAST_SHAPE);
        }
        if (state.getValue(DOWN)) {
            voxelShape = Shapes.or(voxelShape, DOWN_SHAPE);
        }
        return voxelShape.isEmpty() ? Shapes.block() : voxelShape;
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return this.shapesByState.get(state);
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
        stateManager.add(UP, DOWN, NORTH, SOUTH, EAST, WEST);
    }
    
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(directionToProperty(direction)) && !canPlaceOn(world, pos.relative(direction), direction.getOpposite())) {
            state = state.setValue(directionToProperty(direction), false);
            if (getDirections(state).size() == 0) {
                return Blocks.AIR.defaultBlockState();
            }
            else {
                return state;
            }
        }
        else {
            return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
        }
    }
    
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180 -> {
                return (((state.setValue(NORTH, state.getValue(SOUTH))).setValue(EAST, state.getValue(WEST))).setValue(SOUTH, state.getValue(NORTH))).setValue(WEST, state.getValue(EAST));
            }
            case COUNTERCLOCKWISE_90 -> {
                return (((state.setValue(NORTH, state.getValue(EAST))).setValue(EAST, state.getValue(SOUTH))).setValue(SOUTH, state.getValue(WEST))).setValue(WEST, state.getValue(NORTH));
            }
            case CLOCKWISE_90 -> {
                return (((state.setValue(NORTH, state.getValue(WEST))).setValue(EAST, state.getValue(NORTH))).setValue(SOUTH, state.getValue(EAST))).setValue(WEST, state.getValue(SOUTH));
            }
        }
        return state;
    }
    
    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT -> {
                return (state.setValue(NORTH, state.getValue(SOUTH))).setValue(SOUTH, state.getValue(NORTH));
            }
            case FRONT_BACK -> {
                return (state.setValue(EAST, state.getValue(WEST))).setValue(WEST, state.getValue(EAST));
            }
        }
        return super.mirror(state, mirror);
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PlatingBlockEntity(pos, state);
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        // With inheriting from BlockWithEntity this defaults to INVISIBLE, so we need to change that!
        return RenderShape.MODEL;
    }
    
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (world.isClientSide)
            return createTickerHelper(type, PlatingBlockEntity.TYPE, PlatingBlockEntity::tick);
        else
            return createTickerHelper(type, PlatingBlockEntity.TYPE, PlatingBlockEntity::tick);
    }
    
    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (!context.isSecondaryUseActive() && context.getItemInHand().getItem() == this.asItem()) {
            return !state.getValue(directionToProperty(context.getClickedFace().getOpposite()));
        }
        return super.canBeReplaced(state, context);
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState blockState = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (blockState.is(this)) {
            return blockState.setValue(directionToProperty(ctx.getClickedFace().getOpposite()), true);
        }
        return defaultBlockState().setValue(directionToProperty(ctx.getClickedFace().getOpposite()), true);
    }
    
    private boolean canPlaceOn(BlockGetter world, BlockPos pos, Direction side) {
        BlockState blockState = world.getBlockState(pos);
        return blockState.isFaceSturdy(world, pos, side);
    }
    
    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        ArrayList<Direction> directions = getDirections(state);
        if (directions.size() == 1) {
            return canPlaceOn(world, pos.relative(directions.get(0)), directions.get(0).getOpposite());
        }
        //Placing inside an existing plating
        if (directions.size() > 1) {
            for (Direction dir : getDirections(world.getBlockState(pos))) {
                directions.remove(dir);
            }
            return canPlaceOn(world, pos.relative(directions.get(0)), directions.get(0).getOpposite());
        }
        return false;
    }
    
    public static BooleanProperty directionToProperty(Direction direction) {
        return switch (direction) {
            case DOWN -> DOWN;
            case UP -> UP;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
        };
    }
    
    public static void foreachDirection(BlockState blockState, Consumer<Direction> func) {
        for (Direction dir : Direction.values()) {
            if (blockState.getValue(directionToProperty(dir))) {
                func.accept(dir);
            }
        }
    }
    
    public static ArrayList<Direction> getDirections(BlockState blockState) {
        ArrayList<Direction> list = new ArrayList<>();
        //Iterate directions
        for (int directionId = 0; directionId < 6; directionId++) {
            //Convert ID to Direction
            Direction direction = Direction.from3DDataValue(directionId);
            //If the plate has this direction
            if (blockState.getValue(PlatingBlock.directionToProperty(direction))) {
                list.add(direction);
            }
        }
        return list;
    }
    
    public AABB getGravityEffectBox(BlockPos blockPos, Direction direction) {
        double expand = 0.001;
        double minX = blockPos.getX() - expand;
        double minY = blockPos.getY() - expand;
        double minZ = blockPos.getZ() - expand;
        double maxX = blockPos.getX() + 1 + expand;
        double maxY = blockPos.getY() + 1 + expand;
        double maxZ = blockPos.getZ() + 1 + expand;
        //Extend area of effect a bit so the player can jump without falling off
        double delta = GRAVITY_EFFECT_HEIGHT - 1.0;
        switch (direction) {
            case DOWN -> maxY += delta;
            case UP -> minY -= delta;
            case NORTH -> maxZ += delta;
            case SOUTH -> minZ -= delta;
            case WEST -> maxX += delta;
            case EAST -> minX -= delta;
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
