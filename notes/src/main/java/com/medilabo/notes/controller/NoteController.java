package com.medilabo.notes.controller;

import com.medilabo.notes.dto.NoteDTO;
import com.medilabo.notes.dto.NoteRequest;
import com.medilabo.notes.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for patient notes.
 *
 * Base path: /api/notes
 *
 * GET    /api/notes/patient/{patId}  — list all notes for a patient
 * GET    /api/notes/{id}             — get one note by MongoDB id
 * POST   /api/notes                  — add a new note
 * PUT    /api/notes/{id}             — update an existing note
 * DELETE /api/notes/{id}             — delete a note
 */
@Slf4j
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    /** List all notes for a patient, most recent first */
    @GetMapping("/patient/{patId}")
    public ResponseEntity<List<NoteDTO>> getNotesByPatient(@PathVariable Long patId) {
        log.info("GET /api/notes/patient/{}", patId);
        return ResponseEntity.ok(noteService.getNotesByPatient(patId));
    }

    /** Get one note by its id */
    @GetMapping("/{id}")
    public ResponseEntity<NoteDTO> getNoteById(@PathVariable String id) {
        log.info("GET /api/notes/{}", id);
        return ResponseEntity.ok(noteService.getNoteById(id));
    }

    /** Add a new note */
    @PostMapping
    public ResponseEntity<NoteDTO> createNote(@Valid @RequestBody NoteRequest request) {
        log.info("POST /api/notes — patId={}", request.getPatId());
        return ResponseEntity.status(HttpStatus.CREATED).body(noteService.createNote(request));
    }

    /** Update an existing note */
    @PutMapping("/{id}")
    public ResponseEntity<NoteDTO> updateNote(
            @PathVariable String id,
            @Valid @RequestBody NoteRequest request) {
        log.info("PUT /api/notes/{}", id);
        return ResponseEntity.ok(noteService.updateNote(id, request));
    }

    /** Delete a note */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable String id) {
        log.info("DELETE /api/notes/{}", id);
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }
}
