package com.company.base.service;

import com.company.base.file.bucket.BucketComponent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class PictureService {

  private final BucketComponent bucketComponent;
  private final Path IMAGE_BUCKET_DIRECTORY = Path.of("image/");

  public void uploadImage(String id, byte[] file) throws IOException {
    String fileName = id + ".png";
    String bucketKey = IMAGE_BUCKET_DIRECTORY + fileName;

    File tempFile = File.createTempFile(id, ".png");
    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
      fos.write(file);
    }

    bucketComponent.upload(tempFile, bucketKey);
    boolean deleted = tempFile.delete();
    if (!deleted) {
      throw new RuntimeException("Temporary file could not be deleted");
    }
  }

  public String generatePresignedUrl(String id) {
    String bucketKey = IMAGE_BUCKET_DIRECTORY + id + ".png";
    return bucketComponent.presign(bucketKey, Duration.ofHours(1)).toString();
  }

  public File downloadImage(String id) {
    String bucketKey = IMAGE_BUCKET_DIRECTORY + id + ".png";

    return bucketComponent.download(bucketKey);
  }
}
