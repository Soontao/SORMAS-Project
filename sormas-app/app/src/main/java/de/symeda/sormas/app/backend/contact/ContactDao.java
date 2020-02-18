/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.symeda.sormas.app.backend.contact;

import android.location.Location;
import androidx.annotation.NonNull;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.app.backend.caze.Case;
import de.symeda.sormas.app.backend.common.AbstractAdoDao;
import de.symeda.sormas.app.backend.common.AbstractDomainObject;
import de.symeda.sormas.app.backend.common.DaoException;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.config.ConfigProvider;
import de.symeda.sormas.app.backend.person.Person;
import de.symeda.sormas.app.backend.task.Task;
import de.symeda.sormas.app.backend.visit.Visit;
import de.symeda.sormas.app.util.LocationService;

/**
 * Created by Stefan Szczesny on 29.11.2016.
 */
public class ContactDao extends AbstractAdoDao<Contact> {

    public ContactDao(Dao<Contact,Long> innerDao) throws SQLException {
        super(innerDao);
    }

    @Override
    protected Class<Contact> getAdoClass() {
        return Contact.class;
    }

    @Override
    public String getTableName() {
        return Contact.TABLE_NAME;
    }

    public List<Contact> getByCase(Case caze) {
        if (caze.isSnapshot()) {
            throw new IllegalArgumentException("Does not support snapshot entities");
        }

        try {
            QueryBuilder qb = queryBuilder();
            qb.where().eq(Contact.CASE_UUID, caze.getUuid())
                    .and().eq(AbstractDomainObject.SNAPSHOT, false);
            qb.orderBy(Contact.LAST_CONTACT_DATE, false);
            return qb.query();
        } catch (SQLException e) {
            Log.e(getTableName(), "Could not perform getByCase on Contact");
            throw new RuntimeException(e);
        }
    }

    public int getCountByPersonAndDisease(@NonNull Person person, Disease disease) {
        if (person.isSnapshot()) {
            throw new IllegalArgumentException("Does not support snapshot entities");
        }

        try {
            QueryBuilder qb = queryBuilder();
            Where where = qb.where();
            where.and(where.eq(Contact.PERSON, person),
                    where.eq(AbstractDomainObject.SNAPSHOT, false));
            if (disease != null) {
                where.and(where, where.eq(Contact.CASE_DISEASE, disease));
            }
            qb.orderBy(Contact.LAST_CONTACT_DATE, false);
            return (int) qb.countOf();
        } catch (SQLException e) {
            Log.e(getTableName(), "Could not perform getCountByPersonAndDisease on Contact");
            throw new RuntimeException(e);
        }
    }

    @Override
    public Contact build() {

        Contact contact = super.build();

        contact.setReportDateTime(new Date());
        contact.setReportingUser(ConfigProvider.getUser());

        return contact;
    }

    // TODO #704
//    @Override
//    public void markAsRead(Contact contact) {
//        super.markAsRead(contact);
//        DatabaseHelper.getPersonDao().markAsRead(contact.getPerson());
//    }

    @Override
    public Contact saveAndSnapshot(final Contact contact) throws DaoException {
        // If a new contact is created, use the last available location to update its report latitude and longitude
        if (contact.getId() == null) {
            Location location = LocationService.instance().getLocation();
            if (location != null) {
                contact.setReportLat(location.getLatitude());
                contact.setReportLon(location.getLongitude());
                contact.setReportLatLonAccuracy(location.getAccuracy());
            }
        }

        return super.saveAndSnapshot(contact);
    }

    public long countByCriteria(ContactCriteria criteria) {
        try {
            return buildQueryBuilder(criteria).countOf();
        } catch (SQLException e) {
            Log.e(getTableName(), "Could not perform countByCriteria on Contact");
            throw new RuntimeException(e);
        }
    }

    public List<Contact> queryByCriteria(ContactCriteria criteria, long offset, long limit) {
        try {
            return buildQueryBuilder(criteria).orderBy(Contact.REPORT_DATE_TIME, true)
                    .offset(offset).limit(limit).query();
        } catch (SQLException e) {
            Log.e(getTableName(), "Could not perform queryByCriteria on Contact");
            throw new RuntimeException(e);
        }
    }

    private QueryBuilder<Contact, Long> buildQueryBuilder(ContactCriteria criteria) throws SQLException {
        QueryBuilder<Contact, Long> queryBuilder = queryBuilder();
        Where<Contact, Long> where = queryBuilder.where().eq(AbstractDomainObject.SNAPSHOT, false);

        if (criteria.getCaze() != null) {
            where.and().eq(Contact.CASE_UUID, criteria.getCaze().getUuid());
        } else {
            if (criteria.getFollowUpStatus() != null) {
                where.and().eq(Contact.FOLLOW_UP_STATUS, criteria.getFollowUpStatus());
            }
        }

        queryBuilder.setWhere(where);
        return queryBuilder;
    }

    public void deleteContactAndAllDependingEntities(String contactUuid) throws SQLException {
        deleteContactAndAllDependingEntities(queryUuidWithEmbedded(contactUuid));
    }

    public void deleteContactAndAllDependingEntities(Contact contact) throws SQLException {
        // Cancel if not in local database
        if (contact == null) {
            return;
        }

        // Delete all visits associated ONLY with this contact
        List<Visit> visits = DatabaseHelper.getVisitDao().getByContact(contact);
        for (Visit visit : visits) {
            if (DatabaseHelper.getContactDao().getCountByPersonAndDisease(visit.getPerson(), visit.getDisease()) <= 1) {
                DatabaseHelper.getVisitDao().deleteCascade(visit);
            }
        }

        // Delete all tasks associated with this contact
        List<Task> tasks = DatabaseHelper.getTaskDao().queryByContact(contact);
        for (Task task : tasks) {
            DatabaseHelper.getTaskDao().deleteCascade(task);
        }

        deleteCascade(contact);
    }

    public int getContactCountByCaseUuid(String caseUuid) {

        try {
            return (int) queryBuilder().where().eq(Contact.CASE_UUID, caseUuid).countOf();
        } catch (SQLException e) {
            Log.e(getTableName(), "Could not perform getContactCountByCaseUuid on Contact");
            throw new RuntimeException(e);
        }
    }
}
