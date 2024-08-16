package codes.tino.chestbuttons2.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Chestbuttons2Client implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("chestbuttons");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Hello from ChestButtons!");
    }
}
