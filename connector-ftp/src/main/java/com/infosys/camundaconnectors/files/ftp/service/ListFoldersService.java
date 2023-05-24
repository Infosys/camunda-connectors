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
import com.infosys.camundaconnectors.files.ftp.utility.FTPFolderPath;
import com.infosys.camundaconnectors.files.ftp.utility.FolderSortByComparator;

public class ListFoldersService implements FTPRequestData {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ListFoldersService.class);
	@NotBlank
	private String folderPath;
	private String folderNamePattern;
	private String modifiedBefore;
	private String modifiedAfter;
	private String searchSubFolders;
	private String maxNumberOfFolders;
	private String maxDepth;		
	@NotBlank
	private String sortBy;  
	@NotBlank
	private String outputType;
	
	private boolean checkValidPath(String folderPath, FTPClient client) throws IOException {
		return client.changeWorkingDirectory(folderPath);
		
	}
	
	public String getFolderPath() {
		return this.folderPath;
	}
	
	public void setFolderPath(String path) {
		this.folderPath = path;
	}
	
	public String getFolderNamePattern() {
		return this.folderNamePattern;
	}
	
	public void setFolderNamePattern(String pattern) {
		this.folderNamePattern = pattern;
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
	
	public String getMaxNumberOfFolders() {
		return this.maxNumberOfFolders;
	}
	
	public void setMaxNumberOfFolders(String maxFolders) {
		this.maxNumberOfFolders = maxFolders;
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
	
	private boolean handlingDate(boolean noBefore, boolean noAfter, FTPFile folder) throws Exception {
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
		    Date lastModified = folder.getTimestamp().getTime();
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
			Date lastModified = folder.getTimestamp().getTime();
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
			Date lastModified = folder.getTimestamp().getTime();
			if(lastModified.compareTo(aDate)>0) {
				return true;
			}
		} else {
			return true;
		}
		return false;
	}
	
	public long findSizeOfFolder(String path, FTPClient client) throws IOException {
		FTPFile[] files = client.listFiles(path);
		long size = (long) 0;
		for(FTPFile file : files) {
			size += file.getSize();
		}
		return size;
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
			List<FTPFolderPath> folderPathList = new ArrayList<>();
			if(!searchSubFolders.equals("true")) {
				for(FTPFile file : files) {
					if(file.isDirectory()) {
						FTPFolderPath fp = new FTPFolderPath();
						fp.setFTPFile(file);
						fp.setPath(folderPath+File.separator+file.getName());
						fp.setSize(findSizeOfFolder(folderPath+File.separator+file.getName(), client));
						folderPathList.add(fp);
					}
				}
			} else {
				Queue<FTPFolderPath> q = new LinkedList<>();
				for(FTPFile file : files) {
					if(file.isDirectory()) {
						FTPFolderPath fp = new FTPFolderPath();
						fp.setFTPFile(file);
						fp.setPath(folderPath+File.separator+file.getName());
						fp.setSize(findSizeOfFolder(folderPath+File.separator+file.getName(), client));
						q.add(fp);
					}
				}
				int depth = 0;				
				while(!q.isEmpty()) {
					int n = q.size();
					for(int i=0;i<n;i++) {
						FTPFolderPath fp = q.poll();
						FTPFile file = fp.getFTPFile();
						String path = fp.getPath();
						if(file.isDirectory()) {
							folderPathList.add(fp);
							FTPFile[] fs = client.listFiles(path);
							for(FTPFile fl : fs) {
								if(file.isDirectory()) {
									FTPFolderPath fp1 = new FTPFolderPath();
									fp1.setFTPFile(fl);
									fp1.setPath(path+File.separator+fl.getName());	
									fp.setSize(findSizeOfFolder(path+File.separator+fl.getName(), client));
									q.add(fp1);
								}						
							}	
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
			List<FTPFolderPath> folderList = new ArrayList<>();
			boolean noPattern = folderNamePattern==null || folderNamePattern.equals("");
			boolean noBefore = modifiedBefore==null || modifiedBefore.equals("");
			boolean noAfter = modifiedAfter==null || modifiedAfter.equals("");		
			if(noPattern && noBefore && noAfter) {
				for(FTPFolderPath fp : folderPathList) {
					folderList.add(fp);
				}
			} else {
				for (FTPFolderPath folder : folderPathList) {
					FTPFile fldr = folder.getFTPFile();
					boolean isSelected = handlingDate(noBefore, noAfter, fldr);
					if(isSelected) {
						if(noPattern || (!noPattern && fldr.getName().matches(folderNamePattern))) {
							folderList.add(folder);
						}
					}
				}
			}
			if(folderList.size()==0) {
				throw new RuntimeException("No files present!!");
			}
			FolderSortByComparator comp = new FolderSortByComparator();
			if(sortBy.equals("name")) {
				Collections.sort(folderList, comp::sortByName);
			} else if(sortBy.equals("size")) {
				Collections.sort(folderList, comp::sortBySize);
			} else {
				Collections.sort(folderList, comp::sortByDate);
			}
			List<String> finalFolderPaths = new ArrayList<>();
			List<Map<String, Object>> finalFolderDetails = new ArrayList<>();
			LOGGER.info(outputType);
			if(outputType.equals("folderPaths")) {
				if(maxNumberOfFolders!=null) {	
					for(int i=0;i<Math.min(Integer.parseInt(maxNumberOfFolders),folderList.size());i++) {
						finalFolderPaths.add(folderList.get(i).getPath());
					}
				} else {
					for(int i=0;i<folderList.size();i++) {
						finalFolderPaths.add(folderList.get(i).getPath());
					}
				}
				String folderPaths = "[\n" + String.join(",\n", finalFolderPaths) + "\n]";
				byte[] bytes = folderPaths.getBytes();
				if (bytes.length > 32766) {
					throw new RuntimeException("Fetch overloaded!!");
				}
				LOGGER.info(folderPaths);
				resp = new FTPResponse<>(folderPaths);
				LOGGER.info("List of Files retrieved: \n" + resp.getResponse());
			} else {
				if(maxNumberOfFolders!=null) {	
					for(int i=0;i<Math.min(Integer.parseInt(maxNumberOfFolders),folderList.size());i++) {
						FTPFolderPath fp = folderList.get(i);
						Map<String, Object> mp = new TreeMap<>();
						mp.put("Name", fp.getFTPFile().getName());
						mp.put("Path", fp.getPath());
						mp.put("Size", fp.getSize());
						mp.put("Date", fp.getFTPFile().getTimestamp().getTime());
						finalFolderDetails.add(mp);
					}
				} else {
					for(int i=0;i<folderList.size();i++) {
						FTPFolderPath fp = folderList.get(i);
						Map<String, Object> mp = new TreeMap<>();
						mp.put("Name", fp.getFTPFile().getName());
						mp.put("Path", fp.getPath());
						mp.put("Size", fp.getSize());
						mp.put("Date", fp.getFTPFile().getTimestamp().getTime());
						finalFolderDetails.add(mp);
					}
				}
				List<String> folderDetailsList = new ArrayList<>();;
				for(int i=0;i<finalFolderDetails.size();i++) {
					Map<String, Object> mp = finalFolderDetails.get(i);
					String rec = mp.toString();
					folderDetailsList.add(rec);
				}
				String folderDetails = "[\n" + String.join(",\n", folderDetailsList) + "\n]";
				byte[] bytes = folderDetails.getBytes();
				if (bytes.length > 32766) {
					throw new RuntimeException("Fetch overloaded!!");
				}
				LOGGER.info(folderDetails);
				resp = new FTPResponse<>(folderDetails);
				LOGGER.info("List of Folders retrieved: \n" + resp.getResponse());
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
	        folderPath,
	        maxDepth,
	        maxNumberOfFolders,
	        modifiedAfter,
	        modifiedBefore,
	        folderNamePattern,
	        outputType,
	        searchSubFolders,
	        sortBy);
	  }

	  @Override
	  public boolean equals(Object obj) {
	    if (this == obj) return true;
	    if (obj == null) return false;
	    if (getClass() != obj.getClass()) return false;
	    ListFoldersService other = (ListFoldersService) obj;
	    return Objects.equals(folderPath, other.folderPath)
	        && Objects.equals(maxDepth, other.maxDepth)
	        && Objects.equals(maxNumberOfFolders, other.maxNumberOfFolders)
	        && Objects.equals(modifiedAfter, other.modifiedAfter)
	        && Objects.equals(modifiedBefore, other.modifiedBefore)
	        && Objects.equals(folderNamePattern, other.folderNamePattern)
	        && Objects.equals(outputType, other.outputType)
	        && Objects.equals(searchSubFolders, other.searchSubFolders)
	        && Objects.equals(sortBy, other.sortBy);
	  }
}
