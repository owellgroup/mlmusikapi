package com.mlmusik.service;

import com.mlmusik.model.Album;
import com.mlmusik.model.Song;
import com.mlmusik.repository.AlbumRepository;
import com.mlmusik.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class AlbumService {

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private MP3MetadataService mp3MetadataService;

    public List<Album> getAllAlbums() {
        return albumRepository.findAll();
    }

    public Optional<Album> getAlbumById(Long id) {
        return albumRepository.findById(id);
    }

    public Album createAlbum(String title, String artist, MultipartFile coverArt) throws Exception {
        // Store cover art
        String coverArtPath = fileStorageService.storeCoverArt(coverArt);

        // Create album
        Album album = new Album(title, artist, coverArtPath);
        return albumRepository.save(album);
    }

    public Album createAlbumWithSongs(String title, String artist, MultipartFile coverArt,
                                      String[] songTitles, String[] songArtists, String[] songFeaturedArtists,
                                      String[] songProducers, Integer[] songTrackNumbers, MultipartFile[] mp3Files) throws Exception {
        // Store cover art
        String coverArtPath = fileStorageService.storeCoverArt(coverArt);

        // Create album
        Album album = new Album(title, artist, coverArtPath);
        album = albumRepository.save(album);

        // Process each song
        for (int i = 0; i < songTitles.length; i++) {
            String songTitle = songTitles[i];
            String songArtist = songArtists[i];
            String featuredArtists = (songFeaturedArtists != null && i < songFeaturedArtists.length) 
                    ? songFeaturedArtists[i] : null;
            String producer = songProducers[i];
            Integer trackNumber = songTrackNumbers[i];
            MultipartFile mp3File = mp3Files[i];

            // Store MP3 file
            String mp3FilePath = fileStorageService.storeSong(mp3File);

            // Set metadata and embed cover art
            mp3MetadataService.setMetadata(mp3FilePath, songTitle, songArtist, featuredArtists, producer, trackNumber);
            mp3MetadataService.setAlbumMetadata(mp3FilePath, album.getTitle(), album.getArtist());
            mp3MetadataService.embedCoverArt(mp3FilePath, album.getCoverArtPath());

            // Create song
            Song song = new Song(songTitle, songArtist, featuredArtists, producer, trackNumber, mp3FilePath, album.getCoverArtPath());
            song.setAlbum(album);
            song = songRepository.save(song);

            album.getSongs().add(song);
        }

        // Save album with all songs
        return albumRepository.save(album);
    }

    public Album addSongToAlbum(Long albumId, String title, String artist, String featuredArtists,
                                String producer, Integer trackNumber, MultipartFile mp3File) throws Exception {
        Optional<Album> albumOpt = albumRepository.findById(albumId);
        if (!albumOpt.isPresent()) {
            throw new RuntimeException("Album not found");
        }

        Album album = albumOpt.get();

        // Store MP3 file
        String mp3FilePath = fileStorageService.storeSong(mp3File);

        // Set metadata and embed cover art
        mp3MetadataService.setMetadata(mp3FilePath, title, artist, featuredArtists, producer, trackNumber);
        mp3MetadataService.setAlbumMetadata(mp3FilePath, album.getTitle(), album.getArtist());
        mp3MetadataService.embedCoverArt(mp3FilePath, album.getCoverArtPath());

        // Create song
        Song song = new Song(title, artist, featuredArtists, producer, trackNumber, mp3FilePath, album.getCoverArtPath());
        song.setAlbum(album);
        song = songRepository.save(song);

        album.getSongs().add(song);
        return albumRepository.save(album);
    }

    public Album updateAlbum(Long id, String title, String artist) {
        Optional<Album> albumOpt = albumRepository.findById(id);
        if (albumOpt.isPresent()) {
            Album album = albumOpt.get();
            album.setTitle(title);
            album.setArtist(artist);
            return albumRepository.save(album);
        }
        return null;
    }

    public void deleteAlbum(Long id) {
        albumRepository.deleteById(id);
    }
}

