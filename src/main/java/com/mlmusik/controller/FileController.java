package com.mlmusik.controller;

import com.mlmusik.model.Song;
import com.mlmusik.repository.SongRepository;
import com.mlmusik.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Optional;

@RestController
@RequestMapping("/uploads")
public class FileController {

    @Autowired
    private SongService songService;

    @Autowired
    private SongRepository songRepository;

    /**
     * Serve cover art image by path
     * Example: /api/uploads/cover-art/uuid.jpg
     */
    @GetMapping("/cover-art/{filename:.+}")
    public ResponseEntity<Resource> getCoverArt(@PathVariable String filename) {
        try {
            File coverArtFile = new File("./uploads/cover-art/" + filename);
            if (coverArtFile.exists()) {
                Resource resource = new FileSystemResource(coverArtFile);
                String contentType = getContentType(filename);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000")
                        .body(resource);
            }
        } catch (Exception e) {
            // Fall through to not found
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Serve MP3 file by path for playback (streaming)
     * Example: /api/uploads/songs/uuid.mp3
     */
    @GetMapping("/songs/{filename:.+}")
    public ResponseEntity<Resource> getSongFile(@PathVariable String filename) {
        try {
            File songFile = new File("./uploads/songs/" + filename);
            if (songFile.exists()) {
                Resource resource = new FileSystemResource(songFile);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("audio/mpeg"))
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                        .body(resource);
            }
        } catch (Exception e) {
            // Fall through to not found
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Serve cover art image for a song by ID
     */
    @GetMapping("/cover-art/song/{songId}")
    public ResponseEntity<Resource> getCoverArtBySongId(@PathVariable Long songId) {
        Optional<Song> songOpt = songRepository.findById(songId);
        if (songOpt.isPresent()) {
            Song song = songOpt.get();
            if (song.getCoverArtPath() != null) {
                File coverArtFile = new File(song.getCoverArtPath());
                if (coverArtFile.exists()) {
                    Resource resource = new FileSystemResource(coverArtFile);
                    String contentType = getContentType(coverArtFile.getName());
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000")
                            .body(resource);
                }
            }
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Serve MP3 file for playback by song ID (streaming)
     */
    @GetMapping("/songs/song/{songId}")
    public ResponseEntity<Resource> getSongFileBySongId(@PathVariable Long songId) {
        File songFile = songService.getSongFile(songId);
        if (songFile != null && songFile.exists()) {
            Resource resource = new FileSystemResource(songFile);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/mpeg"))
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                    .body(resource);
        }
        return ResponseEntity.notFound().build();
    }

    private String getContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lower.endsWith(".png")) {
            return "image/png";
        } else if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        return "image/jpeg";
    }
}

