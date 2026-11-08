package dev.craftingtableevolving.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import dev.craftingtableevolving.screen.WallCraftingScreenHandler;

import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class WallCraftingTableBlock extends HorizontalFacingBlock {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    private static final Direction[] HORIZONTAL = {
        Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };

    // Boxes in the north-facing orientation: a rail against the wall, two
    // chains, and a shallow worktop hanging close to the wall.
    private static final double[][] COLLISION_BOXES = {
        {0, 12, 12, 16, 16, 16},  // wall rail
        {1, 5, 13, 15, 9, 16}     // hanging worktop
    };

    private static final double[][] OUTLINE_BOXES = {
        {0, 12, 12, 16, 16, 16},  // wall rail
        {4, 9, 14, 6, 12, 15},    // left chain
        {10, 9, 14, 12, 12, 15},  // right chain
        {1, 5, 13, 15, 9, 16}     // hanging worktop
    };

    private static final VoxelShape[] COLLISION_SHAPES =
        createShapes(COLLISION_BOXES);
    private static final VoxelShape[] OUTLINE_SHAPES =
        createShapes(OUTLINE_BOXES);

    public WallCraftingTableBlock(Settings settings) {
        super(settings);
        setDefaultState(stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction side = context.getSide();
        if (!side.getAxis().isHorizontal()) {
            return null;
        }

        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockPos supportPos = pos.offset(side.getOpposite());
        if (!world.getBlockState(supportPos).isSideSolidFullSquare(world, supportPos, side)) {
            return null;
        }

        return getDefaultState().with(FACING, side);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction facing = state.get(FACING);
        BlockPos supportPos = pos.offset(facing.getOpposite());
        return world.getBlockState(supportPos)
            .isSideSolidFullSquare(world, supportPos, facing);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState state,
        Direction direction,
        BlockState neighborState,
        WorldAccess world,
        BlockPos pos,
        BlockPos neighborPos
    ) {
        if (direction.getOpposite() == state.get(FACING)
            && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }

        return super.getStateForNeighborUpdate(
            state,
            direction,
            neighborState,
            world,
            pos,
            neighborPos
        );
    }

    @Override
    public ActionResult onUse(
        BlockState state,
        World world,
        BlockPos pos,
        PlayerEntity player,
        Hand hand,
        BlockHitResult hit
    ) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
            (syncId, playerInventory, p) -> new WallCraftingScreenHandler(
                syncId,
                playerInventory,
                world,
                pos
            ),
            Text.translatable(state.getBlock().getTranslationKey())
        ));
        return ActionResult.CONSUME;
    }

    @Override
    public VoxelShape getOutlineShape(
        BlockState state,
        BlockView world,
        BlockPos pos,
        ShapeContext context
    ) {
        return OUTLINE_SHAPES[shapeIndex(state.get(FACING))];
    }

    @Override
    public VoxelShape getCollisionShape(
        BlockState state,
        BlockView world,
        BlockPos pos,
        ShapeContext context
    ) {
        return COLLISION_SHAPES[shapeIndex(state.get(FACING))];
    }

    private static int shapeIndex(Direction facing) {
        return switch (facing) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
    }

    private static VoxelShape[] createShapes(double[][] boxes) {
        VoxelShape[] shapes = new VoxelShape[HORIZONTAL.length];

        for (int i = 0; i < HORIZONTAL.length; i++) {
            VoxelShape shape = VoxelShapes.empty();

            for (double[] box : boxes) {
                double[] rotated = rotateBox(box, HORIZONTAL[i]);
                shape = VoxelShapes.union(
                    shape,
                    Block.createCuboidShape(
                        rotated[0], rotated[1], rotated[2],
                        rotated[3], rotated[4], rotated[5]
                    )
                );
            }

            shapes[i] = shape;
        }

        return shapes;
    }

    private static double[] rotateBox(double[] box, Direction facing) {
        double x1 = box[0];
        double y1 = box[1];
        double z1 = box[2];
        double x2 = box[3];
        double y2 = box[4];
        double z2 = box[5];

        return switch (facing) {
            case EAST -> new double[] {16 - z2, y1, x1, 16 - z1, y2, x2};
            case SOUTH -> new double[] {16 - x2, y1, 16 - z2, 16 - x1, y2, 16 - z1};
            case WEST -> new double[] {z1, y1, 16 - x2, z2, y2, 16 - x1};
            default -> new double[] {x1, y1, z1, x2, y2, z2};
        };
    }
}
