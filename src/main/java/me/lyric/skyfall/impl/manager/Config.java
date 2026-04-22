package me.lyric.skyfall.impl.manager;

import lombok.Getter;
import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.hud.HUDBase;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.Setting;
import me.lyric.skyfall.api.setting.types.*;
import me.lyric.skyfall.api.ui.InventoryButtonInterface;
import me.lyric.skyfall.api.utils.exception.ExceptionHandler;
import me.lyric.skyfall.api.utils.interfaces.Globals;
import me.lyric.skyfall.impl.feature.miscellaneous.SlotBinding;

import java.awt.*;
import java.io.*;
import java.util.*;


@Getter
@SuppressWarnings("ResultOfMethodCallIgnored")
public final class Config implements Globals {
    @Getter
    private final File gameDir = mc.mcDataDir, folder = new File(gameDir + "/Skyfall"), configsFolder = new File(folder + "/configs");
    private String activeConfig = null;


    public Config() {
        folder.mkdir();
        configsFolder.mkdir();
        try {
            File active = new File(folder + "/active.txt");
            active.createNewFile();
            if (active.exists()) {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(active));
                activeConfig = bufferedReader.readLine();
                bufferedReader.close();
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e, this.getClass());
        }
    }

    public void init()
    {
        load(activeConfig, new ArrayList<>(Arrays.asList(Category.values())));
    }

    public void load(String f, ArrayList<Category> categories) {
        try {

            //----loading slot binds-----
            try {
                HashMap<Integer, Integer> loadedMap = new HashMap<>();
                File file = new File(folder + "/slot_bindings.txt");
                if (file.exists()) {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(":");
                        if (parts.length == 2) {
                            loadedMap.put(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                        }
                    }
                    reader.close();
                    Managers.FEATURES.get(SlotBinding.class).slots = loadedMap;
                }
                else {
                    Skyfall.LOGGER.info("--------Registered that there are no slot bindings saved.--------");
                }
            } catch (IOException e) {
                ExceptionHandler.handle(e, this.getClass());
                Skyfall.LOGGER.error("Exception while loading slot bindings.");
            }
            //----loading inventory buttons-----
            try {
                File file = new File(folder + "/inventory_buttons.txt");
                if (file.exists()) {
                    InventoryButtonInterface.buttons = new java.util.HashSet<>();
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(":", 5);
                        if (parts.length >= 5 && parts[0].equals("button")) {
                            int x = Integer.parseInt(parts[1]);
                            int y = Integer.parseInt(parts[2]);
                            String command = parts[3];
                            String item = parts[4];
                            InventoryButtonInterface.buttons.add(
                                new InventoryButtonInterface.InventoryButton(x, y, command, item)
                            );
                        }
                    }
                    reader.close();
                } else {
                    Skyfall.LOGGER.info("--------Registered that there are no inventory buttons saved.--------");
                    InventoryButtonInterface.buttons = new HashSet<>();
                    for (int i = -25; i <= 33; i++) {
                        for (int l = -14; l <= 22; l++) {
                            if (i > -1 && i < 9 && l > -1 && l < 9) continue;
                            InventoryButtonInterface.buttons.add(
                                new InventoryButtonInterface.InventoryButton(20 * i, -6 + l * 20, "", "")
                            );
                        }
                    }
                }
            } catch (IOException e) {
                ExceptionHandler.handle(e, this.getClass());
                Skyfall.LOGGER.error("Exception while loading inventory buttons.");
            }
            //----loading hud elements-----
            for (HUDBase hudModule : Managers.HUD.getHudModules()) {
                File file = new File(configsFolder + "/" + f + "/hud/" + hudModule.getName() + ".txt");
                if (!file.exists()) {
                    continue;
                }
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                bufferedReader.lines().forEach(line -> {
                    String[] split = line.split(":");
                    String name = split[0];
                    if (name.equals("x")) {
                        hudModule.x = Float.parseFloat(split[1]);
                    }
                    if (name.equals("y")) {
                        hudModule.y = Float.parseFloat(split[1]);
                    }
                    Setting<?> setting = getSetting(hudModule, name);
                    if (setting == null) {
                        return;
                    }
                    String value = split[1];
                    if (setting instanceof BindSetting) {
                        ((BindSetting) setting).invokeValue(Integer.parseInt(value));
                        return;
                    }
                    if (setting instanceof BooleanSetting) {
                        if (setting.getName().equals("Enabled") && !hudModule.isTied()) {
                            if (Boolean.parseBoolean(value)) {
                                hudModule.setEnabled(true);
                            }
                        } else {
                            ((BooleanSetting) setting).invokeValue(Boolean.parseBoolean(value));
                        }
                        return;
                    }
                    if (setting instanceof ColourSetting) {
                        int red = Integer.parseInt(split[1]),
                                green = Integer.parseInt(split[2]),
                                blue = Integer.parseInt(split[3]),
                                alpha = Integer.parseInt(split[4]);
                        ((ColourSetting) setting).invokeValue(new Color(red, green, blue, alpha));
                        return;
                    }
                    if (setting instanceof FloatSetting) {
                        ((FloatSetting) setting).invokeValue(Float.parseFloat(value));
                        return;
                    }
                    if (setting instanceof IntegerSetting) {
                        ((IntegerSetting) setting).invokeValue(Integer.parseInt(value));
                        return;
                    }
                    if (setting instanceof ModeSetting) {
                        ((ModeSetting) setting).invokeValue(value);
                    }
                    if (setting instanceof StringSetting) {
                        ((StringSetting) setting).invokeValue(value);
                    }
                });
                bufferedReader.close();
            }
            //----loading features-----
            for (Feature feature : Managers.FEATURES.getFeatures()) {
                if (categories.stream().noneMatch(category -> feature.getCategory().equals(category))) {
                    continue;
                }
                File file = new File(configsFolder + "/" + f + "/" + feature.getCategory() + "/" + feature.getName() + ".txt");
                if (!file.exists()) {
                    continue;
                }
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                bufferedReader.lines().forEach(line -> {
                    String[] split = line.split(":");
                    String name = split[0];
                    Setting<?> setting = getSetting(feature, name);
                    if (setting == null) {
                        return;
                    }
                    String value = split[1];
                    if (setting instanceof BindSetting) {
                        ((BindSetting) setting).invokeValue(Integer.parseInt(value));
                        return;
                    }
                    if (setting instanceof BooleanSetting) {
                        if (setting.getName().equals("Enabled")) {
                            if (Boolean.parseBoolean(value)) {
                                feature.setEnabled(true);
                            }
                        } else {
                            ((BooleanSetting) setting).invokeValue(Boolean.parseBoolean(value));
                        }
                        return;
                    }
                    if (setting instanceof ColourSetting) {
                        int red = Integer.parseInt(split[1]),
                                green = Integer.parseInt(split[2]),
                                blue = Integer.parseInt(split[3]),
                                alpha = Integer.parseInt(split[4]);
                        ((ColourSetting) setting).invokeValue(new Color(red, green, blue, alpha));
                        return;
                    }
                    if (setting instanceof FloatSetting) {
                        ((FloatSetting) setting).invokeValue(Float.parseFloat(value));
                        return;
                    }
                    if (setting instanceof IntegerSetting) {
                        ((IntegerSetting) setting).invokeValue(Integer.parseInt(value));
                        return;
                    }
                    if (setting instanceof ModeSetting) {
                        ((ModeSetting) setting).invokeValue(value);
                    }
                    if (setting instanceof StringSetting)
                    {
                        ((StringSetting) setting).invokeValue(value);
                    }
                    //noinspection StatementWithEmptyBody
                    if (setting instanceof ActionSetting)
                    {
                        //do nothing lol.
                    }
                });
                bufferedReader.close();
            }
            Managers.FEATURES.invalidateDrawableCache();
        } catch (Exception e) {
            ExceptionHandler.handle(e, this.getClass());
            Skyfall.LOGGER.error("Config exception during general loading.");
        }
    }

    public void save(String f, boolean saveActive, ArrayList<Category> categories) {
        try {
            //saving active
            if (saveActive) {
                File active = new File(folder + "/active.txt");
                active.createNewFile();

                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(active));
                bufferedWriter.write(f);
                bufferedWriter.close();
            }
            //saving slot bindings
            try {
                File file = new File(folder + "/slot_bindings.txt");
                file.getParentFile().mkdirs();
                file.createNewFile();

                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                for (Map.Entry<Integer, Integer> entry : Managers.FEATURES.get(SlotBinding.class).slots.entrySet()) {
                    if (entry == null) {
                        continue;
                    }
                    writer.write(entry.getKey() + ":" + entry.getValue());
                    writer.newLine();
                }
                writer.close();
            } catch (IOException e) {
                ExceptionHandler.handle(e, this.getClass());
            }

            //saving categories
            for (Category category : Category.values()) {
                File file = new File(configsFolder + "/" + f + "/" + category.toString());
                file.mkdirs();
            }

            File hud = new File(configsFolder + "/" + f + "/hud/");
            hud.mkdirs();

            for (HUDBase module : Managers.HUD.getHudModules()) {
                File file = new File(configsFolder + "/" + f + "/hud/" + module.getName() + ".txt");
                file.createNewFile();

                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
                bufferedWriter.write("x:" + module.x + "\n");
                bufferedWriter.write("y:" + module.y + "\n");

                for (Setting<?> setting : module.getSettingsHUD()) {
                    if (setting instanceof ActionSetting)
                    {
                        continue;
                    }
                    String value = String.valueOf(setting.getValue());
                    if (setting instanceof ColourSetting) {
                        Color color = ((ColourSetting) setting).getValue();
                        value = color.getRed() + ":" + color.getGreen() + ":" + color.getBlue() + ":" + color.getAlpha();
                    }
                    bufferedWriter.write(setting.getName() + ":" + value + "\n");
                }

                bufferedWriter.close();
            }
            //saving features
            for (Feature feature : Managers.FEATURES.getFeatures()) {
                if (categories.stream().noneMatch(category -> feature.getCategory().equals(category))) {
                    continue;
                }
                File file = new File(configsFolder + "/" + f + "/" + feature.getCategory() + "/" + feature.getName() + ".txt");
                file.createNewFile();

                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));

                for (Setting<?> setting : feature.getSettings()) {
                    if (setting instanceof ActionSetting)
                    {
                        continue;
                    }
                    String value = String.valueOf(setting.getValue());
                    if (setting instanceof ColourSetting) {
                        Color color = ((ColourSetting) setting).getValue();
                        value = color.getRed() + ":" + color.getGreen() + ":" + color.getBlue() + ":" + color.getAlpha();
                    }
                    bufferedWriter.write(setting.getName() + ":" + value + "\n");
                }

                bufferedWriter.close();
            }
            //saving inventory buttons
            try {
                File file = new File(folder + "/inventory_buttons.txt");
                file.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                for (me.lyric.skyfall.api.ui.InventoryButtonInterface.InventoryButton button : me.lyric.skyfall.api.ui.InventoryButtonInterface.buttons) {
                    writer.write("button:" + button.x + ":" + button.y + ":" + button.command + ":" + button.item);
                    writer.newLine();
                }
                writer.close();
            }
            catch (Exception e)
            {
                ExceptionHandler.handle(e, this.getClass());
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e, this.getClass());
        }
        Skyfall.LOGGER.info("Saved config end.");
    }

    private Setting<?> getSetting(Feature feature, String name) {
        return feature.getSettings().stream().filter(setting -> setting.getName().equals(name)).findFirst().orElse(null);
    }

    private Setting<?> getSetting(HUDBase hudModule, String name) {
        return hudModule.getSettingsHUD().stream().filter(setting -> setting.getName().equals(name)).findFirst().orElse(null);
    }
}
