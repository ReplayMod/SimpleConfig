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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.lang.reflect.Field;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SimpleConfigTest {

    private static TestConfigSettings configSettings;

    private static File configFile;

    private static Configuration config;

    private static int initialIntValue = 9001;
    private static double initialDoubleValue = 10.31;
    private static String initialStringValue = "?=!ASDF";
    private static boolean initialBooleanValue = true;

    private static int newIntValue = 50;
    private static double newDoubleValue = 80.1234;
    private static String newStringValue = "nEwStRiNgVaLuE";
    private static boolean newBooleanValue = false;

    @BeforeClass
    public static void setup() throws Exception { //can't use @Before here as we only wanna run this once
        configFile = new File("test_config.cfg");

        config = new TestConfiguration(configFile);

        configSettings = new TestConfigSettings(config);
    }

    @Test
    public void a_testInitialization() {
        //we expect an exception to be thrown when a setting's value is being accessed
        //before the ConfigSettings object was initialized
        boolean exception = false;
        try {
            configSettings.integerSetting.getIntValue();
        } catch(IllegalStateException e) {
            exception = true;
        }

        assertTrue(exception);

        //after initializing, no exception should be thrown
        configSettings.init();
        exception = false;

        try {
            configSettings.integerSetting.getIntValue();
        } catch(IllegalStateException e) {
            exception = true;
        }

        assertFalse(exception);
    }

    @Test
    public void b_testInitialValues() {
        assertEquals(initialIntValue, configSettings.integerSetting.getIntValue());
        assertEquals(initialDoubleValue, configSettings.doubleSetting.getDoubleValue(), 0d);
        assertEquals(initialStringValue, configSettings.stringSetting.getStringValue());
        assertEquals(initialBooleanValue, configSettings.booleanSetting.getBooleanValue());
    }

    @Test
    public void c_testValueChange() {
        configSettings.integerSetting.setIntValue(newIntValue);
        configSettings.doubleSetting.setDoubleValue(newDoubleValue);
        configSettings.stringSetting.setStringValue(newStringValue);
        configSettings.booleanSetting.setBooleanValue(newBooleanValue);

        assertEquals(newIntValue, configSettings.integerSetting.getIntValue());
        assertEquals(newDoubleValue, configSettings.doubleSetting.getDoubleValue(), 0d);
        assertEquals(newStringValue, configSettings.stringSetting.getStringValue());
        assertEquals(newBooleanValue, configSettings.booleanSetting.getBooleanValue());
    }

    @AfterClass
    public static void teardown() {
        configFile.delete();
    }

    private static class TestConfigSettings extends ConfigSettings {

        public IntegerSetting integerSetting = new IntegerSetting(initialIntValue);
        public DoubleSetting doubleSetting = new DoubleSetting(initialDoubleValue);
        public StringSetting stringSetting = new StringSetting(initialStringValue);
        public BooleanSetting booleanSetting = new BooleanSetting(initialBooleanValue);

        public TestConfigSettings(Configuration config) {
            super(config);
        }
    }

    /**
     * A Configuration class that does not rely on Minecraft being launched to determine the launch directory
     */
    private static class TestConfiguration extends Configuration {

        public TestConfiguration(File file) throws Exception {
            Field fileField = Configuration.class.getDeclaredField("file");
            fileField.setAccessible(true);
            fileField.set(this, file);
        }
    }

}
