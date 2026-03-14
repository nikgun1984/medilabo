package com.medilabo.notes;

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
