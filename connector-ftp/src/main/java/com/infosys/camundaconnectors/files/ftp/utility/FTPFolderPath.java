/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp.utility;

import org.apache.commons.net.ftp.FTPFile;

public class FTPFolderPath {
	
	private FTPFile folder;
	private String path;
	private long size;
	
	public void setFTPFile(FTPFile folder) {
		this.folder = folder;
	}
	
	public FTPFile getFTPFile() {
		return this.folder;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return this.path;
	}
	
	public void setSize(long size) {
		this.size = size;
	}
	
	public Long getSize() {
		return this.size;
	}
	
}
