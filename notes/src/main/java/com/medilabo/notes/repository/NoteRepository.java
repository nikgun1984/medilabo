package com.medilabo.notes.repository;

import com.medilabo.notes.model.Note;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NoteRepository extends MongoRepository<Note, String> {

    /** Return all notes for a given patient, ordered by creation date descending */
    List<Note> findByPatIdOrderByCreatedAtDesc(Long patId);
}
