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

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("083ab397-1ddd-40df-b1d9-d78aa0207678")
@EFapsApplication("eFapsApp-Locations")
public interface LocationsSettings
{
    /**
     * Integer.<br/>
     * Number of Days that will be shown in the Hours Field of a Location.
     * Default: 45.
     */
    String DAYSHOURS = "org.efaps.locations.DaysShownInHoursField";

}
