package gravity_changer.item;

import gravity_changer.GravityChangerMod;
import gravity_changer.api.GravityChangerAPI;
import gravity_changer.api.RotationParameters;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GravityChangerItem extends Item {
    public static final Item GRAVITY_CHANGER_DOWN = new GravityChangerItem(new Properties().stacksTo(1), Direction.DOWN);
    public static final Item GRAVITY_CHANGER_UP = new GravityChangerItem(new Properties().stacksTo(1), Direction.UP);
    public static final Item GRAVITY_CHANGER_NORTH = new GravityChangerItem(new Properties().stacksTo(1), Direction.NORTH);
    public static final Item GRAVITY_CHANGER_SOUTH = new GravityChangerItem(new Properties().stacksTo(1), Direction.SOUTH);
    public static final Item GRAVITY_CHANGER_WEST = new GravityChangerItem(new Properties().stacksTo(1), Direction.WEST);
    public static final Item GRAVITY_CHANGER_EAST = new GravityChangerItem(new Properties().stacksTo(1), Direction.EAST);
    
    public final Direction gravityDirection;
    
    public GravityChangerItem(Properties settings, Direction _gravityDirection) {
        super(settings);
        gravityDirection = _gravityDirection;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide())
            GravityChangerAPI.setBaseGravityDirection(user, gravityDirection);
        return InteractionResultHolder.success(user.getItemInHand(hand));
    }
    
    public static void init() {
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(GravityChangerMod.NAMESPACE, "gravity_changer_down"), GRAVITY_CHANGER_DOWN);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(GravityChangerMod.NAMESPACE, "gravity_changer_up"), GRAVITY_CHANGER_UP);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(GravityChangerMod.NAMESPACE, "gravity_changer_north"), GRAVITY_CHANGER_NORTH);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(GravityChangerMod.NAMESPACE, "gravity_changer_south"), GRAVITY_CHANGER_SOUTH);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(GravityChangerMod.NAMESPACE, "gravity_changer_west"), GRAVITY_CHANGER_WEST);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(GravityChangerMod.NAMESPACE, "gravity_changer_east"), GRAVITY_CHANGER_EAST);
    }
    
}
