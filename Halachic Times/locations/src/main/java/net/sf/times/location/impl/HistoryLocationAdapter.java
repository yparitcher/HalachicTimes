/*
 * Copyright 2012, Moshe Waisberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.times.location.impl;

import android.content.Context;

import net.sf.times.location.ZmanimAddress;
import net.sf.times.location.impl.SpecificLocationAdapter;

import java.util.List;

/**
 * Location adapter for locations the user has "previously visited".
 *
 * @author Moshe Waisberg
 */
public class HistoryLocationAdapter extends SpecificLocationAdapter {

    public HistoryLocationAdapter(Context context, List<LocationItem> items) {
        super(context, items);
    }

    @Override
    protected boolean isSpecific(ZmanimAddress address) {
        return address.getId() > 0L;
    }

}