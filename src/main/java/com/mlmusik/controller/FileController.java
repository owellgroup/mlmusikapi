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
            // Extract just the filename from the path (handle old paths with full directory structure)
            String actualFilename = extractFilename(filename);
            
            // Try multiple possible locations
            String[] possiblePaths = {
                "./uploads/cover-art/" + actualFilename,
                "uploads/cover-art/" + actualFilename,
                actualFilename, // In case it's already a full path
                filename // Original path as-is
            };
            
            for (String path : possiblePaths) {
                File coverArtFile = new File(path);
                if (coverArtFile.exists() && coverArtFile.isFile()) {
                    Resource resource = new FileSystemResource(coverArtFile);
                    String contentType = getContentType(actualFilename);
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000")
                            .body(resource);
                }
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
            // Extract just the filename from the path (handle old paths with full directory structure)
            String actualFilename = extractFilename(filename);
            
            // Try multiple possible locations
            String[] possiblePaths = {
                "./uploads/songs/" + actualFilename,
                "uploads/songs/" + actualFilename,
                actualFilename, // In case it's already a full path
                filename // Original path as-is
            };
            
            for (String path : possiblePaths) {
                File songFile = new File(path);
                if (songFile.exists() && songFile.isFile()) {
                    Resource resource = new FileSystemResource(songFile);
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType("audio/mpeg"))
                            .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                            .body(resource);
                }
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
            if (song.getCoverArtPath() != null && !song.getCoverArtPath().isEmpty()) {
                String coverArtPath = song.getCoverArtPath();
                String filename = extractFilename(coverArtPath);
                
                // Try multiple possible locations
                String[] possiblePaths = {
                    coverArtPath, // Original path as-is
                    "./uploads/cover-art/" + filename,
                    "uploads/cover-art/" + filename,
                    filename
                };
                
                for (String path : possiblePaths) {
                    File coverArtFile = new File(path);
                    if (coverArtFile.exists() && coverArtFile.isFile()) {
                        Resource resource = new FileSystemResource(coverArtFile);
                        String contentType = getContentType(filename);
                        return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType(contentType))
                                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000")
                                .body(resource);
                    }
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

    /**
     * Extract filename from a path, handling both Windows and Unix paths
     */
    private String extractFilename(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        // Handle both forward and backslashes
        String normalized = path.replace("\\", "/");
        int lastSlash = normalized.lastIndexOf("/");
        if (lastSlash >= 0 && lastSlash < normalized.length() - 1) {
            return normalized.substring(lastSlash + 1);
        }
        return path;
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

