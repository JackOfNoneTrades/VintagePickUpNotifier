package org.fentanylsolutions.vintagepickupnotifier;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import org.fentanylsolutions.vintagepickupnotifier.config.AnchorPoint;
import org.fentanylsolutions.vintagepickupnotifier.config.CombineEntries;
import org.fentanylsolutions.vintagepickupnotifier.config.DisplayAmount;
import org.fentanylsolutions.vintagepickupnotifier.config.EntryBackground;
import org.fentanylsolutions.vintagepickupnotifier.config.MoveOut;
import org.fentanylsolutions.vintagepickupnotifier.config.TextColor;

public class Config {

    private static Configuration config;
    private static File configFile;
    private static final boolean DEFAULT_EXPERIENCE_VALUE = true;

    public static class Categories {

        public static final String DEBUG = "debug";
        public static final String GENERAL = "general";
        public static final String BEHAVIOR = "behavior";
        public static final String DISPLAY = "display";
        public static final String SERVER = "server";
    }

    public static boolean debugMode;

    public static boolean clientOnly = false;
    public static boolean includeItems = true;
    public static boolean includeExperience = true;
    public static boolean includeArrows = true;
    public static boolean experienceValue = DEFAULT_EXPERIENCE_VALUE;
    public static boolean disableInCreative = false;
    public static String[] hiddenItemsRaw = new String[0];
    public static Set<Item> hiddenItems = Collections.emptySet();

    public static CombineEntries combineEntries = CombineEntries.EXCLUDE_NAMED;
    public static int displayTime = 80;
    public static MoveOut moveOut = MoveOut.VERTICALLY_ONLY;
    public static int moveTime = 20;
    public static boolean fadeOut = true;

    public static boolean drawSprite = true;
    public static TextColor textColor = TextColor.WHITE;
    public static boolean ignoreRarity = false;
    public static AnchorPoint position = AnchorPoint.BOTTOM_RIGHT;
    public static int offsetX = 8;
    public static int offsetY = 4;
    public static double maxHeight = 0.5D;
    public static int displayScale = 4;
    public static DisplayAmount displayAmount = DisplayAmount.TEXT;
    public static boolean inventoryCount = false;
    public static boolean displaySingleCount = true;
    public static EntryBackground entryBackground = EntryBackground.CHAT;
    public static boolean displayItemName = true;

    public static boolean partialPickUps = true;
    public static boolean backpackIntegration = true;

    public static void loadConfig(File file) {
        configFile = file;
        File parent = file.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            VintagePickUpNotifier.LOG.warn("Failed to create config directory {}", parent);
        }

        config = new Configuration(file);

        try {
            VintagePickUpNotifier.debug("Loading config");
            config.load();
            configureCategories();
            loadDebug();
            loadGeneral();
            loadBehavior();
            loadDisplay();
            loadServer();
            hiddenItems = Collections.unmodifiableSet(parseHiddenItems(hiddenItemsRaw));
        } catch (Exception e) {
            VintagePickUpNotifier.LOG.error("Error loading config", e);
        } finally {
            config.save();
        }
    }

    public static Configuration getRawConfig() {
        return config;
    }

    public static File getConfigFile() {
        return configFile;
    }

    public static void save() {
        if (config != null) {
            config.save();
        }
    }

    public static int getMoveTime() {
        return Math.min(moveTime, displayTime);
    }

    public static float getDisplayScale() {
        return displayScale / 6.0F;
    }

    public static boolean isItemHidden(ItemStack stack) {
        return stack != null && stack.getItem() != null && hiddenItems.contains(stack.getItem());
    }

    public static boolean shouldDisplayCount(int count) {
        return count > 1 || count == 1 && displaySingleCount;
    }

    private static void configureCategories() {
        config.setCategoryComment(Categories.DEBUG, "Controls debug logging and tools.");
        config.setCategoryComment(Categories.GENERAL, "Controls what pickups are collected.");
        config.setCategoryComment(Categories.BEHAVIOR, "Controls entry lifetime, merging, and movement.");
        config.setCategoryComment(Categories.DISPLAY, "Controls HUD layout and entry rendering.");
        config.setCategoryComment(Categories.SERVER, "Controls server-assisted pickup tracking.");

        config.setCategoryLanguageKey(Categories.DEBUG, langCategory(Categories.DEBUG));
        config.setCategoryLanguageKey(Categories.GENERAL, langCategory(Categories.GENERAL));
        config.setCategoryLanguageKey(Categories.BEHAVIOR, langCategory(Categories.BEHAVIOR));
        config.setCategoryLanguageKey(Categories.DISPLAY, langCategory(Categories.DISPLAY));
        config.setCategoryLanguageKey(Categories.SERVER, langCategory(Categories.SERVER));
    }

    private static void loadDebug() {
        debugMode = getBoolean(Categories.DEBUG, "debugMode", debugMode, "Enable debug mode.");
    }

    private static void loadGeneral() {
        clientOnly = getBoolean(
            Categories.GENERAL,
            "client_only",
            clientOnly,
            "Force the mod to run client-side only.");
        includeItems = getBoolean(
            Categories.GENERAL,
            "include_items",
            includeItems,
            "Show item entities the player has collected in the pickup notifications.");
        includeExperience = getBoolean(
            Categories.GENERAL,
            "include_experience",
            includeExperience,
            "Show experience orbs the player has collected in the pickup notifications.");
        includeArrows = getBoolean(
            Categories.GENERAL,
            "include_arrows",
            includeArrows,
            "Show shot arrows the player has collected in the pickup notifications.");
        experienceValue = getBoolean(
            Categories.GENERAL,
            "experience_value",
            DEFAULT_EXPERIENCE_VALUE,
            "Show the value of experience points collected instead of the amount of individual orbs.");
        disableInCreative = getBoolean(
            Categories.GENERAL,
            "disable_in_creative",
            disableInCreative,
            "Prevent pickups from being added to the log when in creative mode.");
        hiddenItemsRaw = getStringList(
            Categories.GENERAL,
            "hidden_items",
            hiddenItemsRaw,
            "Disable specific items or whole mod item groups. Use ids like minecraft:stone or modid:*.");
    }

    private static void loadBehavior() {
        combineEntries = getEnum(
            Categories.BEHAVIOR,
            "combine_entries",
            combineEntries,
            "Combine entries of the same type instead of showing each one individually.");
        displayTime = getInt(
            Categories.BEHAVIOR,
            "display_time",
            displayTime,
            0,
            Integer.MAX_VALUE,
            "Amount of ticks each entry will be shown for. Set to 0 to only remove entries when space is needed.");
        moveOut = getEnum(
            Categories.BEHAVIOR,
            "move_out",
            moveOut,
            "Make outdated entries slowly move out of the screen.");
        moveTime = getInt(
            Categories.BEHAVIOR,
            "move_time",
            moveTime,
            0,
            Integer.MAX_VALUE,
            "Amount of ticks it takes for an entry to move out of the screen.");
        fadeOut = getBoolean(
            Categories.BEHAVIOR,
            "fade_out",
            fadeOut,
            "Make outdated entry names slowly fade away instead of simply vanishing.");
    }

    private static void loadDisplay() {
        drawSprite = getBoolean(
            Categories.DISPLAY,
            "draw_sprite",
            drawSprite,
            "Show a small sprite next to the name of each entry showing its contents.");
        textColor = getEnum(Categories.DISPLAY, "default_text_color", textColor, "Color of the entry name text.");
        ignoreRarity = getBoolean(
            Categories.DISPLAY,
            "ignore_rarity",
            ignoreRarity,
            "Ignore rarity when determining item name color.");
        position = getEnum(
            Categories.DISPLAY,
            "screen_corner",
            position,
            "Screen corner for entry list to be drawn in.");
        offsetX = getInt(
            Categories.DISPLAY,
            "offset_x",
            offsetX,
            0,
            Integer.MAX_VALUE,
            "Offset on x-axis from screen border.");
        offsetY = getInt(
            Categories.DISPLAY,
            "offset_y",
            offsetY,
            0,
            Integer.MAX_VALUE,
            "Offset on y-axis from screen border.");
        maxHeight = getDouble(
            Categories.DISPLAY,
            "max_height",
            maxHeight,
            0.0D,
            1.0D,
            "Percentage of relative screen height entries are allowed to fill at max.");
        displayScale = getInt(
            Categories.DISPLAY,
            "display_scale",
            displayScale,
            1,
            24,
            "Scale of entries. A lower scale will make room for more rows to show.");
        displayAmount = getEnum(
            Categories.DISPLAY,
            "display_amount",
            displayAmount,
            "Where and if to display the amount of items picked up.");
        inventoryCount = getBoolean(
            Categories.DISPLAY,
            "inventory_count",
            inventoryCount,
            "Add the total amount of an item in your inventory to the entry.");
        displaySingleCount = getBoolean(
            Categories.DISPLAY,
            "display_single_count",
            displaySingleCount,
            "Show pickups when the picked up amount is just a single item.");
        entryBackground = getEnum(
            Categories.DISPLAY,
            "entry_background",
            entryBackground,
            "Mode for drawing a background behind entries for better visibility.");
        displayItemName = getBoolean(
            Categories.DISPLAY,
            "display_item_name",
            displayItemName,
            "Add the name of the item to the entry.");
    }

    private static void loadServer() {
        partialPickUps = getBoolean(
            Categories.SERVER,
            "partial_pickups",
            partialPickUps,
            "Show item pickups when only part of a ground stack fits in the player's inventory.");
        backpackIntegration = getBoolean(
            Categories.SERVER,
            "backpack_integration",
            backpackIntegration,
            "Show entries for items picked up that do not go to the player's inventory.");
    }

    private static boolean getBoolean(String category, String key, boolean defaultValue, String comment) {
        Property property = config.get(category, key, defaultValue, comment);
        property.setLanguageKey(langProperty(category, key));
        return property.getBoolean(defaultValue);
    }

    private static int getInt(String category, String key, int defaultValue, int minValue, int maxValue,
        String comment) {
        return config.getInt(key, category, defaultValue, minValue, maxValue, comment, langProperty(category, key));
    }

    private static double getDouble(String category, String key, double defaultValue, double minValue, double maxValue,
        String comment) {
        Property property = config.get(category, key, defaultValue, comment, minValue, maxValue);
        property.setLanguageKey(langProperty(category, key));
        double value = property.getDouble(defaultValue);
        return Math.max(minValue, Math.min(maxValue, value));
    }

    private static String[] getStringList(String category, String key, String[] defaultValue, String comment) {
        Property property = config.get(category, key, defaultValue, comment);
        property.setLanguageKey(langProperty(category, key));
        return property.getStringList();
    }

    private static <T extends Enum<T>> T getEnum(String category, String key, T defaultValue, String comment) {
        Property property = config.get(category, key, defaultValue.name(), comment);
        property.setLanguageKey(langProperty(category, key));
        property.setValidValues(enumNames(defaultValue.getDeclaringClass()));
        try {
            return Enum.valueOf(
                defaultValue.getDeclaringClass(),
                property.getString()
                    .trim()
                    .toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            VintagePickUpNotifier.LOG.warn("Invalid config value '{}' for {}.{}", property.getString(), category, key);
            property.set(defaultValue.name());
            return defaultValue;
        }
    }

    private static String[] enumNames(Class<? extends Enum<?>> enumClass) {
        Enum<?>[] constants = enumClass.getEnumConstants();
        String[] names = new String[constants.length];
        for (int i = 0; i < constants.length; i++) {
            names[i] = constants[i].name();
        }
        return names;
    }

    private static Set<Item> parseHiddenItems(String[] entries) {
        Set<Item> parsed = new HashSet<>();
        for (String entry : entries) {
            String source = normalizeEntry(entry);
            if (source.isEmpty()) {
                continue;
            }

            if (source.indexOf('*') >= 0) {
                addWildcardItems(parsed, source);
            } else {
                addExactItem(parsed, source);
            }
        }
        return parsed;
    }

    private static String normalizeEntry(String entry) {
        return entry == null ? "" : entry.trim();
    }

    private static void addExactItem(Set<Item> parsed, String source) {
        Object item = Item.itemRegistry.getObject(source);
        if (item instanceof Item) {
            parsed.add((Item) item);
        } else {
            VintagePickUpNotifier.LOG.warn("Could not find hidden item config entry '{}'", source);
        }
    }

    private static void addWildcardItems(Set<Item> parsed, String source) {
        String[] parts = splitItemId(source);
        String namespace = parts[0];
        Pattern pathPattern = Pattern.compile(toPathPattern(parts[1]));
        int matches = 0;

        Iterator<?> iterator = Item.itemRegistry.iterator();
        while (iterator.hasNext()) {
            Object value = iterator.next();
            if (!(value instanceof Item)) {
                continue;
            }

            Item item = (Item) value;
            String itemName = Item.itemRegistry.getNameForObject(item);
            if (itemName == null) {
                continue;
            }

            String[] itemParts = splitItemId(itemName);
            if (namespace.equals(itemParts[0]) && pathPattern.matcher(itemParts[1])
                .matches()) {
                parsed.add(item);
                matches++;
            }
        }

        if (matches == 0) {
            VintagePickUpNotifier.LOG.warn("Could not match hidden item config entry '{}'", source);
        }
    }

    private static String[] splitItemId(String source) {
        String[] parts = source.split(":", 2);
        if (parts.length == 1) {
            return new String[] { "minecraft", parts[0] };
        }
        return parts;
    }

    private static String toPathPattern(String wildcardPath) {
        StringBuilder pattern = new StringBuilder();
        for (int i = 0; i < wildcardPath.length(); i++) {
            char c = wildcardPath.charAt(i);
            if (c == '*') {
                pattern.append("[a-z0-9_/.-]*");
            } else {
                pattern.append(Pattern.quote(String.valueOf(c)));
            }
        }
        return pattern.toString();
    }

    private static String langCategory(String category) {
        return VintagePickUpNotifier.MODID + ".config.category." + category;
    }

    private static String langProperty(String category, String key) {
        return VintagePickUpNotifier.MODID + ".config." + category + "." + key;
    }
}
