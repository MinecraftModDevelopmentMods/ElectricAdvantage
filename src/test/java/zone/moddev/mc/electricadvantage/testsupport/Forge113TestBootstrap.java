package zone.moddev.mc.electricadvantage.testsupport;

import java.lang.reflect.Field;
import net.minecraft.init.Bootstrap;

/** Supplies only the launch metadata needed by Forge's vanilla registry owner in plain JUnit. */
public final class Forge113TestBootstrap {
    private Forge113TestBootstrap() {}

    public static synchronized void register() {
        try {
            version("mcVersion", "1.13.2");
            version("forgeVersion", "25.0.223");
            // Plain JUnit has no EventSubclassTransformer-generated EntityEvent constructor.
            java.lang.reflect.Method listeners = Class.forName("net.minecraftforge.eventbus.api.EventListenerHelper")
                    .getDeclaredMethod("getListenerListInternal", Class.class, boolean.class);
            listeners.setAccessible(true);
            listeners.invoke(null, Class.forName("net.minecraftforge.event.entity.EntityEvent"), true);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("Cannot initialize Forge 1.13 test launch metadata", failure);
        }
        Bootstrap.register();
    }

    private static void version(String name, String expected) throws ReflectiveOperationException {
        Field field = Class.forName("net.minecraftforge.fml.loading.FMLLoader").getDeclaredField(name);
        field.setAccessible(true);
        Object current = field.get(null);
        if (current == null) field.set(null, expected);
        else if (!expected.equals(current)) {
            throw new AssertionError("Wrong Forge test environment for " + name + ": " + current);
        }
    }
}
