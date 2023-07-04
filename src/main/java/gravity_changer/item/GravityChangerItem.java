package gravity_changer.item;

import gravity_changer.api.GravityChangerAPI;
import gravity_changer.api.RotationParameters;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GravityChangerItem extends Item {
    public final Direction gravityDirection;
    
    public GravityChangerItem(Properties settings, Direction _gravityDirection) {
        super(settings);
        gravityDirection = _gravityDirection;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide())
            GravityChangerAPI.setBaseGravityDirection(user, gravityDirection, RotationParameters.getDefault());
        return InteractionResultHolder.success(user.getItemInHand(hand));
    }
}
