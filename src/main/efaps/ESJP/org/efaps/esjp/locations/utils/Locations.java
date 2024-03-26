/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
package org.efaps.esjp.locations.utils;

import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.api.annotation.EFapsSysConfAttribute;
import org.efaps.api.annotation.EFapsSystemConfiguration;
import org.efaps.esjp.admin.common.systemconfiguration.BooleanSysConfAttribute;
import org.efaps.util.cache.CacheReloadException;

/**
 *
 * @author The eFaps Team
 */
@EFapsUUID("a3f9278e-0fb3-4def-a3fe-035c202148ad")
@EFapsApplication("eFapsApp-Locations")
@EFapsSystemConfiguration("03662d83-8078-4bbf-9516-3980131d5952")
public class Locations
{
    /**
     * Key used for Caching of Queries. Should be rested when something in
     * Contacts is changed.
     */
    public static final String CACHKEY = "org.efaps.esjp.locations.Locations";

    /** The base. */
    public static final String BASE = "org.efaps.locations.";

    /** Locations-Configuration. */
    public static final UUID SYSCONFUUID = UUID.fromString("03662d83-8078-4bbf-9516-3980131d5952");

    /** See description. */
    @EFapsSysConfAttribute
    public static final BooleanSysConfAttribute ACTIVATE = new BooleanSysConfAttribute()
                    .sysConfUUID(Locations.SYSCONFUUID)
                    .key(Locations.BASE + "Activate")
                    .description("Main Locations Activate switch.");

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
        return SystemConfiguration.get(SYSCONFUUID);
    }
}
