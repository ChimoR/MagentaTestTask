package com.example.AnotherOne;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

import javax.ws.rs.FormParam;

//Container for FormParams
public class FileUploadForm {

    private  byte[] fileData;
    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    @FormParam("directory")
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public byte[] getFileData() {
        return fileData;
    }

    @FormParam("file")
    @PartType("application/octet-stream")
    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }
}
