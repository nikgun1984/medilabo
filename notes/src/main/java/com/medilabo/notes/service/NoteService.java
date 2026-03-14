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
        long start = System.currentTimeMillis();
        List<NoteDTO> notes = noteRepository.findByPatIdOrderByCreatedAtDesc(patId)
                .stream().map(this::toDTO).toList();
        long elapsed = System.currentTimeMillis() - start;
        log.info("Found {} note(s) for patId={} in {}ms", notes.size(), patId, elapsed);
        return notes;
    }

    /** Return a single note by its MongoDB id */
    public NoteDTO getNoteById(String id) {
        log.info("Fetching note id={}", id);
        return noteRepository.findById(id)
                .map(n -> {
                    log.debug("Note id={} belongs to patId={}, patient={}", id, n.getPatId(), n.getPatient());
                    return toDTO(n);
                })
                .orElseThrow(() -> {
                    log.warn("Note not found id={}", id);
                    return new NoteNotFoundException(id);
                });
    }

    /** Create a new note */
    public NoteDTO createNote(NoteRequest request) {
        log.info("Creating note for patId={}, patient={}, noteLength={}",
                request.getPatId(), request.getPatient(), request.getNote().length());
        Note note = Note.builder()
                .patId(request.getPatId())
                .patient(request.getPatient())
                .note(request.getNote())
                .build();
        NoteDTO saved = toDTO(noteRepository.save(note));
        log.info("Created note id={} for patId={}", saved.getId(), saved.getPatId());
        return saved;
    }

    /** Update the text of an existing note */
    public NoteDTO updateNote(String id, NoteRequest request) {
        log.info("Updating note id={} for patId={}", id, request.getPatId());
        Note existing = noteRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot update — note not found id={}", id);
                    return new NoteNotFoundException(id);
                });
        log.debug("Existing note id={}: patId={}, noteLength={}", id, existing.getPatId(), existing.getNote().length());
        existing.setPatId(request.getPatId());
        existing.setPatient(request.getPatient());
        existing.setNote(request.getNote());
        NoteDTO updated = toDTO(noteRepository.save(existing));
        log.info("Updated note id={} — new noteLength={}", id, request.getNote().length());
        return updated;
    }

    /** Delete a note */
    public void deleteNote(String id) {
        log.info("Deleting note id={}", id);
        if (!noteRepository.existsById(id)) {
            log.warn("Cannot delete — note not found id={}", id);
            throw new NoteNotFoundException(id);
        }
        noteRepository.deleteById(id);
        log.info("Deleted note id={}", id);
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
