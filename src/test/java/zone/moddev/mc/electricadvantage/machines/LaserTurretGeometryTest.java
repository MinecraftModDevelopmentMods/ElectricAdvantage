package zone.moddev.mc.electricadvantage.machines;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import org.junit.Test;
import static org.junit.Assert.*;

public class LaserTurretGeometryTest {
    @Test public void nativeRayGeometryKeepsHitsInAllSixDirections() {
        Vector3d origin = new Vector3d(0.5D, 0.5D, 0.5D);
        for (Direction facing : Direction.values()) {
            Vector3d direction = new Vector3d(facing.getStepX(), facing.getStepY(), facing.getStepZ());
            Vector3d center = origin.add(direction.scale(5.0D));
            AxisAlignedBB target = new AxisAlignedBB(center.x - 0.25D, center.y - 0.25D, center.z - 0.25D,
                    center.x + 0.25D, center.y + 0.25D, center.z + 0.25D);
            assertTrue(facing.toString(), LaserTurretTileEntity.rayIntersectsBoundingBox(origin, direction, target));
            assertFalse(facing.toString(), LaserTurretTileEntity.rayIntersectsBoundingBox(origin, direction.scale(-1.0D), target));
        }
    }

    @Test public void anOffsetTargetIsNotHitAndMissingBoundsAreIgnored() {
        Vector3d origin = new Vector3d(0.5D, 0.5D, 0.5D);
        Vector3d direction = new Vector3d(1.0D, 0.0D, 0.0D);
        assertFalse(LaserTurretTileEntity.rayIntersectsBoundingBox(origin, direction,
                new AxisAlignedBB(5.0D, 4.0D, 4.0D, 6.0D, 5.0D, 5.0D)));
        assertFalse(LaserTurretTileEntity.rayIntersectsBoundingBox(origin, direction, null));
    }
}
