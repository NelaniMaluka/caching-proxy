package org.example.model;

import java.awt.image.BufferedImage;

public class ApiResponse {
    private String text;          // JSON, text, XML, HTML
    private BufferedImage image;  // PNG, JPG, GIF, etc.
    private byte[] binary;        // PDF, ZIP, unknown binary

    public static ApiResponse text(String t) {
        ApiResponse r = new ApiResponse();
        r.text = t;
        return r;
    }

    public static ApiResponse image(BufferedImage img) {
        ApiResponse r = new ApiResponse();
        r.image = img;
        return r;
    }

    public static ApiResponse binary(byte[] b) {
        ApiResponse r = new ApiResponse();
        r.binary = b;
        return r;
    }

    public boolean isText() { return text != null; }
    public boolean isImage() { return image != null; }
    public boolean isBinary() { return binary != null; }

    public String getText() { return text; }
    public BufferedImage getImage() { return image; }
    public byte[] getBinary() { return binary; }
}

