package com.medilabo.notes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilabo.notes.dto.NoteDTO;
import com.medilabo.notes.dto.NoteRequest;
import com.medilabo.notes.repository.NoteRepository;
import com.medilabo.notes.service.NoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class NoteControllerTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7-jammy");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private NoteService noteService;

    @BeforeEach
    void setUp() {
        noteRepository.deleteAll();
    }

    // ── GET /api/notes/patient/{patId} ──────────────────────────────────────

    @Test
    void getNotesForPatient_returnsEmptyListWhenNone() throws Exception {
        mockMvc.perform(get("/api/notes/patient/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getNotesForPatient_returnsNotesOrderedByDate() throws Exception {
        createTestNote(1L, "Doe", "First visit note");
        createTestNote(1L, "Doe", "Second visit note");
        createTestNote(2L, "Shmo", "Other patient note");

        mockMvc.perform(get("/api/notes/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].patient", is("Doe")))
                .andExpect(jsonPath("$[1].patient", is("Doe")));
    }

    // ── GET /api/notes/{id} ─────────────────────────────────────────────────

    @Test
    void getNoteById_returnsNote() throws Exception {
        NoteDTO created = createTestNote(1L, "Doe", "Test note content");

        mockMvc.perform(get("/api/notes/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note", is("Test note content")))
                .andExpect(jsonPath("$.patId", is(1)))
                .andExpect(jsonPath("$.patient", is("Doe")));
    }

    @Test
    void getNoteById_returns404WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/notes/nonexistent-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Note Not Found")));
    }

    // ── POST /api/notes ─────────────────────────────────────────────────────

    @Test
    void createNote_returns201WithCreatedNote() throws Exception {
        NoteRequest request = new NoteRequest();
        request.setPatId(1L);
        request.setPatient("Doe");
        request.setNote("Patient presents with mild symptoms.");

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.patId", is(1)))
                .andExpect(jsonPath("$.patient", is("Doe")))
                .andExpect(jsonPath("$.note", is("Patient presents with mild symptoms.")));
    }

    @Test
    void createNote_preservesLineBreaks() throws Exception {
        String multiLine = "Line 1\nLine 2\n\tIndented line 3";
        NoteRequest request = new NoteRequest();
        request.setPatId(1L);
        request.setPatient("Doe");
        request.setNote(multiLine);

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.note", is(multiLine)));
    }

    @Test
    void createNote_returns400WhenPatIdMissing() throws Exception {
        NoteRequest request = new NoteRequest();
        request.setPatient("Doe");
        request.setNote("Some note");

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Validation Error")));
    }

    @Test
    void createNote_returns400WhenNoteBlank() throws Exception {
        NoteRequest request = new NoteRequest();
        request.setPatId(1L);
        request.setPatient("Doe");
        request.setNote("");

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createNote_returns400WhenPatientBlank() throws Exception {
        NoteRequest request = new NoteRequest();
        request.setPatId(1L);
        request.setPatient("");
        request.setNote("Some note");

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createNote_returns400WhenPatIdNegative() throws Exception {
        NoteRequest request = new NoteRequest();
        request.setPatId(-1L);
        request.setPatient("Doe");
        request.setNote("Some note");

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/notes/{id} ─────────────────────────────────────────────────

    @Test
    void updateNote_returnsUpdatedNote() throws Exception {
        NoteDTO original = createTestNote(1L, "Doe", "Original content");

        NoteRequest updateReq = new NoteRequest();
        updateReq.setPatId(1L);
        updateReq.setPatient("Doe");
        updateReq.setNote("Updated content with new observations.");

        mockMvc.perform(put("/api/notes/" + original.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note", is("Updated content with new observations.")))
                .andExpect(jsonPath("$.id", is(original.getId())));
    }

    @Test
    void updateNote_returns404WhenNotFound() throws Exception {
        NoteRequest updateReq = new NoteRequest();
        updateReq.setPatId(1L);
        updateReq.setPatient("Doe");
        updateReq.setNote("Updated content");

        mockMvc.perform(put("/api/notes/nonexistent-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/notes/{id} ──────────────────────────────────────────────

    @Test
    void deleteNote_returns204() throws Exception {
        NoteDTO created = createTestNote(1L, "Doe", "To be deleted");

        mockMvc.perform(delete("/api/notes/" + created.getId()))
                .andExpect(status().isNoContent());

        // Confirm it's gone
        mockMvc.perform(get("/api/notes/" + created.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteNote_returns404WhenNotFound() throws Exception {
        mockMvc.perform(delete("/api/notes/nonexistent-id"))
                .andExpect(status().isNotFound());
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private NoteDTO createTestNote(Long patId, String patient, String note) {
        NoteRequest req = new NoteRequest();
        req.setPatId(patId);
        req.setPatient(patient);
        req.setNote(note);
        return noteService.createNote(req);
    }
}
