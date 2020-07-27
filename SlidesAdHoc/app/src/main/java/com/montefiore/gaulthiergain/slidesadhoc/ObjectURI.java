package com.montefiore.gaulthiergain.slidesadhoc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by gaulthiergain on 3/12/17.
 */

public class ObjectURI implements Serializable {
    private final byte[] inputData;
    private final String extension;
    private String name;

    public ObjectURI() {
        inputData = null;
        extension = null;
        name = null;
    }

    public ObjectURI(String name, InputStream inputStream, String extension) throws IOException {
        this.name = name;
        this.inputData = getBytes(inputStream);
        this.extension = extension;
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public byte[] getInputData() {
        return inputData;
    }

    public String getExtension() {
        return extension;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ObjectURI{" +
                "inputData=" + inputData.length +
                ", extension='" + extension + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
