package gravity_changer.item;

import gravity_changer.GravityChangerMod;
import gravity_changer.api.GravityChangerAPI;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class GravityChangerItemAOE extends Item {
    public static final Item GRAVITY_CHANGER_DOWN_AOE = new GravityChangerItemAOE(new Properties().stacksTo(1), Direction.DOWN);
    public static final Item GRAVITY_CHANGER_UP_AOE = new GravityChangerItemAOE(new Properties().stacksTo(1), Direction.UP);
    public static final Item GRAVITY_CHANGER_NORTH_AOE = new GravityChangerItemAOE(new Properties().stacksTo(1), Direction.NORTH);
    public static final Item GRAVITY_CHANGER_SOUTH_AOE = new GravityChangerItemAOE(new Properties().stacksTo(1), Direction.SOUTH);
    public static final Item GRAVITY_CHANGER_WEST_AOE = new GravityChangerItemAOE(new Properties().stacksTo(1), Direction.WEST);
    public static final Item GRAVITY_CHANGER_EAST_AOE = new GravityChangerItemAOE(new Properties().stacksTo(1), Direction.EAST);
    
    public final Direction gravityDirection;
    
    public GravityChangerItemAOE(Properties settings, Direction _gravityDirection) {
        super(settings);
        gravityDirection = _gravityDirection;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide()) {
            AABB box = user.getBoundingBox().inflate(3);
            List<Entity> list = world.getEntitiesOfClass(Entity.class, box, e -> !(e instanceof Player));
            for (Entity entity : list) {
                GravityChangerAPI.setBaseGravityDirection(entity, gravityDirection);
            }
        }
        return InteractionResultHolder.success(user.getItemInHand(hand));
    }
    
    public static void init() {
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(GravityChangerMod.NAMESPACE, "gravity_changer_down_aoe"), GravityChangerItemAOE.GRAVITY_CHANGER_DOWN_AOE);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(GravityChangerMod.NAMESPACE, "gravity_changer_up_aoe"), GravityChangerItemAOE.GRAVITY_CHANGER_UP_AOE);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(GravityChangerMod.NAMESPACE, "gravity_changer_north_aoe"), GravityChangerItemAOE.GRAVITY_CHANGER_NORTH_AOE);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(GravityChangerMod.NAMESPACE, "gravity_changer_south_aoe"), GravityChangerItemAOE.GRAVITY_CHANGER_SOUTH_AOE);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(GravityChangerMod.NAMESPACE, "gravity_changer_west_aoe"), GravityChangerItemAOE.GRAVITY_CHANGER_WEST_AOE);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(GravityChangerMod.NAMESPACE, "gravity_changer_east_aoe"), GravityChangerItemAOE.GRAVITY_CHANGER_EAST_AOE);
    }
}
