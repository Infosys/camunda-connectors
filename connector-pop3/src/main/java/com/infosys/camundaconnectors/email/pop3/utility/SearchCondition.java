/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.pop3.utility;

import java.text.SimpleDateFormat;
import java.util.*;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This will take Map containing filter condition and create SearchTerm Object for fields size,
 * from, subject, message number, messageID, sentDate
 */
public class SearchCondition {
  private final Logger log = LoggerFactory.getLogger(SearchCondition.class);

  /**
   * @param filterMap Filter Map can have 3 keys filter, logicalOperator, filterList
   * @return SearchTerm object which can have just one condition or multiple conditions aggregated
   *     using AndTerm or OrTerm
   */
  public SearchTerm convertToSearchTerm(Map<String, Object> filterMap) {
    if (filterMap == null || filterMap.isEmpty()) return null;
    SearchTerm filter;
    int type = 0;
    if (filterMap.getOrDefault("logicalOperator", null) != null
        && filterMap.getOrDefault("filterList", null) != null) type = 2;
    else if (filterMap.getOrDefault("filter", null) != null) type = 1;

    switch (type) {
      case 1:
        {
          // single filter
          String filterString = filterMap.get("filter").toString();
          String[] parts = filterString.split("\\s+", 2);
          if (parts.length == 2 && parts[0] != null && parts[1] != null) {
            filter = parseFilterMap(parts[0], parts[1]);
          } else {
            throw new RuntimeException("InvalidFilter" + filterString);
          }
        }
        break;
      case 2:
        {
          // complex filter
          List<SearchTerm> filters = new ArrayList<>();
          @SuppressWarnings("unchecked")
          List<Map<String, Object>> filterList =
              (List<Map<String, Object>>) filterMap.get("filterList");
          for (Map<String, Object> childFilter : filterList) {
            SearchTerm child = convertToSearchTerm(childFilter);
            if (child != null) {
              filters.add(child);
            }
          }
          if (filters.isEmpty()) {
            throw new RuntimeException("InvalidFilter - filterList is empty");
          }
          String logicOperator = filterMap.get("logicalOperator").toString();
          if (logicOperator != null && logicOperator.equalsIgnoreCase("and"))
            filter = new AndTerm(filters.toArray(new SearchTerm[0]));
          else filter = new OrTerm(filters.toArray(new SearchTerm[0]));
        }
        break;
      default:
        throw new RuntimeException(
            "Invalid filter, it should be a map with keys - filter or "
                + "logicalOperator and filterList. Please refer documentation for details");
    }
    if (filter == null) {
      throw new RuntimeException(
          "InvalidFilter: unable to create filter condition. Filter can be applied on - "
              + List.of(
                  "size",
                  "flag",
                  "body",
                  "from",
                  "header",
                  "subject",
                  "recipient",
                  "messageNumber",
                  "messageId",
                  "sentDate"));
    }
    return filter;
  }

  /**
   * @param searchType Search partial or complete
   * @param searchValue Content to search for
   * @return SearchTerm object
   * @throws RuntimeException Exception Message
   */
  private SearchTerm parseFilterMap(String searchType, String searchValue) {
    searchType = searchType.toLowerCase();
    if (searchType.contains("size")) return sizeTerm(searchValue);
    else if (searchType.contains("flag")) return flagTerm(searchValue);
    else if (searchType.contains("body")) return bodyTerm(searchValue);
    else if (searchType.contains("from")) return fromStringTerm(searchValue);
    else if (searchType.contains("header")) return headerTerm(searchValue);
    else if (searchType.contains("subject")) return subjectTerm(searchValue);
    else if (searchType.contains("recipient")) return recipientStringTerm(searchValue);
    else if (searchType.contains("message") && searchType.contains("number"))
      return messageNumberTerm(searchValue);
    else if (searchType.contains("sent")
        && (searchType.contains("date") || searchType.contains("time")))
      return sentDateTerm(searchValue);
    else if (searchType.contains("message") && searchType.contains("id"))
      return messageIDTerm(searchValue);
    else if (searchType.contains("received")
        && (searchType.contains("date") || searchType.contains("time")))
      throw new RuntimeException(
          "Unable to resolve filter. Received date is not supported in POP3, please use IMAP");
    else return null;
  }

  /**
   * @param searchValue messageId, ignore case.
   * @return MessageIDTerm object
   * @throws RuntimeException Exception Message
   */
  private MessageIDTerm messageIDTerm(String searchValue) {
    String[] args = searchValue.split("\\s+", 2);
    if (args.length == 2 && args[0] != null && args[1] != null) {
      return new MessageIDTerm(((String) parseValue(args[1])));
    } else if (args.length == 1 && args[0] != null)
      return new MessageIDTerm(((String) parseValue(args[0])));
    return null;
  }

  /**
   * @param searchValue from field value i.e. email ID of sender, ignore case.
   * @return FromStringTerm object
   * @throws RuntimeException Exception Message
   */
  private FromStringTerm fromStringTerm(String searchValue) {
    String[] args = searchValue.split("\\s+", 2);
    if (args.length == 2 && args[0] != null && args[1] != null) {
      return new FromStringTerm(((String) parseValue(args[1])));
    } else if (args.length == 1 && args[0] != null)
      return new FromStringTerm(((String) parseValue(args[0])));
    return null;
  }

  /**
   * @param searchValue value to search in email subject, ignore case.
   * @return SubjectTerm object
   * @throws RuntimeException Exception Message
   */
  private SubjectTerm subjectTerm(String searchValue) {
    String[] args = searchValue.split("\\s+", 2);
    if (args.length == 2 && args[0] != null && args[1] != null) {
      String input = ((String) parseValue(args[1]));
      return new SubjectTerm(input);
    } else if (args.length == 1 && args[0] != null)
      return new SubjectTerm(((String) parseValue(args[0])));
    return null;
  }

  /**
   * @param searchValue value to search in email body, ignore case.
   * @return BodyTerm object
   * @throws RuntimeException Exception Message
   */
  private BodyTerm bodyTerm(String searchValue) {
    String[] args = searchValue.split("\\s+", 2);
    if (args.length == 2 && args[0] != null && args[1] != null) {
      return new BodyTerm(((String) parseValue(args[1])));
    } else if (args.length == 1 && args[0] != null)
      return new BodyTerm(((String) parseValue(args[0])));
    return null;
  }

  /**
   * @param searchValue value to match against email messageNumber, ignore case.
   * @return MessageNumberTerm
   * @throws RuntimeException Exception Message
   */
  private MessageNumberTerm messageNumberTerm(String searchValue) {
    String[] args = searchValue.split("\\s+", 2);
    if (args.length == 2 && args[0] != null && args[1] != null) {
      return new MessageNumberTerm(((Integer) parseValue(args[1])));
    } else if (args.length == 1 && args[0] != null)
      return new MessageNumberTerm(((Integer) parseValue(args[0])));
    return null;
  }

  /**
   * @param searchValue space separated comparison operator and size value
   * @return SizeTerm object
   * @throws RuntimeException Exception Message
   */
  private SizeTerm sizeTerm(String searchValue) {
    String[] args = searchValue.split("\\s+", 2);
    if (args.length == 2 && args[0] != null && args[1] != null) {
      return new SizeTerm(getComparison(args[0]), ((Integer) parseValue(args[1])));
    }
    return null;
  }

  /**
   * @param searchValue space separated comparison operator and flag value
   * @return FlagTerm object
   * @throws RuntimeException Exception Message
   */
  private FlagTerm flagTerm(String searchValue) {
    String[] args = searchValue.split("\\s+", 2);
    if (args.length == 2 && args[0] != null && args[1] != null) {
      return new FlagTerm(new Flags(getFlag(args[0])), ((Boolean) parseValue(args[1])));
    }
    return null;
  }

  /**
   * @param searchValue space separated Header name and Header value
   * @return HeaderTerm object
   * @throws RuntimeException Exception Message
   */
  private HeaderTerm headerTerm(String searchValue) {
    String[] args = searchValue.split("\\s+", 2);
    List<String> headersList =
        List.of(
            "Return-Path",
            "Date",
            "Message-ID",
            "MIME-Version",
            "X-UserIsAuth",
            "Delivered-To",
            "Received",
            "From",
            "Sender",
            "To",
            "Subject",
            "Content-Type");
    if (args.length == 2 && args[0] != null && args[1] != null) {
      if (!headersList.contains(args[0]))
        throw new RuntimeException(
            ("InvalidHeaderName: " + args[0] + " header can have values - " + headersList));
      return new HeaderTerm(args[0], ((String) parseValue(args[1])));
    }
    return null;
  }

  /**
   * @param searchValue space separated recipient type and recipient email value
   * @return RecipientStringTerm
   * @throws RuntimeException Exception Message
   */
  private RecipientStringTerm recipientStringTerm(String searchValue) {
    String[] args = searchValue.split("\\s+", 2);
    if (args.length == 2 && args[0] != null && args[1] != null) {
      return new RecipientStringTerm(getRecipientType(args[0]), ((String) parseValue(args[1])));
    }
    return null;
  }

  /**
   * @param searchValue space separated comparison operator and sent date value
   * @return SentDateTerm
   * @throws RuntimeException Exception Message
   */
  private SentDateTerm sentDateTerm(String searchValue) {
    String[] args = searchValue.split("\\s+", 2);
    if (args.length == 2 && args[0] != null && args[1] != null) {
      return new SentDateTerm(getComparison(args[0]), getDate(((String) parseValue(args[1]))));
    }
    return null;
  }

  /**
   * @param operator [ LE:1, LT:2, EQ:3, NE = 4, GT = 5, GE = 6]
   * @return Integer value against operator
   * @throws RuntimeException Exception Message
   */
  private int getComparison(String operator) {
    int comparison;
    switch (operator.toLowerCase()) {
      case "<=":
        comparison = 1;
        break;
      case "<":
        comparison = 2;
        break;
      case "=":
      case "like":
      case "equals":
      case "==":
        comparison = 3;
        break;
      case "!=":
      case "<>":
        comparison = 4;
        break;
      case ">":
        comparison = 5;
        break;
      case ">=":
        comparison = 6;
        break;
      default:
        throw new RuntimeException(("InvalidFilterOperator - invalid operator " + operator));
    }
    return comparison;
  }

  /**
   * @param flagVal [answered, deleted, draft, flagged, recent, seen, user]
   * @return Flags.Flag object
   * @throws RuntimeException Exception Message
   */
  private Flags.Flag getFlag(String flagVal) {
    Flags.Flag flag;
    switch (flagVal.toLowerCase()) {
      case "answered":
        flag = Flags.Flag.ANSWERED;
        break;
      case "deleted":
        flag = Flags.Flag.DELETED;
        break;
      case "draft":
        flag = Flags.Flag.DRAFT;
        break;
      case "flagged":
        flag = Flags.Flag.FLAGGED;
        break;
      case "recent":
        flag = Flags.Flag.RECENT;
        break;
      case "seen":
        flag = Flags.Flag.SEEN;
        break;
      case "user":
        flag = Flags.Flag.USER;
        break;
      default:
        throw new RuntimeException(
            ("InvalidFlagValue: "
                + flagVal
                + ", it can have values - answered, "
                + "deleted,"
                + " "
                + "draft, flagged, recent, seen, user"));
    }
    return flag;
  }

  /**
   * @param val Recipient type [to, cc, bcc]
   * @return Message.RecipientType
   * @throws RuntimeException Exception Message
   */
  private Message.RecipientType getRecipientType(String val) {
    Message.RecipientType recipientType;
    switch (val.toLowerCase()) {
      case "bcc":
        recipientType = Message.RecipientType.BCC;
        break;
      case "cc":
        recipientType = Message.RecipientType.CC;
        break;
      case "to":
        recipientType = Message.RecipientType.TO;
        break;
      default:
        throw new RuntimeException(
            ("InvalidMessageRecipientTypeValue: "
                + val
                + ", it can have values - to, "
                + "cc,"
                + " bcc"));
    }
    return recipientType;
  }

  /**
   * @param dateString Date String in format 'dd/MM/yyyy hh:mm:ss a'
   * @return Date object
   */
  private Date getDate(String dateString) {
    Date date = null;
    List<String> formatStrings =
        Arrays.asList(
            "dd/MM/yyyy hh:mm:ss a",
            "dd/MM/yyyy HH:mm:ss",
            "dd/MM/yyyy",
            "E MMM dd HH:mm:ss Z yyyy",
            "EEEE MMMM d yyyy",
            "MMMM d yyyy",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-d HH:mm:ss",
            "yyyy-MM-dd",
            "dd MMM yyyy");
    for (String formatString : formatStrings) {
      try {
        SimpleDateFormat formatter = new SimpleDateFormat(formatString);
        date = formatter.parse(dateString);
      } catch (Exception e) {
        log.error(e.getLocalizedMessage());
      }
      if (date != null) {
        log.info("Parsed date string value: {}", date);
        break;
      }
    }
    if (date == null)
      throw new RuntimeException(
          "Unable to parse date, please provide datetime in format: 'dd/MM/yyyy hh:mm:ss a' E"
              + ".g: '21/07/2022 12:01 AM'");
    return date;
  }

  // Parse Value
  private Object parseValue(String value) {
    if (value == null) return null;
    if (value.matches("'.*'")) return value.substring(1, value.length() - 1);
    else
      log.debug("Value \"{}\" is not considered string as it's not wrapped in single quote", value);
    if (value.equalsIgnoreCase("true")) return true;
    if (value.equalsIgnoreCase("false")) return false;
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      log.debug("Value {} is not integer", value);
    }
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException e) {
      log.debug("Value {} is not double", value);
    }
    try {
      return Float.parseFloat(value);
    } catch (NumberFormatException e) {
      log.debug("Value {} is not float", value);
    }
    throw new RuntimeException("InvalidFilterValue: " + value);
  }
}
