# Camunda Oracle Database Connector

Find the user documentation at [Oracle-Connector-Documentation](Oracle-Connector-Documentation.pdf)

## Build

```bash
mvn clean package
```

## API

### Input

##### Database Connection Details

```json
{
  "databaseConnection": {
    "host": "HOSTNAME",
    "port": "PORT_NUMBER",
    "username": "USERNAME",
    "password": "secrets.MY_SECRET"
  },
  "operation": "",
  "data": {}
}
```

### Data

#### Input for Alter Table

```json
{
  "databaseConnection": {},
  "operation": "oracledb.alter-table",
  "data": {
    "databaseName": "XE",
    "tableName": "rio",
    "method": "renameTable",
    "newTableName": "testRio"
  }
}
```

> **_*NOTE:*_** Please refer [documentation](Oracle-Connector-Documentation.pdf) for other *Method types*  (
> ***Rename column, Add constraint, Drop constraint, Drop Column, Modify Column, Add Column, Enable constraint and
> Disable constraint*** ) and
> their respective input parameters.

#### Input for Create Table

```json
{
  "databaseConnection": {},
  "operation": "oracledb.create-table",
  "data": {
    "databaseName": "XE",
    "tableName": "testRio",
    "columnsList": [
      {
        "constraints": [
          "NOT NULL",
          "PRIMARY KEY"
        ],
        "dataType": "int",
        "colName": "empid"
      },
      {
        "Datatype": "varchar(50)",
        "ColName": "empName"
      },
      {
        "datatype": "int",
        "colname": "empNumber"
      }
    ]
  }
}
```

#### Input for Insert Data

```json
{
  "databaseConnection": {},
  "operation": "oracledb.insert-data",
  "data": {
    "databaseName": "XE",
    "tableName": "testRio",
    "dataToInsert": [
      {
        "PersonID": 1,
        "FirstName": "Alpha"
      }
    ]
  }
}
```

#### Input for Delete Data

```json
{
  "databaseConnection": {},
  "operation": "oracledb.delete-data",
  "data": {
    "databaseName": "XE",
    "tableName": "testRio",
    "filters": {
      "filter": {
        "colName": "firstname",
        "operator": "=",
        "value": "Xon"
      }
    },
    "orderBy": [
      {
        "sortOn": "firstname",
        "order": "desc"
      }
    ],
    "limit": 1
  }
}
```

> **filters** is a map with keys - filter, filterList and logicalOperator. It will be used to create where clause.</br>
> The value for filter key is a map with keys - colName, operator and value.</br>
> For complex filter ( more than one condition ) logicalOperator and filterList must co-exist.</br>

> ***Examples :***</br>
> **1. Simple filter **↴****</br>

```json
{
  "filter": {
    "colName": "alias",
    "operator": "like",
    "value": "%superman%"
  }
}
```

> **2. Simple filter with negation **↴****</br>

```json
{
  "filter": {
    "colName": "alias",
    "operator": "like",
    "value": "%superman%"
  },
  "logicalOperator": "NOT"
}
```

> **3. Complex filter **↴****</br>

```json
{
  "logicalOperator": "AND",
  "filterList": [
    {
      "filter": {
        "colName": "empAddress",
        "operator": "=",
        "value": "Krypton"
      }
    },
    {
      "filter": {
        "colName": "empName",
        "operator": "like",
        "value": "%superman%"
      }
    },
    {
      "filter": {
        "colName": "age",
        "operator": ">",
        "value": 28
      }
    }
  ]
}
```

<br>

> **OrderBy** is a list of maps with keys - sortOn and order. It will be used to create orderBy clause for SQL
> query.</br>
> ***Example :***</br>

```json
[
  {
    "sortOn": "columnName1",
    "order": "desc"
  },
  {
    "sortOn": "columnName2",
    "order": "asc"
  }
]
```

> **_*Link:*_** Please refer [documentation](Oracle-Connector-Documentation.pdf) **Appendix & FAQ** for more details

#### Input for  Read Data

```json
{
  "databaseConnection": {},
  "operation": "oracledb.read-data",
  "data": {
    "databaseName": "XE",
    "tableName": "testRio",
    "columnNames": [
      "firstname"
    ],
    "filters": {
      "logicalOperator": "not",
      "filter": {
        "colName": "firstname",
        "operator": "=",
        "value": "Xon"
      }
    },
    "orderBy": [
      {
        "sortOn": "firstname",
        "order": "desc"
      }
    ],
    "limit": 1
  }
}
```

#### Input for Update Data

```json
{
  "databaseConnection": {},
  "operation": "oracledb.update-data",
  "data": {
    "databaseName": "XE",
    "tableName": "testRio",
    "updateMap": {
      "city": "Uri",
      "firstname": "Alex"
    },
    "filters": {
      "logicalOperator": "not",
      "filter": {
        "colName": "firstname",
        "operator": "=",
        "value": "Xon"
      }
    },
    "orderBy": [
      {
        "sortOn": "firstname",
        "order": "desc"
      }
    ],
    "limit": 1
  }
}
```

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
the [oracle-database-connector.json](element-templates/oracle-database-connector.json) file.
