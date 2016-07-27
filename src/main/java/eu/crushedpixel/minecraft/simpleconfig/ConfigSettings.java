/*
 * This file is part of SimpleConfig API, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016 CrushedPixel <https://github.com/CrushedPixel>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package eu.crushedpixel.minecraft.simpleconfig;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * A wrapper around a Configuration object allowing for simple writing and reading of
 * Integer, Double, Boolean and String values in a Configuration File.<br>
 * <br>
 * To create your own ConfigSettings object, simply extend this class, adding fields that extend {@link Setting}. <br>
 * Example code:<br>
 * <pre>
 * <code>public class MyConfigSettings {
 *
 *         public IntegerSetting integerSetting = new IntegerSetting(initialIntValue);
 *         public StringSetting stringSetting = new StringSetting(initialStringValue);
 *
 *         //registering settings without an explicit default value.
 *         //The default values specified by {@link BooleanSetting} and {@link DoubleSetting} will be used.
 *         public BooleanSetting booleanSetting;
 *         public DoubleSetting doubleSetting;
 *
 *         //pass-through constructor
 *         public MyConfigSettings(Configuration config) {
 *             super(config);
 *         }
 * }</code>
 * </pre>
 *
 * When calling {@link Setting#setValue(Object)}, this value
 * will be automatically written to the Configuration file passed in the constructor.<br>
 * <br>
 * <b>Please note</b> that {@link ConfigSettings#init()} has to be called
 * before any of the setting's values can be accessed or modified.<br>
 * This is because possible initial values defined in the {@link Setting} field declarations
 * are not assigned when the constructor is called and would therefore always be overwritten.
 */
public abstract class ConfigSettings {

    private final Configuration config;
    private final String categoryName;

    private Set<Field> settingFields;

    public ConfigSettings(Configuration config) {
        this(config, "config_settings");
    }

    public ConfigSettings(Configuration config, String categoryName) {
        this.config = config;
        this.categoryName = categoryName.toLowerCase().trim().replaceAll("\\s", "_");
    }

    /**
     * Loads the configuration file and assigns it's values to the {@link Setting} fields of this class.<br>
     * If no value is set in the configuration file, the {@link Setting} object's default value will be used.<br>
     * <br>
     * This method has to be called before any of the setting's values can be accessed or modified.
     */
    public void init() {
        this.settingFields = new HashSet<>();

        for(Field f : this.getClass().getDeclaredFields()) {
            if(Setting.class.isAssignableFrom(f.getType())) {
                try {
                    f.setAccessible(true);

                    Setting setting = (Setting)f.get(this);
                    if(setting == null) setting = (Setting)f.getType().newInstance();
                    setting.setParent(this);

                    f.set(this, setting);

                    settingFields.add(f);
                } catch(InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        load();
    }

    private void load() {
        config.load();

        for(Field f : settingFields) {
            if(config.hasKey(categoryName, f.getName())) {
                try {
                    Property p = config.get(categoryName, f.getName(), "");
                    Setting s = (Setting)f.get(this);

                    if(s instanceof StringSetting)
                        s.setValue(p.getString());
                    else if(s instanceof IntegerSetting)
                        s.setValue(p.getInt());
                    else if(s instanceof BooleanSetting)
                        s.setValue(p.getBoolean());
                    else if(s instanceof DoubleSetting)
                        s.setValue(p.getDouble());

                } catch(IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * Writes the {@link Setting} field values to the configuration file.
     */
    public void save() {
        config.removeCategory(config.getCategory(categoryName));

        for(Field f : settingFields) {
            try {
                Setting s = (Setting) f.get(this);
                if(s.getValue() == null) continue;

                if(s instanceof StringSetting)
                    config.get(categoryName, f.getName(), ((StringSetting)s).getValue());
                else if(s instanceof IntegerSetting)
                    config.get(categoryName, f.getName(), ((IntegerSetting)s).getValue());
                else if(s instanceof BooleanSetting)
                    config.get(categoryName, f.getName(), ((BooleanSetting)s).getValue());
                else if(s instanceof DoubleSetting)
                    config.get(categoryName, f.getName(), ((DoubleSetting)s).getValue());

            } catch(IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        config.save();
    }

}
