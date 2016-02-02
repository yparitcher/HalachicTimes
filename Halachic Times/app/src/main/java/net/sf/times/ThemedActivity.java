/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 *
 * http://sourceforge.net/projects/halachictimes
 *
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 *
 */
package net.sf.times;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import net.sf.times.preference.ZmanimSettings;

/**
 * Activity that applies a custom theme.
 *
 * @author Moshe Waisberg
 */
public class ThemedActivity extends Activity {

    /** The settings and preferences. */
    protected ZmanimSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = this;
        settings = new ZmanimSettings(context);
        setTheme(settings.getTheme());

    }
}