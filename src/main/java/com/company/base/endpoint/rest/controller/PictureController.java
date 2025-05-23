package com.company.base.endpoint.rest.controller;

import com.company.base.service.PictureService;
import java.io.File;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
public class PictureController {

  private final PictureService pictureService;

  @PostMapping(value = "/picture/{id}")
  public ResponseEntity<Void> uploadImage(
      @PathVariable String id, @RequestBody(required = false) byte[] file) throws IOException {

    pictureService.uploadImage(id, file);
    return ResponseEntity.ok().build();
  }

  @GetMapping("picture/{id}")
  public ResponseEntity<String> getPresignedUrl(@PathVariable String id) {
    String url = pictureService.generatePresignedUrl(id);
    return ResponseEntity.ok(url);
  }

  @GetMapping("/picture/download/{id}")
  public ResponseEntity<Resource> downloadImage(@PathVariable String id) {

    File file = pictureService.downloadImage(id);
    Resource resource = new FileSystemResource(file);

    return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(resource);
  }
}
