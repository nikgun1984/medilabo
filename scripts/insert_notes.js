// Seed script for the notes MongoDB collection
// Run with:
//   docker exec -i medilabo-mongodb mongosh -u medilabo -p medilabo123 --authenticationDatabase admin notes_db < scripts/insert_notes.js

db = db.getSiblingDB('notes_db');

db.notes.drop();

db.notes.insertMany([
  {
    patId: 1,
    patient: "TestNone",
    note: "The patient states that they \"feel very well.\" Weight is equal to or below what is recommended.",
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    patId: 2,
    patient: "TestBorderline",
    note: "The patient states that they feel a lot of stress at work. They also complain that their hearing has been abnormal lately.",
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    patId: 2,
    patient: "TestBorderline",
    note: "The patient states that they had a reaction to medication in the past three months. They also note that their hearing continues to be abnormal.",
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    patId: 3,
    patient: "TestInDanger",
    note: "The patient states that they have recently started smoking.",
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    patId: 3,
    patient: "TestInDanger",
    note: "The patient states that they used to smoke but that they quit smoking last year. They also complain of abnormal sleep apnea episodes. Laboratory tests indicate a high LDL cholesterol level.",
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    patId: 4,
    patient: "TestEarlyOnset",
    note: "The patient states that it has become difficult for them to climb stairs. They also complain of shortness of breath. Laboratory tests indicate elevated antibody levels. Reaction to medication.",
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    patId: 4,
    patient: "TestEarlyOnset",
    note: "The patient states that they experience back pain when sitting for long periods.",
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    patId: 4,
    patient: "TestEarlyOnset",
    note: "The patient states that they have recently started smoking. Hemoglobin A1C is above the recommended level.",
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    patId: 4,
    patient: "TestEarlyOnset",
    note: "Height, Weight, Cholesterol, Dizziness, and Reaction to medication.",
    createdAt: new Date(),
    updatedAt: new Date()
  }
]);

print("Inserted " + db.notes.countDocuments() + " notes.");
