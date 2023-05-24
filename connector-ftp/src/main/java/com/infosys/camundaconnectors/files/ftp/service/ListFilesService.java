/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp.service;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.TreeMap;
import javax.validation.constraints.NotBlank;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.infosys.camundaconnectors.files.ftp.model.request.FTPRequestData;
import com.infosys.camundaconnectors.files.ftp.model.response.FTPResponse;
import com.infosys.camundaconnectors.files.ftp.utility.FTPFilePath;
import com.infosys.camundaconnectors.files.ftp.utility.FileSortByComparator;


public class ListFilesService implements FTPRequestData {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ListFilesService.class);
	@NotBlank
	private String folderPath;
	private String fileNamePattern;
	private String modifiedBefore;
	private String modifiedAfter;
	private String searchSubFolders;
	private String maxNumberOfFiles;
	private String maxDepth;		
	@NotBlank
	private String sortBy;   
	@NotBlank
	private String outputType;
		
	public String getFolderPath() {
		return this.folderPath;
	}
	
	public void setFolderPath(String path) {
		this.folderPath = path;
	}
	
	public String getFileNamePattern() {
		return this.fileNamePattern;
	}
	
	public void setFileNamePattern(String pattern) {
		this.fileNamePattern = pattern;
	}
	
	public String getModifiedBefore() {
		return this.modifiedBefore;
	}
	
	public void setModifiedBefore(String mBefore) {
		this.modifiedBefore = mBefore;
	}
	
	public String getModifiedAfter() {
		return this.modifiedAfter;
	}
	
	public void setModifiedAfter(String mAfter) {
		this.modifiedAfter = mAfter;
	}
	
	public String getSearchSubFolders() {
		return this.searchSubFolders;
	}
	
	public void setSearchSubFolders(String search) {
		this.searchSubFolders = search;
	}
	
	public String getMaxNumberOfFiles() {
		return this.maxNumberOfFiles;
	}
	
	public void setMaxNumberOfFiles(String maxFiles) {
		this.maxNumberOfFiles = maxFiles;
	}
	
	public String getMaxDepth() {
		return this.maxDepth;
	}
	
	public void setMaxDepth(String maxDepth) {
		this.maxDepth = maxDepth;
	}
	
	public String getSortBy() {
		return this.sortBy;
	}
	
	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}
	
	public String getOutputType() {
		return this.outputType;
	}
	
	public void setOutputType(String oType) {
		this.outputType = oType;
	}
	
	private boolean checkValidPath(String folderPath, FTPClient client) throws IOException {
		LOGGER.info(folderPath);
		return client.changeWorkingDirectory(folderPath);
	}
	
	private boolean handlingDate(boolean noBefore, boolean noAfter, FTPFile file) throws Exception {
		Calendar cal;
		if(!noBefore && !noAfter) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			Date bDate, aDate;
			try {
				bDate = dateFormat.parse(modifiedBefore);
			} catch (ParseException e) {
				LOGGER.error("Please enter the modifiedBefore date in right format!!");
				throw new RuntimeException("Please enter the modifiedBefore date in right format!!");
			}
			try {
				aDate = dateFormat.parse(modifiedAfter);
			} catch (ParseException e) {
				LOGGER.error("Please enter the modifiedAfter date in right format!!");
				throw new RuntimeException("Please enter the modifiedAfter date in right format!!");
			}
		    cal = file.getTimestamp();
		    Date lastModified = cal.getTime();
		    if(aDate.compareTo(bDate)<0) {
		    	if(lastModified.compareTo(aDate)>0 && lastModified.compareTo(bDate)<0) {
		    		return true;
		    	}
		    } else {
		    	if(lastModified.compareTo(bDate)<0 || lastModified.compareTo(aDate)>0) {
		    		return true;
		    	}
		    } 
		} else if(!noBefore) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			Date bDate;
			try {
				bDate = dateFormat.parse(modifiedBefore);
			} catch (ParseException e) {
				LOGGER.error("Please enter the modifiedAfter date in right format!!");
				throw new RuntimeException("Please enter the modifiedAfter date in right format!!");
			}
			cal = file.getTimestamp();
			Date lastModified = cal.getTime();
			if(lastModified.compareTo(bDate)<0) {
				return true;
			}
		} else if(!noAfter) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			Date aDate;
			try {
				aDate = dateFormat.parse(modifiedAfter);
			} catch (ParseException e) {
				LOGGER.error("Please enter the modifiedBefore date in right format!!");
				throw new RuntimeException("Please enter the modifiedBefore date in right format!!");
			}
		    cal = file.getTimestamp();
			Date lastModified = cal.getTime();
			if(lastModified.compareTo(aDate)>0) {
				return true;
			}
		} else {
			return true;
		}
		return false;
	}
	
	@Override
	public FTPResponse<String> invoke(FTPClient client) {
	
		FTPResponse<String> resp;
		FTPFile[] files;
		try {
			try {
				if(!checkValidPath(folderPath, client)) {
					throw new RuntimeException("Invalid Path!!");
				}
			} catch(Exception e) {
				LOGGER.error("Error: " + e.getMessage());
				throw new RuntimeException("Error: " + e.getMessage());
			}
			if(!checkValidPath(folderPath, client)) {
				throw new RuntimeException("Invalid Path!!");
			}
			files = client.listFiles(folderPath);
			List<FTPFilePath> filePathList = new ArrayList<>();
			if(!searchSubFolders.equals("true")) {
				for(FTPFile file : files) {
					if(file.isFile()) {
						FTPFilePath fp = new FTPFilePath();
						fp.setFTPFile(file);
						fp.setPath(folderPath+File.separator+file.getName());
						filePathList.add(fp);
					}	
				}
			} else {
				Queue<FTPFilePath> q = new LinkedList<>();
				for(FTPFile file : files) {
					FTPFilePath fp = new FTPFilePath();
					fp.setFTPFile(file);
					fp.setPath(folderPath+File.separator+file.getName());
					q.add(fp);
				}
				int depth = 0;
				while(!q.isEmpty()) {
					int n = q.size();
					for(int i=0;i<n;i++) {
						FTPFilePath fp = q.poll();
						FTPFile file = fp.getFTPFile();
						String path = fp.getPath();
						if(file.isDirectory()) {
							FTPFile[] fs = client.listFiles(path);
							for(FTPFile fl : fs) {
								FTPFilePath fp1 = new FTPFilePath();
								fp1.setFTPFile(fl);
								fp1.setPath(path+File.separator+fl.getName());	
								q.add(fp1);
							}			
						} else {
							filePathList.add(fp);
						}
					}
					if(maxDepth!=null) {
						depth++;
						if(depth==Integer.parseInt(maxDepth)) {
							break;
						}
					}
				}
			}
			List<FTPFilePath> fileList = new ArrayList<>();
			boolean noPattern = fileNamePattern==null || fileNamePattern.equals("");
			boolean noBefore = modifiedBefore==null || modifiedBefore.equals("");
			boolean noAfter = modifiedAfter==null || modifiedAfter.equals("");		
			if(noPattern && noBefore && noAfter) {
				for(FTPFilePath fp : filePathList) {
					fileList.add(fp);
				}
			} else {
				LOGGER.info(fileNamePattern);
				for (FTPFilePath fp : filePathList) {
					FTPFile file = fp.getFTPFile();
					boolean isSelected = handlingDate(noBefore, noAfter, file);
					if(isSelected) {
						if(noPattern || (!noPattern && file.getName().matches(fileNamePattern))) {
							fileList.add(fp);
						}
					}
					
				}
			}
			if(fileList.size()==0) {
				throw new RuntimeException("No files present!!");
			}
			FileSortByComparator comp = new FileSortByComparator();
			if(sortBy.equals("name")) {
				Collections.sort(fileList, comp::sortByName);
			} else if(sortBy.equals("size")) {
				Collections.sort(fileList, comp::sortBySize);
			} else {
				Collections.sort(fileList, comp::sortByDate);
			}
			List<String> finalFilePaths = new ArrayList<>();
			List<Map<String, Object>> finalFileDetails = new ArrayList<>();
			LOGGER.info(outputType);
			if(outputType.equals("filePaths")) {
				if(maxNumberOfFiles!=null) {	
					for(int i=0;i<Math.min(Integer.parseInt(maxNumberOfFiles),fileList.size());i++) {
						finalFilePaths.add(fileList.get(i).getPath());
					}
				} else {
					for(int i=0;i<fileList.size();i++) {
						finalFilePaths.add(fileList.get(i).getPath());
					}
				}
				String filePaths = "[\n" + String.join(",\n", finalFilePaths) + "\n]";
				byte[] bytes = filePaths.getBytes();
				if (bytes.length > 32766) {
					throw new RuntimeException("Fetch overloaded!!");
				}
				LOGGER.info(filePaths);
				resp = new FTPResponse<>(filePaths);
				LOGGER.info("List of Files retrieved: \n" + resp.getResponse());
			} else {
				if(maxNumberOfFiles!=null) {	
					for(int i=0;i<Math.min(Integer.parseInt(maxNumberOfFiles),fileList.size());i++) {
						FTPFilePath fp = fileList.get(i);
						Map<String, Object> mp = new TreeMap<>();
						mp.put("Name", fp.getFTPFile().getName());
						mp.put("Path", fp.getPath());
						mp.put("Size", fp.getFTPFile().getSize());
					    Calendar calObj = fp.getFTPFile().getTimestamp();
						mp.put("Date", calObj.getTime());
						finalFileDetails.add(mp);
					}
				} else {
					for(int i=0;i<fileList.size();i++) {
						FTPFilePath fp = fileList.get(i);
						Map<String, Object> mp = new TreeMap<>();
						mp.put("Name", fp.getFTPFile().getName());
						mp.put("Path", fp.getPath());
						mp.put("Size", fp.getFTPFile().getSize());
						Calendar calObj = fp.getFTPFile().getTimestamp();
						mp.put("Date", calObj.getTime());
						finalFileDetails.add(mp);
					}
				}
				List<String> fileDetailsList = new ArrayList<>();
				for(int i=0;i<finalFileDetails.size();i++) {
					Map<String, Object> mp = finalFileDetails.get(i);
					String rec = mp.toString();
					fileDetailsList.add(rec);
				}
				String fileDetails = "[\n" + String.join(",\n", fileDetailsList) + "\n]";
				byte[] bytes = fileDetails.getBytes();
        		if (bytes.length > 32766) {
          			throw new RuntimeException("Fetch overloaded!!");
        		}
				LOGGER.info(fileDetails);
				resp = new FTPResponse<>(fileDetails);
				LOGGER.info("List of Files retrieved: \n" + resp.getResponse());
			}	
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new RuntimeException("Error: " + e.getMessage());
		} finally {
			try {
				if(client.isConnected()) {
					client.logout();
					client.disconnect();
					LOGGER.debug("Connection closed!!");
				}
			} catch(IOException e) {
				LOGGER.warn("Error while disconnecting from the FTP server!!");
			}
		}
		return resp;
	}

	@Override
	public FTPResponse<String> invoke(FTPClient client1, FTPClient client2) {
		return null;
	}
	
  @Override
  public int hashCode() {
    return Objects.hash(
        fileNamePattern,
        folderPath,
        maxDepth,
        maxNumberOfFiles,
        modifiedAfter,
        modifiedBefore,
        outputType,
        searchSubFolders,
        sortBy);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ListFilesService other = (ListFilesService) obj;
    return Objects.equals(fileNamePattern, other.fileNamePattern)
        && Objects.equals(folderPath, other.folderPath)
        && Objects.equals(maxDepth, other.maxDepth)
        && Objects.equals(maxNumberOfFiles, other.maxNumberOfFiles)
        && Objects.equals(modifiedAfter, other.modifiedAfter)
        && Objects.equals(modifiedBefore, other.modifiedBefore)
        && Objects.equals(outputType, other.outputType)
        && Objects.equals(searchSubFolders, other.searchSubFolders)
        && Objects.equals(sortBy, other.sortBy);
  }


}
