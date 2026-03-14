package com.medilabo.notes;

import com.medilabo.notes.dto.NoteDTO;
import com.medilabo.notes.dto.NoteRequest;
import com.medilabo.notes.exception.NoteNotFoundException;
import com.medilabo.notes.repository.NoteRepository;
import com.medilabo.notes.service.NoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
class NoteServiceTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7-jammy");

    @Autowired
    private NoteService noteService;

    @Autowired
    private NoteRepository noteRepository;

    @BeforeEach
    void setUp() {
        noteRepository.deleteAll();
    }

    // ── Create + Retrieve ───────────────────────────────────────────────────

    @Test
    void createAndRetrieveNote() {
        NoteRequest req = new NoteRequest();
        req.setPatId(1L);
        req.setPatient("TestNone");
        req.setNote("Patient feels well.");

        var created = noteService.createNote(req);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getNote()).isEqualTo("Patient feels well.");

        var fetched = noteService.getNoteById(created.getId());
        assertThat(fetched.getPatId()).isEqualTo(1L);
    }

    @Test
    void createNote_setsPatientAndPatId() {
        NoteRequest req = new NoteRequest();
        req.setPatId(42L);
        req.setPatient("Smith");
        req.setNote("Initial consultation.");

        NoteDTO created = noteService.createNote(req);
        assertThat(created.getPatId()).isEqualTo(42L);
        assertThat(created.getPatient()).isEqualTo("Smith");
        assertThat(created.getNote()).isEqualTo("Initial consultation.");
        assertThat(created.getId()).isNotBlank();
    }

    // ── Get by patient ──────────────────────────────────────────────────────

    @Test
    void getNotesByPatient_returnsOnlyMatchingNotes() {
        createNote(1L, "TestNone", "Note A");
        createNote(2L, "TestBorderline", "Note B");

        List<NoteDTO> notes = noteService.getNotesByPatient(1L);
        assertThat(notes).hasSize(1);
        assertThat(notes.get(0).getPatient()).isEqualTo("TestNone");
    }

    @Test
    void getNotesByPatient_returnsEmptyForUnknownPatient() {
        createNote(1L, "TestNone", "Note A");

        List<NoteDTO> notes = noteService.getNotesByPatient(999L);
        assertThat(notes).isEmpty();
    }

    @Test
    void getNotesByPatient_returnsMultipleNotesInOrder() {
        createNote(5L, "Gun", "First visit");
        createNote(5L, "Gun", "Second visit");
        createNote(5L, "Gun", "Third visit");

        List<NoteDTO> notes = noteService.getNotesByPatient(5L);
        assertThat(notes).hasSize(3);
        // Most recent first (descending by createdAt)
        assertThat(notes.get(0).getNote()).isEqualTo("Third visit");
    }

    // ── Get by ID ───────────────────────────────────────────────────────────

    @Test
    void getNoteById_throwsWhenNotFound() {
        assertThatThrownBy(() -> noteService.getNoteById("nonexistent-id"))
                .isInstanceOf(NoteNotFoundException.class)
                .hasMessageContaining("nonexistent-id");
    }

    // ── Update ──────────────────────────────────────────────────────────────

    @Test
    void updateNote_changesContent() {
        NoteDTO created = createNote(1L, "TestNone", "Original");

        NoteRequest req = new NoteRequest();
        req.setPatId(1L);
        req.setPatient("TestNone");
        req.setNote("Updated content");
        var updated = noteService.updateNote(created.getId(), req);
        assertThat(updated.getNote()).isEqualTo("Updated content");
    }

    @Test
    void updateNote_changesPatientAndPatId() {
        NoteDTO created = createNote(1L, "TestNone", "Note text");

        NoteRequest req = new NoteRequest();
        req.setPatId(2L);
        req.setPatient("TestBorderline");
        req.setNote("Note text");
        var updated = noteService.updateNote(created.getId(), req);

        assertThat(updated.getPatId()).isEqualTo(2L);
        assertThat(updated.getPatient()).isEqualTo("TestBorderline");
        assertThat(updated.getId()).isEqualTo(created.getId()); // same document
    }

    @Test
    void updateNote_throwsWhenNotFound() {
        NoteRequest req = new NoteRequest();
        req.setPatId(1L);
        req.setPatient("TestNone");
        req.setNote("Updated");

        assertThatThrownBy(() -> noteService.updateNote("nonexistent-id", req))
                .isInstanceOf(NoteNotFoundException.class);
    }

    // ── Delete ──────────────────────────────────────────────────────────────

    @Test
    void deleteNote_removesFromDatabase() {
        NoteDTO created = createNote(1L, "TestNone", "To be deleted");

        noteService.deleteNote(created.getId());
        assertThatThrownBy(() -> noteService.getNoteById(created.getId()))
                .isInstanceOf(NoteNotFoundException.class);
    }

    @Test
    void deleteNote_throwsWhenNotFound() {
        assertThatThrownBy(() -> noteService.deleteNote("nonexistent-id"))
                .isInstanceOf(NoteNotFoundException.class);
    }

    @Test
    void deleteNote_doesNotAffectOtherNotes() {
        NoteDTO note1 = createNote(1L, "TestNone", "Keep this");
        NoteDTO note2 = createNote(1L, "TestNone", "Delete this");

        noteService.deleteNote(note2.getId());

        assertThat(noteService.getNotesByPatient(1L)).hasSize(1);
        assertThat(noteService.getNoteById(note1.getId()).getNote()).isEqualTo("Keep this");
    }

    // ── Formatting preservation ─────────────────────────────────────────────

    @Test
    void preservesOriginalFormatting() {
        String multiLine = "Line one.\nLine two.\n\tIndented line three.";
        NoteDTO saved = createNote(3L, "TestInDanger", multiLine);
        assertThat(noteService.getNoteById(saved.getId()).getNote()).isEqualTo(multiLine);
    }

    @Test
    void preservesLongNote() {
        String longNote = "A".repeat(10_000);
        NoteDTO saved = createNote(1L, "TestNone", longNote);
        assertThat(noteService.getNoteById(saved.getId()).getNote()).hasSize(10_000);
    }

    @Test
    void preservesSpecialCharacters() {
        String special = "Patient says: \"I feel better!\"\nTemperature: 98.6°F\nWeight ≤ 150 lbs\n\tTab indented.";
        NoteDTO saved = createNote(1L, "TestNone", special);
        assertThat(noteService.getNoteById(saved.getId()).getNote()).isEqualTo(special);
    }

    @Test
    void preservesUnicodeContent() {
        String unicode = "Patiënt klaagt over pijn.\n患者は痛みを訴えています。\nПациент жалуется на боль.";
        NoteDTO saved = createNote(1L, "TestNone", unicode);
        assertThat(noteService.getNoteById(saved.getId()).getNote()).isEqualTo(unicode);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private NoteDTO createNote(Long patId, String patient, String note) {
        NoteRequest req = new NoteRequest();
        req.setPatId(patId);
        req.setPatient(patient);
        req.setNote(note);
        return noteService.createNote(req);
    }
}


    @Autowired
    private NoteService noteService;

    @Autowired
    private NoteRepository noteRepository;

    @BeforeEach
    void setUp() {
        noteRepository.deleteAll();
    }

    @Test
    void createAndRetrieveNote() {
        NoteRequest req = new NoteRequest();
        req.setPatId(1L);
        req.setPatient("TestNone");
        req.setNote("Patient feels well.");

        var created = noteService.createNote(req);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getNote()).isEqualTo("Patient feels well.");

        var fetched = noteService.getNoteById(created.getId());
        assertThat(fetched.getPatId()).isEqualTo(1L);
    }

    @Test
    void getNotesByPatient_returnsOnlyMatchingNotes() {
        NoteRequest r1 = new NoteRequest();
        r1.setPatId(1L); r1.setPatient("TestNone"); r1.setNote("Note A");
        NoteRequest r2 = new NoteRequest();
        r2.setPatId(2L); r2.setPatient("TestBorderline"); r2.setNote("Note B");

        noteService.createNote(r1);
        noteService.createNote(r2);

        List<?> notes = noteService.getNotesByPatient(1L);
        assertThat(notes).hasSize(1);
    }

    @Test
    void updateNote_changesContent() {
        NoteRequest req = new NoteRequest();
        req.setPatId(1L); req.setPatient("TestNone"); req.setNote("Original");
        var created = noteService.createNote(req);

        req.setNote("Updated content");
        var updated = noteService.updateNote(created.getId(), req);
        assertThat(updated.getNote()).isEqualTo("Updated content");
    }

    @Test
    void deleteNote_removesFromDatabase() {
        NoteRequest req = new NoteRequest();
        req.setPatId(1L); req.setPatient("TestNone"); req.setNote("To be deleted");
        var created = noteService.createNote(req);

        noteService.deleteNote(created.getId());
        assertThatThrownBy(() -> noteService.getNoteById(created.getId()))
                .isInstanceOf(NoteNotFoundException.class);
    }

    @Test
    void preservesOriginalFormatting() {
        String multiLine = "Line one.\nLine two.\n\tIndented line three.";
        NoteRequest req = new NoteRequest();
        req.setPatId(3L); req.setPatient("TestInDanger"); req.setNote(multiLine);

        var saved = noteService.createNote(req);
        assertThat(noteService.getNoteById(saved.getId()).getNote()).isEqualTo(multiLine);
    }
}
