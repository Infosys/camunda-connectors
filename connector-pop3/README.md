# Camunda POP3 Email Connector

Find the user documentation at [POP3-Connector-Documentation](POP3-Connector-Documentation.pdf)

## Build

```bash
mvn clean package
```

## API

### Input

##### Authentication Details

```json
{
  "authentication": {
    "hostname": "HOSTNAME",
    "portNumber": "995",
    "username": "USERNAME",
    "password": "secrets.MY_SECRET",
    "domainName": "DOMAIN_NAME",
    "keyStorePath": "KEYSTORE_PATH",
    "keyStorePassword": "secrets.KEYSTORE_PASSWORD"
  },
  "operation": "",
  "data": {}
}
```

#### Input for Delete Email

```json
{
  "authentication": {},
  "operation": "pop3.delete-email",
  "data": {
    "messageId": "MESSAGE_ID"
  }
}
```

#### Input for Download Email

```json
{
  "authentication": {},
  "operation": "pop3.download-email",
  "data": {
    "messageId": "MESSAGE_ID",
    "downloadFolderPath": "D:/Email/EMLFile"
  }
}
```

#### Input for List Emails

```json
{
  "authentication": {},
  "operation": "pop3.list-emails",
  "data": {
    "filters": {
      "filter": "Subject = 'Test Email'"
    },
    "sortBy": {
      "sortOn": "ReceivedTime",
      "order": "asc"
    },
    "maxResults": 100,
    "outputType": "headerDetails"
  }
}
```

> **maxResults** is the maximum number of records in output.</br>
> **outputType** it can be  "headerDetails" or "messageIds".</br>
> **filters** is a map with keys - filter, filterList and logicalOperator. It will be used to create *SearchTerm* for
> search messages.</br>
> The value for *filter* key is string with space separated - searchField, operator and value. If value is string or
> date, wrap it in single quote.</br>
</br>
> searchField can be - "size","flag","body","from","header","subject","recipient","messageNumber","messageId","
> sentDate"</br>
</br>
> Date String should be in format - "dd/MM/yyyy hh:mm:ss a","dd/MM/yyyy HH:mm:ss","dd/MM/yyyy","E MMM dd HH:mm:ss Z
> yyyy","EEEE MMMM d yyyy","MMMM d yyyy","yyyy-MM-dd HH:mm:ss","yyyy-MM-d HH:mm:ss","yyyy-MM-dd","dd MMM yyyy"</br>
</br>
> For complex filter ( more than one condition ) logicalOperator and filterList must co-exist.</br>

> ***Examples :***</br>
> **1. Simple filter **↴****</br>

```json
{
  "filter": "subject = 'Test Email'"
}
```

> {"filter": "subject = 'Test Email'"} -> *Search for emails containing Test Email as sub-string* <br>
> {"filter", "Flag seen true"} -> *Search for emails with Flag.SEEN marked as true* <br>
> {"filter", "ReceivedTime >= '11/07/2023 12:01:00 AM'"} -> *Search for emails with ReceivedTime greater than equal to
this date* <br>
> {"filter", "ReceivedTime < '14/07/2023 12:01:00 AM'"} -> *Search for emails with ReceivedTime less than this
date* <br>
> {"filter": "body contains 'this string'"} -> *Search for emails whose body contains 'this string'* <br>
> {"filter": "from = 'dev@xyz.com'"} -> *Search for emails with Flag.SEEN marked as true* <br>
> {"filter": "size >= 91801"} -> *Search for emails with size greater than or equal to 91801 bytes* <br>

> **2. Simple filter with negation **↴****</br>

```json
{
  "filter": "recipient cc 'test@xyz.com'",
  "logicalOperator": "NOT"
}
```

> **3. Complex filter **↴****</br>

```json
{
  "logicalOperator": "AND",
  "filterList": [
    {
      "filter": "body contains 'this string'"
    },
    {
      "filter": "from = 'dev@xyz.com'"
    },
    {
      "filter": "size >= 91801"
    }
  ]
}
```

> **sortBy** is a maps with keys - sortOn and order. For sorting the list of messages, based on ( *sortOn* )- size,
> from, subject, message number, messageID, sent date or received date</br>
> ***Example :***</br>

```json
{
  "sortOn": "receivedDate",
  "order": "desc"
}
```

#### Input for Search Emails

```json
{
  "authentication": {},
  "operation": "pop3.search-emails",
  "data": {
    "searchType": "complete",
    "searchField": "subject",
    "searchContent": "Test Email",
    "outputType": "headerDetails"
  }
}
```

> **searchType**   "complete" or "partial".</br>
> **searchField** it can be  "subject", "sender", "from", "body", or "attachment".</br>
> **searchContent** content to match against the search field.</br>
> **outputType** it can be  "headerDetails" or "messageIds".</br>

### Output

```json
{
  "result": {
    "response": "....."
  }
}
```

## Test locally

Run unit tests

```bash
mvn clean verify
```

### Test as local Job Worker

Use
the [Camunda Connector Runtime](https://github.com/camunda-community-hub/spring-zeebe/tree/master/connector-runtime#building-connector-runtime-bundles)
to run your function as a local Job Worker.

See also the [:lock:Camunda Cloud Connector Run-Time](https://github.com/camunda/connector-runtime-cloud)

## Element Template

The element templates can be found in
the [pop3-email-connector.json](element-templates/pop3-email-connector.json) file.
