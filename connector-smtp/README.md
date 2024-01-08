# Camunda SMTP Email Connector

Find the user documentation at [SMTP-Connector-Documentation](SMTP-Connector-Documentation.pdf)

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
    "port": "25",
    "username": "USERNAME",
    "password": "secrets.MY_SECRET"
  },
  "mailBoxName": "mailBoxName",
  "toRecipients": [
    "demo@localhost"
  ],
  "ccRecipients": [
    "demo@localhost"
  ],
  "bccRecipients": [
    "demo@localhost"
  ],
  "subject": "subject",
  "contentType": "text",
  "content": "Hello, world!",
  "attachments": [
    "file://path to attachment file"
  ],
  "importance": true,
  "readReceipt": false,
  "followUp": "today",
  "directReplyTo": [
    "demo@localhost"
  ],
  "sensitivity": "Private"
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
the [smtp-email-connector.json](element-templates/smtp-email-connector.json) file.
