# Camunda MySQL Database Connector

Find the user documentation at [MySQL-Connector-Documentation](MySQL-Connector-Documentation.pdf)

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

#### Input for Create Database

```json
{
  "databaseConnection": {},
  "operation": "mysql.create-database",
  "data": {
    "databaseName": "db_name"
  }
}
```

#### Input for Create Table

```json
{
  "databaseConnection": {},
  "operation": "mysql.create-table",
  "data": {
    "databaseName": "db_name",
    "tableName": "table_name",
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

#### Input for Alter Table

```json
{
  "databaseConnection": {},
  "operation": "mysql.alter-table",
  "data": {
    "databaseName": "db_name",
    "tableName": "table_name",
    "method": "renameTable",
    "newTableName": "rio"
  }
}
```

> **_*NOTE:*_** Please refer [documentation](MySQL-Connector-Documentation.pdf) for other *Method types* ( ***Rename
> column, Add constraint, Drop, Modify Column and Add Column*** ) and
> their respective input parameters.

#### Input for Insert Data

```json
{
  "databaseConnection": {},
  "operation": "mysql.insert-data",
  "data": {
    "databaseName": "db_name",
    "tableName": "table_name",
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
  "operation": "mysql.delete-data",
  "data": {
    "databaseName": "db_name",
    "tableName": "table_name",
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

> **_*Link:*_** Please refer [documentation](MySQL-Connector-Documentation.pdf) **Appendix & FAQ** for more details

#### Input for Read Data

```json
{
  "databaseConnection": {},
  "operation": "mysql.read-data",
  "data": {
    "databaseName": "db_name",
    "tableName": "table_name",
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
  "operation": "mysql.update-data",
  "data": {
    "databaseName": "db_name",
    "tableName": "table_name",
    "updateMap": {
      "city": "Uri",
      "firstname": "Kat"
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
the [mysql-database-connector.json](element-templates/mysql-database-connector.json) file.
