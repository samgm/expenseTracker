package com.antso.expensesmanager.persistence;

import com.antso.expensesmanager.utils.Utils;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by asolano on 09/09/2014.
 */
public enum EntityIdGenerator {
    ENTITY_ID_GENERATOR;

    private boolean withDate;

    private class EntityInfo {
        String prefix;
        long sequence;

        public EntityInfo(String prefix, long sequence) {
            this.prefix = prefix;
            this.sequence = sequence;
        }
    }

    Map<Class, EntityInfo> entities = new HashMap<Class, EntityInfo>();

    public void registerEntity(Class entity, String prefix, long startSequence, boolean withDate) {
        entities.put(entity, new EntityInfo(prefix, startSequence));
        this.withDate = withDate;
    }

    public String createId(Class entity) {
        EntityInfo info = entities.get(entity);
        String id = "";
        if (withDate) {
            id = info.prefix + DateTime.now().toString(Utils.getDatePattenForDB()) + info.sequence;
        } else {
            id = info.prefix + info.sequence;
        }

        info.sequence++;
        entities.put(entity, info);
        return id;
    }
}
