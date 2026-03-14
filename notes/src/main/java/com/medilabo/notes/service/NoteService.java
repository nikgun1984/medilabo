package com.medilabo.notes.service;

import com.medilabo.notes.dto.NoteDTO;
import com.medilabo.notes.dto.NoteRequest;
import com.medilabo.notes.exception.NoteNotFoundException;
import com.medilabo.notes.model.Note;
import com.medilabo.notes.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;

    /** Return all notes for a given patient, most recent first */
    public List<NoteDTO> getNotesByPatient(Long patId) {
        log.info("Fetching notes for patId={}", patId);
        List<NoteDTO> notes = noteRepository.findByPatIdOrderByCreatedAtDesc(patId)
                .stream().map(this::toDTO).toList();
        log.debug("Found {} note(s) for patId={}", notes.size(), patId);
        return notes;
    }

    /** Return a single note by its MongoDB id */
    public NoteDTO getNoteById(String id) {
        log.info("Fetching note id={}", id);
        return noteRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> {
                    log.warn("Note not found id={}", id);
                    return new NoteNotFoundException(id);
                });
    }

    /** Create a new note */
    public NoteDTO createNote(NoteRequest request) {
        log.info("Creating note for patId={}", request.getPatId());
        Note note = Note.builder()
                .patId(request.getPatId())
                .patient(request.getPatient())
                .note(request.getNote())
                .build();
        NoteDTO saved = toDTO(noteRepository.save(note));
        log.info("Created note id={}", saved.getId());
        return saved;
    }

    /** Update the text of an existing note */
    public NoteDTO updateNote(String id, NoteRequest request) {
        log.info("Updating note id={}", id);
        Note existing = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(id));
        existing.setPatId(request.getPatId());
        existing.setPatient(request.getPatient());
        existing.setNote(request.getNote());
        return toDTO(noteRepository.save(existing));
    }

    /** Delete a note */
    public void deleteNote(String id) {
        log.info("Deleting note id={}", id);
        if (!noteRepository.existsById(id)) {
            throw new NoteNotFoundException(id);
        }
        noteRepository.deleteById(id);
    }

    // ── mapping ──────────────────────────────────────────────────────────────

    private NoteDTO toDTO(Note n) {
        return NoteDTO.builder()
                .id(n.getId())
                .patId(n.getPatId())
                .patient(n.getPatient())
                .note(n.getNote())
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .build();
    }
}
