package gravity_changer.util;

import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public abstract class QuaternionUtil {
    public static Quaternionf getViewRotation(float pitch, float yaw) {
        Quaternionf r1 = new Quaternionf().fromAxisAngleDeg(new Vector3f(1, 0, 0), pitch);
        Quaternionf r2 = new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 1, 0), yaw + 180);
        r1.mul(r2);
        return r1;
    }
    
    // NOTE the "from" and "to" cannot be opposite
    public static Quaternionf getRotationBetween(Vec3d from, Vec3d to) {
        from = from.normalize();
        to = to.normalize();
        Vec3d axis = from.crossProduct(to).normalize();
        double cos = from.dotProduct(to);
        double angle = Math.acos(cos);
        return new Quaternionf().fromAxisAngleRad(
            new Vector3f((float) axis.x, (float) axis.y, (float) axis.z),
            (float) angle
        );
    }
}
