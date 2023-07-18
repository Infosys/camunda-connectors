# Camunda FTP File Connector

Find the user documentation [here](#documentation)

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
        "host": "HostName",
        "port": "21",
        "username": "Username",
        "password": "secrets.PASSWORD"
  },
  "operation": "",
  "data": {}
}
```

#### Input for Copy File

```json
{
     	"authentication": {},
      "operation": "ftp.copy-file",
      "data": {
        "sourceFolderPath": "/Documents/ftproot",
        "sourceFileName": "a.txt",
        "targetFolderPath":"/Documents/ftproot/Dir",
        "createTargetFolder":"True",
        "actionIfFileExists":"rename"
      }
}
```
> **actionIfFileExists** can be "rename","replace" or "skip". If file which is going to be copied in target folder already exists in target folder, then these three operations can be performed.
</br>
>  **createTargetFolder**  is set as a "True" then it will create a "targetDirectory" if not exists.
#### Input for Copy Folder

```json
{
  "authentication": {},
  "operation": "ftp.copy-folder",
  "data": {
    "sourcePath": "C:/Users/user/Documents/Source/demo.txt",
    "targetPath":"C:/Users/user/Documents/Target",
    "actionIfFolderExists":"rename",
    "createTargetFolder": "false"	
  }
}
```

#### Input for Delete File

```json
{
      "authentication": {},
      "operation": "ftp.delete-file",
      "data": {
          "folderPath":"/Documents/ftproot",
          "fileName":"b.txt"
      }
    }
```

#### Input for Delete Folder

```json
{
  "authentication": {},
  "operation": "ftp.delete-folder",
  "data": {
    "parentFolderPath": "C:/Users/user/Documents",
    "folderName": "FolderA"
  }
}
```

#### Input for List Files

```json
{
  "authentication": {},
  "operation": "ftp.list-files",
  "data": {
    "filePath":"C:/Users/user/Documents/Source",
    "fileNamePattern":"demo",
    "modifiedBefore":"31/03/2023",
    "modifiedAfter":"29/03/2023",
    "searchSubFolders":"true",
    "maxNumberOfFiles":"5",
    "maxDepth":"2",
    "outputType":"FilePaths",
    "sortBy":"size"
  }
}
```

> **modifiedBefore** is the date field in the format "dd/MM/yyyy" which will list all the files which are modified before this date.</br>
> **modifiedAfter** is the date field in the format "dd/MM/yyyy" which will list all the files which are modified after this date</br>
> **searchSubFoldersAlso** can be True or False. If "True" it will list all the files in sub folders also. If "false" it will list files in current folder only.</br>
> **maxDepth** indicates a maximum level it will go for listing files.</br>
> **maxNumberOfFiles** is the maximum number of files/folders in output.</br>
> **outputType** it can be  "fileNames" or "fileDetails".</br>
> **sortBy** is a string value on basis of which sorting process occur. It will be used to  *sort* files</br>
> sortOn can be - "size","modifiedDate","name"</br>
#### Input for List Folders

```json
{
  "authentication": {},
  "operation": "ftp.list-folders",
  "data": {
    "folderPath":"C:/Users/user/Documents",
    "folderNamePattern":"demo",
    "modifiedBefore":"01/04/2023",
    "modifiedAfter":"22/03/2023",
    "searchSubFolders":"true",
    "maxNumberOfFiles":"5",
    "maxDepth":"2",
    "outputType":"FilePaths",
    "sortBy":"size"
  }
}
```

#### Input for Create Folder

```json
{
  "authentication": {},
  "operation": "ftp.create-folder",
  "data": {
    "folderPath": "C:/Users/user/Documents",
    "folderName": "MyFolder",
    "actionIfFolderExists":"rename"
  }
}
```
> **actionIfFileExists** or **actionIfFolderExists** can be "rename","replace" or "skip". If folder which is going to be created in target folder already exists in target folder, then these three operations can be performed.
#### Input for Move File

```json
{
  "authentication": {},
  "operation": "ftp.move-file",
  "data": {
     "sourceFolderPath":"C:\\Users\\Documents\\folder1",
     "sourceFileName":"a.txt",
     "targetFolderPath":"C:\\Users\\Documents\\Demo1",
     "actionIfFileExists":"rename",
     "createTargetFolder":"false"
  }
}
```

#### Input for Move Folder

```json
{
  "authentication": {},
  "operation": "ftp.move-folder",
  "data": {
    "sourcePath": "C:/Users/user/Documents/demo1",
    "targetPath": "C:/Users/user/Documents/demoFolder",
    "actionIfFolderExists":"replace",
    "createTargetFolder":"true"
  }
}
```


#### Input for Read File

```json
{
  "authentication": {},
  "operation": "ftp.read-file",
  "data": {
    "folderPath": "C:/Users/user/Documents",
    "fileName": "a.txt"
  }
}
```

#### Input for Write File

```json
{
  "authentication": {},
  "operation": "ftp.write-file",
  "data": {
    "filePath": "C:/Users/user/Documents",
    "fileName": "myFile.txt",
    "content": "Here is the Content!!"
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
the [ftp-file-connector.json](element-templates/ftp-file-connector.json) file.

# **Documentation**

**FTP** – *File Transfer Protocol*. It is an application-layer Internet standard protocol which is used for transferring files between the organizations/computers over a network.
</br>
The FTP Connector can be used for performing various kind of file operations using templates from your BPMN process.


### **Prerequisites**

To start working with the FTP Connector. user need a server details like - host, portNumber, username, password.

*The following parameters are necessary for establishing connection* -

-	**host**- A hostname is a distinct name or label assigned to any device connected to a computer network, in this case its location where server is hosted.
-	**port**: Port Number of FTP server(default is 22)
-	**username** and **password**: Username and password of user with required privilege.

### **Creating FTP connector task**

Currently, the FTP Connector supports eleven types of operations: Copy File, Copy Folder, Create Folder, Delete File, Delete Folder, List Files, List Folders, Move File, Move Folder, Read File and Write File.

To use a FTP Connector in your process, either change the type of existing task by clicking on it and using the wrench-shaped **Change type** context menu icon or create a new Connector task by using the **Append Connector** context menu. Follow our [guide on using Connectors](https://docs.camunda.io/docs/components/connectors/use-connectors/) to learn more.

### **Making FTP Connector executable**

To make the FTP Connector executable, fill out the mandatory fields highlighted in red in the properties panel.

### **Authentication for FTP Connector**

FTP Connector authentication object takes – **host**, **port**, **username** and **password** *(as secrets Token i.e. secrets.Token)*.


## **List Files**

![List Files!](./assets/images/listFiles.png)

> **To List Files, take the following steps:**
1.	In the ***Operation** section*, set the field value *Operation* as **List Files**.
2.	Set the required parameters and credentials in the **Authentication** section.
3.	In the **Input Mapping** section, set the field folderPath, File Name Pattern, modifiedBefore, modifiedAfter, SearchSubFolders, maxNumberOfFiles, MaxDepth, outputType, sortBy.
4. **modifiedBefore** is the date field in the format "dd/MM/yyyy" which will list all the files which are modified before this date.
5. **modifiedAfter** is the date field in the format "dd/MM/yyyy" which will list all the files which are modified after this date
6. **searchSubFoldersAlso** can be True or False. If "True" it will list all the files in sub folders also. If "false" it will list files in current folder only.
7. **maxDepth** indicates a maximum level it will go for listing files.
8. Set [**sortBy**](#what-is-sortby-input-parameter), using dropdown menu which includes dateModified, name, size.
9.	**maxNumberOfFiles** is maximum number of files in output
10.	**outputType** can be selected as **FilePaths** or **FileDetails** as per requirements.
<br>
> **List Files operation response**
You can use an output mapping to map the response:
- Use **Result Variable** to store the response in a process variable. Response is based on what one selects in outputType i.e. FileNames or FileDetails.
</br>
- FilePaths - Paths of files.
  FileDetails - List of maps of file attribute information related to - name, size, parent, path, etc.
## **List Folders**

![List Folders!](./assets/images/listFolders.png "List Folders")

> **To List Folders, take the following steps:**
1.	In the ***Operation** section*, set the field value *Operation* as **List Folders**.
2.	Set the required parameters and credentials in the **Authentication** section.
3.	In the **Input Mapping** section, set the field folderPath, Folder Name Pattern, modifiedBefore, modifiedAfter, SearchSubFolders, maxNumberOfFiles, MaxDepth, outputType, sortBy . You must use FEEL expression for sortBy.
4.  File Path e.g. ```C:/Users/user/Documents/sourceFolder/demo.txt```
5. Set [**sortBy**](#what-is-sortby-input-parameter), using dropdown menu which includes dateModified, name, size.
6.	**maxNumberOfFolders** is maximum number of folders in output
7.	**outputType** can be selected as **FolderPaths** or **FolderDetails** as per requirements.

<br>

> **List Folders operation response**
You can use an output mapping to map the response:
- Use **Result Variable** to store the response in a process variable. Response is based on what one selects in outputType i.e. FolderNames or FolderDetails.
</br>
- FolderPaths - Path of folder.
  FolderDetails - List of maps of folder attribute information related to - name, size, path, etc.
## **Move File**

![Move File!](./assets/images/moveFile.png "Move File")

> **To Move File, take the following steps:**
1.	In the ***Operation** section*, set the field value *Operation* as **Move File**.
2.	Set the required parameters and credentials in the **Authentication** section.
3.	In the **Input Mapping** section, set the field **sourcePath**, **targetPath**, **Action if File Exists**".
4.	Set **sourcePath**, which is basically a path of file which will be moved. (Mention a file name with extension)
5. Set **targetPath**, which is basically a path of folder where file is going to moved.
6. Set "Action If File Exists" as "rename", "replace" or "skip". This operations are performed when the file is already exists in a target folder.
   </br> If **rename** is selected  then it will rename a file.
   </br> If **replace** is selected then it will replace a file.
   </br> if **skip** is selected then it will skip this operation/file.
> **Move File operation response**
You can use an output mapping to map the response:
-	Use **Result Variable** to store the response in a process variable.
## **Move Folder**

![Move Folder!](./assets/images/moveFolder.png "Move Folder")

> **To Move Folder, take the following steps:**
1.	In the ***Operation** section*, set the field value *Operation* as **Move Folder**.
2.	Set the required parameters and credentials in the **Authentication** section.
3.	In the **Input Mapping** section, set the field **sourceDirectory**, **targetPath**, **Action if Folder Exists**".
4.	Set **sourceDirectory**, which is basically a path of folder which will be moved.
5. Set **targetPath**, which is basically a path of folder where folder is going to moved. If it does not exists then connector will create a targetDirectory.
6. Set "Action If Folder Exists" as "rename", "replace" or "skip". This operations are performed when the folder is already exists in a target folder.
   </br> If **rename** is selected  then it will rename a folder.
   </br>  If **replace** is selected then it will replace a folder.
   </br> if **skip** is selected then it will skip this operation/folder.
> **Move File operation response**
You can use an output mapping to map the response:
-	Use **Result Variable** to store the response in a process variable.
## **Copy File**

![Copy File!](./assets/images/copyFile.png "Copy File")

> **To Copy File, take the following steps:**
1. In the ***Operation** section*, set the field value *Operation* as **Copy File**.
2. Set the required parameters and credentials in the **Authentication** section.
3. In the **Input Mapping** section, set the field **sourceFolderPath**, **sourceFileName**, **targetFolderPath**, **actionIfFileExists**, **createTargetFolder**".
4. Set **sourceFolderPath**, which is basically a path of folder of file. 
5. Set **sourceFilePath**, which is the path of file which has to be copied.
6. Set **targetPath**, which is basically a path of folder where file is going to copied.
7. Set "Action If File Exists" as "rename", "replace" or "skip". This operations are performed when the file is already exists in a target folder.
   </br> If **rename** is selected  then it will rename a file.
   </br>  If **replace** is selected then it will replace a file.
   </br> if **skip** is selected then it will skip this operation/file.
> **Copy File operation response**
You can use an output mapping to map the response:
-	Use **Result Variable** to store the response in a process variable.
## **Copy Folder**

![Copy Folder!](./assets/images/copyFolder.png "Copy Folder")

> **To Copy Folder, take the following steps:**
1.	In the ***Operation** section*, set the field value *Operation* as **Copy Folder**.
2.	Set the required parameters and credentials in the **Authentication** section.
3.	In the **Input Mapping** section, set the field **sourcePath**, **targetPath**, **Action if Folder Exists**", **Create Target Folder**.
4.	Set **sourcePath**, which is basically a path of folder which will be copied.
5. Set **targetPath**, which is basically a path of folder where folder is going to copied. If it does not exists then connector will create a targetDirectory.
6. Set "Action If Folder Exists" as "rename", "replace" or "skip". This operations are performed when the folder is already exists in a target folder.
   </br> If **rename** is selected  then it will rename a folder.
   </br>  If **replace** is selected then it will replace a folder.
   </br>  if **skip** is selected then it will skip this operation/folder.
> **Move Folder operation response**
You can use an output mapping to map the response:
-	Use **Result Variable** to store the response in a process variable.
## **Delete File**

![Delete File!](./assets/images/deleteFile.png "Delete File")

> **To Delete File, take the following steps:**
1.	In the ***Operation** section*, set the field value *Operation* as **Delete File**.
2.	Set the required parameters and credentials in the **Authentication** section.
3.	In the **Input Mapping** section, set the field **folderPath**, **fileName**.
4.	Set **folderPath**, which is basically a path of folder where a file resides.
5. Set **fileName**, which is basically a name of file which is going to be deleted.
> **Delete File operation response**
You can use an output mapping to map the response:
-	Use **Result Variable** to store the response in a process variable.

## **Delete Folder**

![Delete Folder!](./assets/images/deleteFolder.png "Delete Folder")

> **To Delete Folder, take the following steps:**
1.	In the ***Operation** section*, set the field value *Operation* as **Delete Folder**.
2.	Set the required parameters and credentials in the **Authentication** section.
3.	In the **Input Mapping** section, set the field **folderPath**, **fileName**.
4.	Set **folderPath**, which is basically a path of folder which is going to get deleted.
5.	Set **fileName**, the name of the file.
> **Delete Folder operation response**
You can use an output mapping to map the response:
-	Use **Result Variable** to store the response in a process variable.

## **Create Folder**

![Create Folder!](./assets/images/createFolder.png "Create Folder")

> **To Create Folder, take the following steps:**
1.	In the ***Operation** section*, set the field value *Operation* as **Create Folder**.
2.	Set the required parameters and credentials in the **Authentication** section.
3.	In the **Input Mapping** section, set the field **parentFolderPath**, **folderName**, **Action If Folder Exists**.
4.	Set **parentFolderPath**, which is basically a path of folder where a folder is going to be created
5. Set **folderName**, which is name of folder which is going to get created in **parentFolderPath**
6. Set **Action If Folder Exists** as "rename", "replace" or "skip". This operations are performed when the folder is already exists in a target folder.
   </br> If **rename** is selected  then it will rename a folder.
   </br> If **replace** is selected then it will replace a folder.
   </br> if **skip** is selected then it will skip this operation/folder.
> **Create Folder operation response**
You can use an output mapping to map the response:
-	Use **Result Variable** to store the response in a process variable.

## **Read File**

![Read File!](./assets/images/readFile.png "Read File")

> **To Read File, take the following steps:**
1.	In the ***Operation** section*, set the field value *Operation* as **Read File**.
2.	Set the required parameters and credentials in the **Authentication** section.
3.	In the **Input Mapping** section, set the field **folderPath**, **fileName**.
4.	Set **folderPath**, which is basically a path of folder of the required file.
5.	Set **fileName**, name of the file from which content has to be read.  It is advisable to use those files which is readable like .txt file.
> **Read File operation response**
You can use an output mapping to map the response:
-	Use **Result Variable** to store the response in a process variable.
## **Write File**

![Write File!](./assets/images/writeFile.png "Write File")

> **To Write File, take the following steps:**
1.	In the ***Operation** section*, set the field value *Operation* as **Write File**.
2.	Set the required parameters and credentials in the **Authentication** section.
3.	In the **Input Mapping** section, set the field **folderPath**, **fileName**, **Content** and **appendContent**.
4.	Set **folderPath**, which is basically a path of folder of the required file.
5.	Set **fileName**, name of the file from which content has to be read.  It is advisable to use those files which is readable like .txt file.
6.  **Content** is the information/content which will be written in a "file".
7.  **appendContent** is contains boolean value which can have "true" or "false" specifying whether the new content should be appended to the old content or override the old content of the file.
> **Write File operation response**
You can use an output mapping to map the response:
-	Use **Result Variable** to store the response in a process variable.


# **Appendix & FAQ**

### **How can I authenticate FTP Connector?**

The FTP Connector needs the credentials for connection -
-	**host**- A hostname is a distinct name or label assigned to any device connected to a computer network, in this case its location server is hosted.
-	**port**: Port Number of server
-	**username** and **password**: Username and password of user with required privilege.

### **What is sortBy input parameter?**

sortBy input is a string which specifies the sorting parameter.

- **sortOn** – It can have values like - size, dateModified and name.
