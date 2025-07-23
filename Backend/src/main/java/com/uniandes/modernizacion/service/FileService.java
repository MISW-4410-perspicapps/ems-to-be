/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.uniandes.modernizacion.service;

import com.google.cloud.storage.BlobInfo;
import com.uniandes.modernizacion.model.File;
import java.io.IOException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.uniandes.modernizacion.repository.FileRepository;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author Andres Alarcon
 */
@Service
public class FileService {

    private final Storage storage;
    private final String bucketName;
    private final FileRepository fileRepository;

    public FileService(@Value("${gcp.bucket.name}") String bucketName, FileRepository fileRepository) {
        System.out.println("BuekctNmase " + bucketName);
        this.fileRepository = fileRepository;
        this.storage = StorageOptions.getDefaultInstance().getService();
        this.bucketName = bucketName;
    }

    public void uploadFiles(MultipartFile[] files) throws Exception {
        for (MultipartFile multipartFile : files) {           
            if (multipartFile.isEmpty()) {               
                throw new Exception("El archivo está vacio");
            } else {               
                try {
                    File file = new File();
                    System.out.println("Entra#3 " + multipartFile.getOriginalFilename());
                    file.setName(multipartFile.getOriginalFilename());
                    file.setUrl(String.format("https://storage.googleapis.com/%s/%s", bucketName, file.getName()));
                    BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, file.getName()).build();
                    storage.create(blobInfo, multipartFile.getBytes());
                    //fileRepository.save(file);
                } catch (IOException e) {
                    throw new RuntimeException("Error al subir el archivo", e);
                }
            }

        }
    }
}
