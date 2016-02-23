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
                        ((StringSetting)s).setStringValue(p.getString());
                    else if(s instanceof IntegerSetting)
                        ((IntegerSetting)s).setIntValue(p.getInt());
                    else if(s instanceof BooleanSetting)
                        ((BooleanSetting)s).setBooleanValue(p.getBoolean());
                    else if(s instanceof DoubleSetting)
                        ((DoubleSetting)s).setDoubleValue(p.getDouble());

                } catch(IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void save() {
        config.load();
        config.removeCategory(config.getCategory(categoryName));

        for(Field f : settingFields) {
            try {
                Setting s = (Setting) f.get(this);
                if(s.getValue() == null) continue;

                if(s instanceof StringSetting)
                    config.get(categoryName, f.getName(), ((StringSetting)s).getStringValue());
                else if(s instanceof IntegerSetting)
                    config.get(categoryName, f.getName(), ((IntegerSetting)s).getIntValue());
                else if(s instanceof BooleanSetting)
                    config.get(categoryName, f.getName(), ((BooleanSetting)s).getBooleanValue());
                else if(s instanceof DoubleSetting)
                    config.get(categoryName, f.getName(), ((DoubleSetting)s).getDoubleValue());

            } catch(IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        config.save();
    }

}
