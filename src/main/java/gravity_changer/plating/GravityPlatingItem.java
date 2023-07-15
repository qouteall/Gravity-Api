package gravity_changer.plating;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GravityPlatingItem extends BlockItem {
    public static final Item PLATING_BLOCK_ITEM = new GravityPlatingItem(GravityPlatingBlock.PLATING_BLOCK, new FabricItemSettings());
    
    public static void init() {
        Registry.register(
            BuiltInRegistries.ITEM, new ResourceLocation("gravity_changer:plating"),
            GravityPlatingItem.PLATING_BLOCK_ITEM
        );
    }
    
    public GravityPlatingItem(Block block, Properties properties) {
        super(block, properties);
    }
    
    public static @Nullable GravityPlatingBlockEntity.SideData getSideData(@Nullable CompoundTag tag) {
        if (tag == null) {
            return null;
        }
        
        if (tag.contains("sideData")) {
            CompoundTag t = tag.getCompound("sideData");
            return GravityPlatingBlockEntity.SideData.fromTag(t);
        }
        return null;
    }
    
    public static void setSideData(CompoundTag tag, @Nullable GravityPlatingBlockEntity.SideData sideData) {
        if (sideData != null) {
            tag.put("sideData", sideData.toTag());
        }
        else {
            tag.remove("sideData");
        }
    }
    
    public static ItemStack createStack(@Nullable GravityPlatingBlockEntity.SideData sideData) {
        ItemStack itemStack = new ItemStack(GravityPlatingItem.PLATING_BLOCK_ITEM);
        setSideData(itemStack.getOrCreateTag(), sideData);
        return itemStack;
    }
    
    @Override
    public Component getName(ItemStack stack) {
        GravityPlatingBlockEntity.SideData sideData = getSideData(stack.getTag());
        if (sideData != null) {
            return Component.translatable(
                "gravity_changer.plating.item_name",
                sideData.level, GravityPlatingBlockEntity.getForceText(sideData.isAttracting)
            );
        }
        
        return super.getName(stack);
    }
    
    @Override
    public InteractionResult place(BlockPlaceContext context) {
        InteractionResult result = super.place(context);
        
        Level level = context.getLevel();
        ItemStack itemStack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();
        
        if (level.isClientSide()) {
            return result;
        }
        
        GravityPlatingBlockEntity.SideData sideData = getSideData(itemStack.getOrCreateTag());
        
        if (sideData != null) {
            BlockEntity blockEntity = level.getBlockEntity(clickedPos);
            if (blockEntity instanceof GravityPlatingBlockEntity be) {
                be.onPlacing(context.getClickedFace().getOpposite(), sideData);
            }
        }
        
        return result;
    }
    
    @Override
    public void appendHoverText(ItemStack itemStack, Level world, List<Component> tooltip, TooltipFlag tooltipContext) {
        tooltip.add(Component.translatable("gravity_changer.plating.tooltip.0").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gravity_changer.plating.tooltip.1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gravity_changer.plating.tooltip.2").withStyle(ChatFormatting.GRAY));
    }
}