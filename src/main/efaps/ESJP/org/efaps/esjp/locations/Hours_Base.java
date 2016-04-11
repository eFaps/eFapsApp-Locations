/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */


package org.efaps.esjp.locations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.datamodel.ui.IUIValue;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.ci.CIType;
import org.efaps.db.CachedPrintQuery;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.esjp.ci.CILocations;
import org.efaps.esjp.common.uiform.Create;
import org.efaps.esjp.locations.utils.Locations;
import org.efaps.esjp.locations.utils.LocationsSettings;
import org.efaps.esjp.ui.html.HtmlTable;
import org.efaps.util.EFapsException;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("e2a8d6e1-c95b-40dc-99a5-264c5cd39bab")
@EFapsApplication("eFapsApp-Sales")
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

    private static final char TRUECRITERIA = "1".toCharArray()[0];


    public Map<DateTime, Set<HoursInfo>> getDates2Infos(final Parameter _parameter,
                                                                final Instance _parentLocInst,
                                                          final DateTime _startDate,
                                                          final DateTime _endDate)
        throws EFapsException
    {
        final Map<DateTime, Set<HoursInfo>> ret = new TreeMap<DateTime, Set<HoursInfo>>();

        final List<Instance> locInstances = getLocations(_parameter, _parentLocInst);
        locInstances.add(_parentLocInst);

        final List<HoursInfo> generals = getHours(_parameter, locInstances, _startDate, _endDate,
                        CILocations.HoursGeneral);
        DateTime date = _startDate;
        while (!date.isAfter(_endDate)) {
            final Set<HoursInfo> set = new HashSet<HoursInfo>();
            ret.put(date, set);
            for (final HoursInfo info : generals) {
                set.add(info);
            }
            date = date.plusDays(1);
        }

        final List<HoursInfo> specials = getHours(_parameter, locInstances, _startDate, _endDate,
                        CILocations.HoursSpecial);
        DateTime date3 = _startDate;
        while (!date3.isAfter(_endDate)) {
            final Set<HoursInfo> set = ret.get(date3);
            for (final HoursInfo info : specials) {
                if (info.getDate().compareTo(date3.withDayOfWeek(DateTimeConstants.MONDAY)) == 0) {
                    if (set.contains(info)) {
                        set.remove(info);
                    }
                    set.add(info);
                }
            }
            date3 = date3.plusDays(1);
        }
        return ret;
    }


    public Return getHoursFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final DateTime startDate = new DateMidnight().toDateTime().withDayOfWeek(DateTimeConstants.MONDAY);

        final int days = Locations.getSysConfig().getAttributeValueAsInteger(LocationsSettings.DAYSHOURS);
        final DateTime endDate = startDate.plusDays(days > 0 ? days : 45).withDayOfWeek(DateTimeConstants.SUNDAY);

        final Map<DateTime, Set<HoursInfo>> dates = getDates2Infos(_parameter, _parameter.getInstance(), startDate,
                        endDate);

        final DateTimeFormatter formatter = DateTimeFormat.forPattern("EEEE");
        final DateTimeFormatter dateFormatter = DateTimeFormat.shortDate();
        final HtmlTable table = new HtmlTable();
        table.append("<div class=\"hours\">").append(getCss());
        table.table().tr();
        for (final Integer day : Hours_Base.WEEKDAYS) {
            table.th(new DateTime().withDayOfWeek(day).toString(formatter));
        }

        DateTime date2 = startDate;
        while (!date2.isAfter(endDate)) {
            if (date2.getDayOfWeek() == Hours_Base.WEEKDAYS.get(0)) {
                table.trC().tr();
            }
            final StringBuilder inner = new StringBuilder();
            inner.append("<span class=\"date\">").append(date2.toString(dateFormatter)).append("</span>");
            final Set<HoursInfo> set = dates.get(date2);
            for (final HoursInfo info : set) {
                if (info.isActive(date2)) {
                    final PrintQuery print = new CachedPrintQuery(info.getLocInstance(), Locations.CACHKEY);
                    print.addAttribute(CILocations.LocationAbstract.Name);
                    print.execute();
                    inner.append(print.<String>getAttribute(CILocations.LocationAbstract.Name)).append("<br/>");
                }
            }
            table.td(inner.toString());
            date2 = date2.plusDays(1);
        }
        table.trC().tableC();
        table.append("</div>");
        ret.put(ReturnValues.SNIPLETT,  table.toString());
        return ret;
    }

    protected String getCss()
    {
        final StringBuilder ret = new StringBuilder();
        ret.append("<style type=\"text/css\">")
            .append(".hours TD {")
            .append("vertical-align: top;")
            .append("}")
            .append(".hours TH {")
            .append("min-width: 100px;")
            .append("}")
            .append(".date {")
            .append("background-color: lightgrey;")
            .append("display: block;")
            .append("text-align: center;")
            .append("}")
            .append("</style>");
        return ret.toString();
    }


    protected List<Instance> getLocations(final Parameter _parameter,
                                          final Instance _instance)
        throws EFapsException
    {
        final List<Instance> ret = new ArrayList<Instance>();
        final QueryBuilder queryBldr = new QueryBuilder(CILocations.LocationAbstract);
        queryBldr.addWhereAttrEqValue(CILocations.LocationAbstract.ParentLinkAbstract, _instance);
        final InstanceQuery query = queryBldr.getQuery();
        query.execute();
        while (query.next()) {
            ret.add(query.getCurrentValue());
            ret.addAll(getLocations(_parameter, query.getCurrentValue()));
        }
        return ret;
    }


    public List<HoursInfo> getHours(final Parameter _parameter,
                                    final List<Instance> _instances,
                                    final DateTime _startDate,
                                    final DateTime _endDate,
                                    final CIType _ciType)
        throws EFapsException
    {
        final List<HoursInfo> ret = new ArrayList<HoursInfo>();
        final QueryBuilder queryBldr = new QueryBuilder(_ciType);
        if (CILocations.HoursGeneral.equals(_ciType)) {
            queryBldr.addWhereAttrGreaterValue(CILocations.HoursGeneral.ValidUntil, _startDate.minusSeconds(1));
        } else {
            queryBldr.addWhereAttrGreaterValue(CILocations.HoursGeneral.Date, _startDate.minusSeconds(1));
            queryBldr.addWhereAttrLessValue(CILocations.HoursGeneral.Date, _endDate.plusSeconds(1));
        }
        queryBldr.addWhereAttrEqValue(CILocations.HoursAbstract.LocationAbstractLink, _instances.toArray());
        queryBldr.addOrderByAttributeAsc(CILocations.HoursAbstract.Date);

        final MultiPrintQuery multi = queryBldr.getPrint();
        multi.setEnforceSorted(true);
        final SelectBuilder sel = SelectBuilder.get().linkto(CILocations.HoursAbstract.LocationAbstractLink).instance();
        multi.addSelect(sel);
        multi.addAttribute(CILocations.HoursAbstract.Date, CILocations.HoursAbstract.Definition,
                        CILocations.HoursAbstract.FromTime, CILocations.HoursAbstract.ToTime);
        multi.execute();
        while (multi.next()) {
            final HoursInfo info = new HoursInfo();
            ret.add(info);
            info.setInstance(multi.getCurrentInstance());
            info.setLocInstance(multi.<Instance>getSelect(sel));
            info.setDate(multi.<DateTime>getAttribute(CILocations.HoursAbstract.Date));
            info.setDefinition(multi.<Integer>getAttribute(CILocations.HoursAbstract.Definition));
            info.setToTime(multi.<LocalTime>getAttribute(CILocations.HoursAbstract.ToTime));
            info.setFromTime(multi.<LocalTime>getAttribute(CILocations.HoursAbstract.FromTime));
        }
        return ret;
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

    /**
     * Gets the definition field value.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the definition field value
     * @throws EFapsException on error
     */
    public Return getDefinitionFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final IUIValue fieldValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        if (TargetMode.CREATE.equals(_parameter.get(ParameterValues.ACCESSMODE))) {
            ret.put(ReturnValues.SNIPLETT, getCreateHtml4Definitino(_parameter));
        } else if (TargetMode.VIEW.equals(_parameter.get(ParameterValues.ACCESSMODE))
                        || TargetMode.UNKNOWN.equals(_parameter.get(ParameterValues.ACCESSMODE))) {
            final Map<?,?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
            final boolean shortFormat = "SHORT".equalsIgnoreCase((String)properties.get("format"));
            if (shortFormat) {
                ret.put(ReturnValues.SNIPLETT, getShortFormat4Value(_parameter, fieldValue.getObject()));
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


        int i = 0;
        boolean first = true;
        for (final Integer day : Hours_Base.WEEKDAYS) {
            if (Hours_Base.TRUECRITERIA == bitArr[i]) {
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

    /**
     * Gets the integer4 parameter.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the integer4 parameter
     */
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


    public static class HoursInfo
    {
        private DateTime date;

        private LocalTime fromTime;

        private LocalTime toTime;

        private Integer definition;

        private Instance instance;

        private Instance locInstance;

        /**
         * Getter method for the instance variable {@link #date}.
         *
         * @return value of instance variable {@link #date}
         */
        public DateTime getDate()
        {
            return this.date;
        }

        /**
         * @param _date
         */
        public boolean isActive(final DateTime _date)
        {
            boolean ret = false;
            if (CILocations.HoursGeneral.uuid.equals(this.instance.getType().getUUID()) ||
                            this.date.withDayOfWeek(_date.getDayOfWeek()).compareTo(_date) == 0) {
                String bits = Integer.toBinaryString(this.definition);
                bits = StringUtils.leftPad(bits, 7, "0");
                final char[] bitArr = bits.toCharArray();
                int i = 0;
                for (final int day  : Hours_Base.WEEKDAYS) {
                    if (day == _date.getDayOfWeek()) {
                       ret = bitArr[i] == Hours_Base.TRUECRITERIA;
                       break;
                    }
                    i++;
                }
            }
            return ret;
        }

        /**
         * Setter method for instance variable {@link #date}.
         *
         * @param _date value for instance variable {@link #date}
         */
        public void setDate(final DateTime _date)
        {
            this.date = _date;
        }

        /**
         * Getter method for the instance variable {@link #definition}.
         *
         * @return value of instance variable {@link #definition}
         */
        public Integer getDefinition()
        {
            return this.definition;
        }

        /**
         * Setter method for instance variable {@link #definition}.
         *
         * @param _definition value for instance variable {@link #definition}
         */
        public void setDefinition(final Integer _definition)
        {
            this.definition = _definition;
        }

        /**
         * Getter method for the instance variable {@link #instance}.
         *
         * @return value of instance variable {@link #instance}
         */
        public Instance getInstance()
        {
            return this.instance;
        }

        /**
         * Setter method for instance variable {@link #instance}.
         *
         * @param _instance value for instance variable {@link #instance}
         */
        public void setInstance(final Instance _instance)
        {
            this.instance = _instance;
        }

        /**
         * Getter method for the instance variable {@link #locInstance}.
         *
         * @return value of instance variable {@link #locInstance}
         */
        public Instance getLocInstance()
        {
            return this.locInstance;
        }

        /**
         * Setter method for instance variable {@link #locInstance}.
         *
         * @param _locInstance value for instance variable {@link #locInstance}
         */
        public void setLocInstance(final Instance _locInstance)
        {
            this.locInstance = _locInstance;
        }

        /**
         * Getter method for the instance variable {@link #fromTime}.
         *
         * @return value of instance variable {@link #fromTime}
         */
        public LocalTime getFromTime()
        {
            return this.fromTime;
        }

        /**
         * Setter method for instance variable {@link #fromTime}.
         *
         * @param _fromTime value for instance variable {@link #fromTime}
         */
        public void setFromTime(final LocalTime _fromTime)
        {
            this.fromTime = _fromTime;
        }

        /**
         * Getter method for the instance variable {@link #toTime}.
         *
         * @return value of instance variable {@link #toTime}
         */
        public LocalTime getToTime()
        {
            return this.toTime;
        }

        /**
         * Setter method for instance variable {@link #toTime}.
         *
         * @param _toTime value for instance variable {@link #toTime}
         */
        public void setToTime(final LocalTime _toTime)
        {
            this.toTime = _toTime;
        }

        @Override
        public int hashCode()
        {
            return this.locInstance.hashCode();
        }

        @Override
        public boolean equals(final Object _obj)
        {
            final boolean ret;
            if (_obj instanceof HoursInfo) {
                ret = this.locInstance.equals(((HoursInfo) _obj).locInstance);
            } else {
                ret = super.equals(_obj);
            }
            return ret;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}
