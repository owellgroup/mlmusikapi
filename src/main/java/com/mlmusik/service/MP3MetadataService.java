package com.mlmusik.service;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class MP3MetadataService {

    /**
     * Embeds cover art into MP3 file metadata
     * @param mp3FilePath Path to the MP3 file
     * @param coverArtFilePath Path to the cover art image file
     * @throws Exception If embedding fails
     */
    public void embedCoverArt(String mp3FilePath, String coverArtFilePath) throws Exception {
        try {
            File mp3File = new File(mp3FilePath);
            File coverArtFile = new File(coverArtFilePath);

            if (!mp3File.exists()) {
                throw new IOException("MP3 file not found: " + mp3FilePath);
            }
            if (!coverArtFile.exists()) {
                throw new IOException("Cover art file not found: " + coverArtFilePath);
            }

            // Read MP3 file
            Mp3File mp3 = new Mp3File(mp3FilePath);
            
            // Get or create ID3v2 tag
            ID3v2 id3v2Tag;
            if (mp3.hasId3v2Tag()) {
                id3v2Tag = mp3.getId3v2Tag();
            } else {
                id3v2Tag = new ID3v24Tag();
                mp3.setId3v2Tag(id3v2Tag);
            }

            // Read cover art image
            byte[] imageData = Files.readAllBytes(coverArtFile.toPath());
            
            // Set album art (APIC frame)
            id3v2Tag.setAlbumImage(imageData, getMimeType(coverArtFilePath));
            
            // Save the MP3 file with updated tag
            mp3.save(mp3FilePath + ".tmp");
            new File(mp3FilePath).delete();
            new File(mp3FilePath + ".tmp").renameTo(new File(mp3FilePath));
            
        } catch (Exception e) {
            throw new Exception("Failed to embed cover art into MP3: " + e.getMessage(), e);
        }
    }

    /**
     * Sets MP3 metadata (title, artist, featured artists, producer, track number)
     * @param mp3FilePath Path to the MP3 file
     * @param title Song title
     * @param artist Song artist
     * @param featuredArtists Featured artists
     * @param producer Producer
     * @param trackNumber Track number
     * @throws Exception If setting metadata fails
     */
    public void setMetadata(String mp3FilePath, String title, String artist, 
                           String featuredArtists, String producer, Integer trackNumber) throws Exception {
        try {
            File mp3File = new File(mp3FilePath);
            if (!mp3File.exists()) {
                throw new IOException("MP3 file not found: " + mp3FilePath);
            }

            // Read MP3 file
            Mp3File mp3 = new Mp3File(mp3FilePath);
            
            // Get or create ID3v2 tag
            ID3v2 id3v2Tag;
            if (mp3.hasId3v2Tag()) {
                id3v2Tag = mp3.getId3v2Tag();
            } else {
                id3v2Tag = new ID3v24Tag();
                mp3.setId3v2Tag(id3v2Tag);
            }

            // Set metadata fields
            if (title != null) id3v2Tag.setTitle(title);
            if (artist != null) {
                String artistName = artist;
                if (featuredArtists != null && !featuredArtists.isEmpty()) {
                    artistName = artist + " ft. " + featuredArtists;
                }
                id3v2Tag.setArtist(artistName);
            }
            if (producer != null) {
                // Producer is stored in TPE4 frame (usually "Interpreted, remixed, or otherwise modified by")
                id3v2Tag.setOriginalArtist(producer);
            }
            if (trackNumber != null) {
                id3v2Tag.setTrack(String.valueOf(trackNumber));
            }

            // Save the MP3 file with updated tag
            mp3.save(mp3FilePath + ".tmp");
            new File(mp3FilePath).delete();
            new File(mp3FilePath + ".tmp").renameTo(new File(mp3FilePath));
            
        } catch (Exception e) {
            throw new Exception("Failed to set MP3 metadata: " + e.getMessage(), e);
        }
    }

    /**
     * Sets album metadata for MP3
     * @param mp3FilePath Path to the MP3 file
     * @param albumTitle Album title
     * @param albumArtist Album artist
     * @throws Exception If setting metadata fails
     */
    public void setAlbumMetadata(String mp3FilePath, String albumTitle, String albumArtist) throws Exception {
        try {
            File mp3File = new File(mp3FilePath);
            if (!mp3File.exists()) {
                throw new IOException("MP3 file not found: " + mp3FilePath);
            }

            // Read MP3 file
            Mp3File mp3 = new Mp3File(mp3FilePath);
            
            // Get or create ID3v2 tag
            ID3v2 id3v2Tag;
            if (mp3.hasId3v2Tag()) {
                id3v2Tag = mp3.getId3v2Tag();
            } else {
                id3v2Tag = new ID3v24Tag();
                mp3.setId3v2Tag(id3v2Tag);
            }

            if (albumTitle != null) id3v2Tag.setAlbum(albumTitle);
            if (albumArtist != null) id3v2Tag.setAlbumArtist(albumArtist);

            // Save the MP3 file with updated tag
            mp3.save(mp3FilePath + ".tmp");
            new File(mp3FilePath).delete();
            new File(mp3FilePath + ".tmp").renameTo(new File(mp3FilePath));
            
        } catch (Exception e) {
            throw new Exception("Failed to set album metadata: " + e.getMessage(), e);
        }
    }

    /**
     * Determines MIME type from file extension
     * @param filePath Path to the image file
     * @return MIME type string
     */
    private String getMimeType(String filePath) {
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerPath.endsWith(".png")) {
            return "image/png";
        } else if (lowerPath.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerPath.endsWith(".bmp")) {
            return "image/bmp";
        }
        return "image/jpeg"; // Default to JPEG
    }
}
