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
package net.sf.geonames;

/**
 * Alternate name.
 *
 * @author Moshe Waisberg
 */
public class AlternateName {

    private String name;
    private String languageCode;

    public AlternateName() {
    }

    public AlternateName(String languageCode, String name) {
        setLanguage(languageCode);
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return languageCode;
    }

    public void setLanguage(String languageCode) {
        this.languageCode = languageCode;
    }

    @Override
    public String toString() {
        return languageCode + ": " + name;
    }
}