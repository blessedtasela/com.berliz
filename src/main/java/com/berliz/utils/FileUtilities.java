package com.berliz.utils;

import com.berliz.constants.BerlizConstants;
import com.berliz.models.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@Service
public class FileUtilities {

    public void saveProfilePhoto(User user, MultipartFile file) throws IOException {
        // Save the image file to a folder on the server
        Path imagePath = Paths.get(BerlizConstants.PROFILE_PICTURE_LOCATION, user.getId()
                + "-" + user.getFirstname() + user.getDate());
        Files.write(imagePath, file.getBytes());
    }

    // compress the image bytes before storing it in the database
    public byte[] compressBytes(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        try {
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
        } catch (IOException e) {
            // Handle IOException appropriately, e.g., log it
            e.printStackTrace();
        }

        System.out.println("Compressed Image Byte Size - " + outputStream.toByteArray().length);

        return outputStream.toByteArray();
    }

    // uncompress the image bytes before returning it to the angular application
    public byte[] decompressBytes(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
        } catch (IOException | DataFormatException e) {
            // Handle exceptions appropriately, e.g., log them
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    public boolean isValidImageType(MultipartFile file) {
        // Implement file type validation logic (e.g., allow only image/jpeg, image/png)
        return true; // Replace with actual validation
    }

    public boolean isValidImageSize(MultipartFile file) {
        return file.getSize() <= 5 * 1024 * 1024; // 5MB
    }
}
