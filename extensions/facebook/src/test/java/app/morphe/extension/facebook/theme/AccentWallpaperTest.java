/*
 * Copyright 2026 Hushfacebook contributors
 * https://github.com/SysAdminDoc/HushFacebook
 */
package app.morphe.extension.facebook.theme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import app.morphe.extension.facebook.settings.Settings;
import app.morphe.extension.shared.SettingsContextRule;
import app.morphe.extension.shared.settings.HushfacebookPause;
import app.morphe.extension.shared.settings.PauseForTests;

/**
 * The Accent color row's Material You choice: Facebook's blues take the phone's own accent family,
 * read from the wallpaper palette Android 12 and newer publish. It stays out of the Material You
 * theme's way, which already draws the wallpaper's accent, and a paused Facebook keeps its blue.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class AccentWallpaperTest {
    @Rule public final SettingsContextRule settingsContext = new SettingsContextRule();

    @After
    public void restore() {
        PauseForTests.resume();
        Settings.ACCENT_COLOR.resetToDefault();
        AccentColor.forgetWallpaperForTests();
    }

    @Test
    public void facebookBluesTakeThePhonesAccentAtTheirOwnLightness() {
        TonePalette phone = AccentColor.palette(AccentColor.Preset.GREEN);
        AccentColor.useWallpaperForTests(phone);
        Settings.ACCENT_COLOR.save(AccentColor.Preset.WALLPAPER);

        assertEquals(AccentColor.Preset.WALLPAPER, AccentColor.chosen());
        for (int blue : new int[]{0xFF0866FF, 0xFF1D85FC, 0xFF5AA7FF}) {
            int themed = AccentColor.fds(blue, "ACCENT", AccentColor.Preset.WALLPAPER, true, true);
            assertEquals(phone.sameLightness(TonePalette.ACCENT, blue), themed);
            assertEquals(TonePalette.lstar(blue), TonePalette.lstar(themed), 1.0);
        }
    }

    @Test
    public void materialYouKeepsItsWallpaperAccentAndDoesNotOverrideIt() {
        TonePalette phone = AccentColor.palette(AccentColor.Preset.GREEN);
        AccentColor.useWallpaperForTests(phone);
        Settings.ACCENT_COLOR.save(AccentColor.Preset.WALLPAPER);

        assertNull("Material You already draws the wallpaper's accent", AccentColor.pickedForMaterialYou());
        assertSame(phone, AccentColor.onWallpaper(phone, AccentColor.Preset.WALLPAPER));
        assertTrue(AccentColor.wallpaperAvailable());
    }

    @Test
    public void aPausedFacebookKeepsItsBlue() {
        AccentColor.useWallpaperForTests(AccentColor.palette(AccentColor.Preset.GREEN));
        Settings.ACCENT_COLOR.save(AccentColor.Preset.WALLPAPER);
        assertEquals(AccentColor.Preset.WALLPAPER, AccentColor.chosen());
        PauseForTests.pause(HushfacebookPause.Reason.SWITCH);

        assertEquals(0xFF0866FF, AccentColor.fds(0xFF0866FF, Token.ACCENT));
        assertNull(AccentColor.chosen());
    }

    /** Stands in for an FDS token: only the constant name matters. */
    enum Token {
        ACCENT
    }
}
