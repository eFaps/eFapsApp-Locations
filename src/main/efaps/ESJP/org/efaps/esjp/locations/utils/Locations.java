/*
 * Copyright 2003 - 2013 The eFaps Team
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
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */


package org.efaps.esjp.locations.utils;

import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.cache.CacheReloadException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("a3f9278e-0fb3-4def-a3fe-035c202148ad")
@EFapsRevision("$Rev$")
public class Locations
{
    /**
     * Key used for Caching of Queries. Should be rested when something in
     * Contacts is changed.
     */
    public static final String CACHKEY = "org.efaps.esjp.locations.Locations";
    /**
     * Singelton.
     */
    private Locations()
    {
    }

    /**
     * @return the SystemConfigruation for locations
     * @throws CacheReloadException on error
     */
    public static SystemConfiguration getSysConfig()
        throws CacheReloadException
    {
        // Locations-Configuration
        return SystemConfiguration.get(UUID.fromString("03662d83-8078-4bbf-9516-3980131d5952"));
    }
}
