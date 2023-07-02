package gravity_changer.item;

import gravity_changer.api.GravityChangerAPI;
import gravity_changer.api.RotationParameters;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class GravityChangerItem extends Item {
    public final Direction gravityDirection;

    public GravityChangerItem(Settings settings, Direction _gravityDirection) {
        super(settings);
        gravityDirection = _gravityDirection;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if(!world.isClient())
            GravityChangerAPI.setDefaultGravityDirection(user, gravityDirection, new RotationParameters());
        return TypedActionResult.success(user.getStackInHand(hand));
    }
}
