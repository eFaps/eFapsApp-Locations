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


package org.efaps.esjp.locations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.db.Insert;
import org.efaps.esjp.ci.CILocations;
import org.efaps.esjp.common.uiform.Create;
import org.efaps.esjp.ui.html.HtmlTable;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("e2a8d6e1-c95b-40dc-99a5-264c5cd39bab")
@EFapsRevision("$Rev$")
public abstract class Hours_Base
{
    private static final List<Integer> WEEKDAYS = new ArrayList<Integer>();

    static {
        Hours_Base.WEEKDAYS.add(DateTimeConstants.MONDAY);
        Hours_Base.WEEKDAYS.add(DateTimeConstants.TUESDAY);
        Hours_Base.WEEKDAYS.add(DateTimeConstants.WEDNESDAY);
        Hours_Base.WEEKDAYS.add(DateTimeConstants.THURSDAY);
        Hours_Base.WEEKDAYS.add(DateTimeConstants.FRIDAY);
        Hours_Base.WEEKDAYS.add(DateTimeConstants.SATURDAY);
        Hours_Base.WEEKDAYS.add(DateTimeConstants.SUNDAY);
    }

    /**
     * Create a new Hours Object.
     * @param _parameter Parameter as passed by the eFasp API
     * @return empty Return
     * @throws EFapsException on error
     */
    public Return create(final Parameter _parameter)
        throws EFapsException
    {
        final Create create = new Create()
        {

            @Override
            protected void add2basicInsert(final Parameter _parameter,
                                           final Insert _insert)
                throws EFapsException
            {
               _insert.add(CILocations.HoursAbstract.Definition, getInteger4Parameter(_parameter));

            }
        };
        return create.execute(_parameter);
    }

    public Return getDefinitionFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        if (fieldValue.getTargetMode().equals(TargetMode.CREATE)) {
            ret.put(ReturnValues.SNIPLETT, getCreateHtml4Definitino(_parameter));
        } else if (fieldValue.getTargetMode().equals(TargetMode.VIEW)
                        || fieldValue.getTargetMode().equals(TargetMode.UNKNOWN)) {
            final Map<?,?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
            final boolean shortFormat = "SHORT".equalsIgnoreCase((String)properties.get("format"));
            if (shortFormat) {
                ret.put(ReturnValues.SNIPLETT, getShortFormat4Value(_parameter, fieldValue.getValue()));
            }
        }
        return ret;
    }

    /**
     * @param _parameter
     * @param _value
     * @return
     */
    protected String getShortFormat4Value(final Parameter _parameter,
                                          final Object _value)
    {
        final StringBuilder ret = new StringBuilder();
        final DateTimeFormatter formatter = DateTimeFormat.forPattern("E");
        String bits = Integer.toBinaryString((int) _value);
        bits = StringUtils.leftPad(bits, 7, "0");

        final char[] bitArr = bits.toCharArray();

        final char trueCriteria = "1".toCharArray()[0];
        int i = 0;
        boolean first = true;
        for (final Integer day : Hours_Base.WEEKDAYS) {
            if (trueCriteria == bitArr[i]) {
                if (first) {
                    first = false;
                } else {
                    ret.append(", ");
                }
                ret.append(new DateTime().withDayOfWeek(day).toString(formatter));
            }
            i++;
        }
        return ret.toString();
    }




    protected Integer getInteger4Parameter(final Parameter _parameter)
    {
        final StringBuilder bits = new StringBuilder();
        for (final Integer day : Hours_Base.WEEKDAYS) {
            bits.append("true".equalsIgnoreCase(_parameter.getParameterValue("definition" + day)) ? "1" : "0");
        }
        //
        return Integer.parseInt(bits.toString(), 2);
    }


    /**
     * @param _parameter
     * @return
     */
    protected String getCreateHtml4Definitino(final Parameter _parameter)
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        final DateTimeFormatter formatter = DateTimeFormat.forPattern("EEEE");
        final HtmlTable table = new HtmlTable();
        table.table().tr();
        for (final Integer day : Hours_Base.WEEKDAYS) {
            table.th(new DateTime().withDayOfWeek(day).toString(formatter));
        }
        table.trC().tr();
        for (final Integer day : Hours_Base.WEEKDAYS) {
            table.td(getRadioButtons(day));
        }
        table.trC().tableC();
        ret.append(table.toString());
        return ret.toString();
    }

    protected String getRadioButtons(final int _weekday)
    {
        final StringBuilder ret = new StringBuilder();
        ret.append("<input type=\"radio\" name=\"definition").append(_weekday).append("\" value=\"true\">")
            .append(getLabel("active")).append("<br/>")
            .append("<input type=\"radio\" name=\"definition").append(_weekday).append("\" value=\"false\">")
            .append(getLabel("inactive"));
        return ret.toString();
    }

    protected String getLabel(final String _key)
    {
        return DBProperties.getProperty(Hours.class.getName() + "." + _key);
    }
}
