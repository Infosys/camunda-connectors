/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.mssql.utility;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConstructWhereClause {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConstructWhereClause.class);

  private ConstructWhereClause() {}

  public static String getWhereClause(Map<String, Object> queryFilter) {
    String whereClause = "";
    if (queryFilter != null && !queryFilter.isEmpty()) {
      try {
        Map<String, Object> treeFilterMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        treeFilterMap.putAll(queryFilter);
        if (!treeFilterMap.containsKey("filter")
            && !treeFilterMap.containsKey("logicalOperator")
            && !treeFilterMap.containsKey("filterList"))
          throw new RuntimeException("Map can have keys - filter, logicalOperator and filterList");
        whereClause = " WHERE " + generateSQLWhereClause(treeFilterMap);
      } catch (Exception e) {
        LOGGER.error("Invalid filter: {}", e.getMessage());
        throw new RuntimeException("Invalid filter: " + e.getMessage());
      }
    }
    LOGGER.debug("whereClause: {}", whereClause);
    return whereClause;
  }

  @SuppressWarnings("unchecked")
  private static String generateSQLWhereClause(Map<String, Object> filter) {
    String whereClause = "";
    if (filter.get("filterList") == null
        || ((List<Map<String, Object>>) filter.get("filterList")).isEmpty()) {
      if (!filter.containsKey("filter")) throw new RuntimeException("Map should have key - filter");
      Map<String, Object> filterMap;
      try {
        filterMap = (Map<String, Object>) filter.get("filter");
      } catch (RuntimeException re) {
        throw new RuntimeException(
            "filter should be a map and can have keys - filter, logicalOperator and filterList");
      }
      if (filterMap != null && !filterMap.isEmpty()) {
        Map<String, Object> simpleFilterMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        simpleFilterMap.putAll(filterMap);
        if (!simpleFilterMap.containsKey("colName")
            || !simpleFilterMap.containsKey("operator")
            || !simpleFilterMap.containsKey("value"))
          throw new RuntimeException(simpleFilterMap.toString());
        if (simpleFilterMap.get("colName") == null
            || ((String) simpleFilterMap.get("colName")).isBlank())
          throw new RuntimeException("Column Name can not be null or blank");
        if (simpleFilterMap.get("operator") == null)
          throw new RuntimeException("Operator can't be null");
        // Simple
        final String VALUE_ERROR = "Value can't be null";
        switch (simpleFilterMap.get("operator").toString().toLowerCase()) {
          case "=":
          case "==":
          case "equals":
            whereClause =
                sqlCondition(
                    (String) simpleFilterMap.get("colName"), simpleFilterMap.get("value"), "=");
            break;
          case "<>":
          case "not equals":
          case "notequals":
            whereClause =
                sqlCondition(
                    (String) simpleFilterMap.get("colName"), simpleFilterMap.get("value"), "<>");
            break;
          case "<":
          case "less than":
          case "lessthan":
            whereClause =
                sqlCondition(
                    (String) simpleFilterMap.get("colName"), simpleFilterMap.get("value"), "<");
            break;
          case ">":
          case "greaterthan":
          case "greater than":
            whereClause =
                sqlCondition(
                    (String) simpleFilterMap.get("colName"), simpleFilterMap.get("value"), ">");
            break;
          case "<=":
          case "lessthanorequals":
          case "lessthan or equals":
            whereClause =
                sqlCondition(
                    (String) simpleFilterMap.get("colName"), simpleFilterMap.get("value"), "<=");
            break;
          case ">=":
          case "greaterthanorequals":
          case "greater than or equals":
            whereClause =
                sqlCondition(
                    (String) simpleFilterMap.get("colName"), simpleFilterMap.get("value"), ">=");
            break;
          case "like":
            if (simpleFilterMap.get("value") == null) throw new RuntimeException(VALUE_ERROR);
            whereClause =
                sqlCondition(
                    (String) simpleFilterMap.get("colName"),
                    simpleFilterMap.get("value").toString(),
                    " LIKE ");
            break;
          case "in":
            if (simpleFilterMap.get("value") == null) throw new RuntimeException(VALUE_ERROR);
            whereClause =
                sqlCondition(
                    (String) simpleFilterMap.get("colName"),
                    simpleFilterMap.get("value").toString(),
                    " IN ");
            break;
          case "is":
            whereClause =
                sqlCondition(
                    (String) simpleFilterMap.get("colName"), simpleFilterMap.get("value"), " IS ");
            break;
          case "not in":
          case "notin":
            if (simpleFilterMap.get("value") == null) throw new RuntimeException(VALUE_ERROR);
            whereClause =
                sqlCondition(
                    (String) simpleFilterMap.get("colName"),
                    simpleFilterMap.get("value").toString(),
                    " NOT IN ");
            break;
          case "between":
            if (simpleFilterMap.get("value") == null) throw new RuntimeException(VALUE_ERROR);
            whereClause =
                sqlCondition(
                    (String) simpleFilterMap.get("colName"),
                    simpleFilterMap.get("value").toString(),
                    " BETWEEN ");
            break;
          case "startswith":
          case "starts with":
            if (simpleFilterMap.get("value") == null) throw new RuntimeException(VALUE_ERROR);
            whereClause =
                sqlCondition(
                    (String) simpleFilterMap.get("colName"),
                    simpleFilterMap.get("value").toString() + "%",
                    " LIKE ");
            break;
          case "endswith":
          case "ends with":
            if (simpleFilterMap.get("value") == null) throw new RuntimeException(VALUE_ERROR);
            whereClause =
                sqlCondition(
                    (String) simpleFilterMap.get("colName"),
                    "%" + simpleFilterMap.get("value").toString(),
                    " LIKE ");
            break;
          default:
            throw new RuntimeException("Invalid Operator type");
        }
        if (filter.get("logicalOperator") != null
            && ((String) filter.get("logicalOperator")).equalsIgnoreCase("not")) {
          whereClause = "NOT " + whereClause;
        }
      } else throw new RuntimeException("Map should have key - filter");
    } else {
      // Complex
      if (!filter.containsKey("logicalOperator") && !filter.containsKey("filterList"))
        throw new RuntimeException("Map should have keys - logicalOperator and filterList");
      List<String> partialWhereClause = new ArrayList<>();
      List<Map<String, Object>> filterList = (List<Map<String, Object>>) filter.get("filterList");
      for (Map<String, Object> partialFilter : filterList) {
        // Recursively create child filters
        partialWhereClause.add(generateSQLWhereClause(partialFilter));
      }
      switch (((String) filter.get("logicalOperator")).toLowerCase()) {
        case "and":
          whereClause = "(" + String.join(" AND ", partialWhereClause) + ")";
          break;
        case "or":
          whereClause = "(" + String.join(" OR ", partialWhereClause) + ")";
          break;
        case "not":
        case "nand":
          whereClause = "NOT (" + String.join(" AND ", partialWhereClause) + ")";
          break;
        case "nor":
          whereClause = "NOT (" + String.join(" OR ", partialWhereClause) + ")";
          break;
        default:
          throw new RuntimeException(
              "Invalid Logical operator type, it can be - and, or, nor, nand/not");
      }
    }
    return whereClause;
  }

  private static String sqlCondition(String colName, Object value, String operator) {
    if (value == null) operator = " IS ";
    if (value != null
        && (value instanceof String || value instanceof Date || operator.equals(" LIKE "))) {
      return String.format("%s %s '%s'", colName, operator, value.toString());
    }
    return String.format("%s %s %s", colName, operator, value != null ? value.toString() : null);
  }
}
