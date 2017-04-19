package com.hujiang.juice.common.model;

import lombok.Data;

import java.util.Map;
import java.util.Set;

/**
 * Created by xujia on 17/3/3.
 */
@Data
public class Constraints {

    private String field;
    private Set<String> values;

    public Constraints(String field, Set<String> values) {
        this.field = field;
        this.values = values;
    }

    public enum FIELD {
        RACK_ID("rack_id"),
        HOSTNAME("hostname");

        private String field;

        public String getField() {
            return field;
        }

        FIELD(String field) {
            this.field = field;
        }
    }

    public boolean isAvailable(Map<String, Set<String>> facts) {

        Set<String> fValues = facts.get(field);
        return values.stream().parallel().anyMatch(fValues::contains);
    }
}


