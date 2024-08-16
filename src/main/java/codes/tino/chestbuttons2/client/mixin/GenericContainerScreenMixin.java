package codes.tino.chestbuttons2.client.mixin;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GenericContainerScreen.class)
public abstract class GenericContainerScreenMixin
        extends HandledScreen<GenericContainerScreenHandler>
        implements ScreenHandlerProvider<GenericContainerScreenHandler> {
    @Shadow
    @Final
    private int rows;

    private int mode;

    private static final int MODE_STEAL = 1;
    private static final int MODE_STORE = 2;
    private static final int MODE_GARBAGE = 3;
    private static final int MODE_FOOD = 4;
    private static final int MODE_FUEL = 5;
    private static final int MODE_ITEMS = 6;
    private static final int MODE_ORES = 7;
    private static final int MODE_SHOP = 8;

    public GenericContainerScreenMixin(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();

        int buttonHeight = 18;
        int buttonWidth = 28;

        // y + 20

        // 1. Zeile - Garbage / Food
        addDrawableChild(
                new ButtonWidget
                        .Builder(Text.of("Gar"), b -> garbage())
                        .dimensions(x + backgroundWidth + 10, y + 2, buttonWidth, buttonHeight)
                        .build()
        );

        addDrawableChild(
                new ButtonWidget
                        .Builder(Text.of("Foo"), b -> food())
                        .dimensions(x + backgroundWidth + 42, y + 2, buttonWidth, buttonHeight)
                        .build()
        );

        // 2. Zeile - Fuel / Items
        addDrawableChild(
                new ButtonWidget
                        .Builder(Text.of("Fue"), b -> fuel())
                        .dimensions(x + backgroundWidth + 10, y + 22, buttonWidth, buttonHeight)
                        .build()
        );

        addDrawableChild(
                new ButtonWidget
                        .Builder(Text.of("Ite"), b -> items())
                        .dimensions(x + backgroundWidth + 42, y + 22, buttonWidth, buttonHeight)
                        .build()
        );

        // 3. Zeile - Ores / Shop
        addDrawableChild(
                new ButtonWidget
                        .Builder(Text.of("Ore"), b -> ores())
                        .dimensions(x + backgroundWidth + 10, y + 42, buttonWidth, buttonHeight)
                        .build()
        );

        addDrawableChild(
                new ButtonWidget
                        .Builder(Text.of("Sho"), b -> shop())
                        .dimensions(x + backgroundWidth + 42, y + 42, buttonWidth, buttonHeight)
                        .build()
        );

        // 4. Zeile - ↓ / ↑
        addDrawableChild(
                new ButtonWidget
                        .Builder(Text.of("↓"), b -> steal())
                        .dimensions(x + backgroundWidth + 10, y + 62, buttonWidth, buttonHeight)
                        .build()
        );

        addDrawableChild(
                new ButtonWidget
                        .Builder(Text.of("↑"), b -> store())
                        .dimensions(x + backgroundWidth + 42, y + 62, buttonWidth, buttonHeight)
                        .build()
        );
    }

    private void steal() {
        runInThread(() -> shiftClickSlots(0, rows * 9, MODE_STEAL));
    }

    private void store() {
        runInThread(() -> shiftClickSlots(rows * 9, rows * 9 + 44, MODE_STORE));
    }

    private void garbage() {
        runInThread(() -> shiftClickSlots(0, rows * 9, MODE_GARBAGE));
    }

    private void food() {
        runInThread(() -> shiftClickSlots(0, rows * 9, MODE_FOOD));
    }

    private void fuel() {
        runInThread(() -> shiftClickSlots(0, rows * 9, MODE_FUEL));
    }

    private void items() {
        runInThread(() -> shiftClickSlots(0, rows * 9, MODE_ITEMS));
    }

    private void ores() {
        runInThread(() -> shiftClickSlots(0, rows * 9, MODE_ORES));
    }

    private void shop() {
        runInThread(() -> shiftClickSlots(0, rows * 9, MODE_SHOP));
    }


    private void runInThread(Runnable r) {
        new Thread(() -> {
            try {
                r.run();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void shiftClickSlots(int from, int to, int mode) {
        this.mode = mode;

        for (int i = from; i < to; i++) {
            if (handler.slots.size() == i) {
                break;
            }

            Slot slot = handler.slots.get(i);
            if (slot.getStack().isEmpty()) {
                continue;
            }

            String translation = slot.getStack().getItem().getTranslationKey();
            //ChestButtonsMod.LOGGER.info(translation);

            if (mode == MODE_GARBAGE && !isGarbageItem(translation)) {
                continue;
            }

            if (mode == MODE_FUEL &&
                    !(
                            (
                                    translation.contains("block.minecraft.spruce_") ||
                                            translation.contains("block.minecraft.oak_") ||
                                            translation.contains("block.minecraft.birch_") ||
                                            translation.contains("block.minecraft.jungle_") ||
                                            translation.contains("block.minecraft.acacia_") ||
                                            translation.contains("block.minecraft.dark_oak_") ||
                                            translation.contains("block.minecraft.mangrove_") ||
                                            translation.contains("block.minecraft.cherry_") ||
                                            translation.contains("block.minecraft.crimson_") ||
                                            translation.contains("block.minecraft.warped_")
                            )
                                    && !translation.contains("_wood")
                                    && !translation.contains("_sapling")
                                    && !translation.contains("_planks")
                                    && !translation.contains("_roots")
                    ) &&
                    !translation.contains("item.minecraft.wooden_") &&
                    !translation.equals("block.minecraft.crafting_table") &&
                    !translation.equals("block.minecraft.ladder") &&
                    !translation.equals("block.minecraft.scaffolding") &&
                    !translation.equals("item.minecraft.bow") &&
                    !translation.equals("item.minecraft.crossbow")
            ) {
                continue;
            }

            if (mode == MODE_ITEMS && !isSmeltingItem(translation)) {
                continue;
            }

            // Ores
            if (mode == MODE_ORES &&
                    !translation.equals("block.minecraft.gold_block") &&
                    !translation.equals("block.minecraft.iron_block") &&
                    !translation.equals("item.minecraft.coal") &&
                    !translation.equals("item.minecraft.copper_ingot") &&
                    !translation.equals("item.minecraft.diamond") &&
                    !translation.equals("item.minecraft.emerald") &&
                    !translation.equals("item.minecraft.flint") &&
                    !translation.equals("item.minecraft.gold_ingot") &&
                    !translation.equals("item.minecraft.gold_nugget") &&
                    !translation.equals("item.minecraft.iron_ingot") &&
                    !translation.equals("item.minecraft.iron_nugget") &&
                    !translation.equals("item.minecraft.lapis_lazuli") &&
                    !translation.equals("item.minecraft.quartz") &&
                    !translation.equals("item.minecraft.raw_diamond") &&
                    !translation.equals("item.minecraft.raw_gold") &&
                    !translation.equals("item.minecraft.raw_iron") &&
                    !translation.equals("item.minecraft.raw_copper") &&
                    !translation.equals("item.minecraft.netherite_ingot")
            ) {
                continue;
            }

            if (mode == MODE_FOOD && !isFoodItem(translation)) {
                continue;
            }

            if (mode == MODE_SHOP && !isShopItem(translation)) {
                continue;
            }

            waitForDelay();

            if (this.mode != mode || client.currentScreen == null) {
                break;
            }

            onMouseClick(slot, slot.id, 0, SlotActionType.QUICK_MOVE);
        }
    }

    private void waitForDelay() {
        try {
            Thread.sleep(70);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isSmeltingItem(String item) {
        if (
                item.equals("item.minecraft.iron_axe") ||
                        item.equals("item.minecraft.iron_boots") ||
                        item.equals("item.minecraft.iron_chestplate") ||
                        item.equals("item.minecraft.iron_helmet") ||
                        item.equals("item.minecraft.iron_leggings") ||
                        item.equals("item.minecraft.iron_pickaxe") ||
                        item.equals("item.minecraft.iron_shovel") ||
                        item.equals("item.minecraft.iron_sword") ||
                        item.equals("item.minecraft.iron_hoe") ||
                        item.contains("item.minecraft.chainmail_")
        ) {
            return true;
        }

        if (item.contains("item.minecraft.golden_") && !item.equals("item.minecraft.golden_carrot")) {
            return true;
        }

        return false;
    }

    private boolean isFoodItem(String item) {
        if (
                item.startsWith("item.minecraft.cooked_") ||
                        item.equals("item.minecraft.apple") ||
                        item.equals("item.minecraft.bread") ||
                        item.equals("item.minecraft.cookie")
        ) {
            return true;
        }

        return false;
    }

    private boolean isShopItem(String item) {
        return item.equals("block.minecraft.cactus") ||
                item.equals("item.minecraft.wheat") ||
                item.equals("item.minecraft.book") ||
                item.equals("item.minecraft.wheat_seeds") ||
                item.equals("block.minecraft.bamboo") ||
                item.equals("block.minecraft.repeater") ||
                item.equals("block.minecraft.comparator") ||
                item.equals("item.minecraft.sweet_berries") ||
                item.equals("item.minecraft.beetroot_seeds") ||
                item.equals("item.minecraft.cocoa_beans") ||
                item.equals("item.minecraft.nether_wart") ||
                item.equals("item.minecraft.experience_bottle") ||
                item.equals("item.minecraft.totem_of_undying");
    }

    private boolean isGarbageItem(String item) {
        if (
                item.contains("item.minecraft.leather_") ||
                        item.contains("item.minecraft.stone_") ||
                        item.equals("block.minecraft.carved_pumpkin") ||
                        item.equals("block.minecraft.cobbled_deepslate") ||
                        item.equals("block.minecraft.dirt") ||
                        item.equals("block.minecraft.green_dye") ||
                        item.equals("block.minecraft.netherrack") ||
                        item.equals("block.minecraft.pumpkin") ||
                        item.equals("block.minecraft.sugar_cane") ||
                        item.equals("item.minecraft.arrow") ||
                        item.equals("item.minecraft.bone") ||
                        item.equals("item.minecraft.bone_meal") ||
                        item.equals("item.minecraft.egg") ||
                        item.equals("item.minecraft.green_dye") ||
                        item.equals("item.minecraft.leather") ||
                        item.equals("item.minecraft.leather_leggings") ||
                        item.equals("item.minecraft.melon_slice") ||
                        item.equals("item.minecraft.rotten_flesh") ||
                        item.equals("item.minecraft.slime_ball") ||
                        item.equals("item.minecraft.spider_eye") ||
                        item.equals("item.minecraft.string") ||
                        item.equals("item.minecraft.sugar")
        ) {
            return true;
        }

        return false;
    }
}