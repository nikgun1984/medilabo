// Seed additional notes matched to the real patients in PostgreSQL
// Run with:
//   docker exec -i medilabo-mongodb mongosh -u medilabo -p medilabo123 --authenticationDatabase admin notes_db < scripts/insert_notes_real.js

db = db.getSiblingDB('notes_db');

db.notes.insertMany([
  // Patient 1 — Jane Doe
  {
    patId: 1,
    patient: "Doe",
    note: "Patient presents in good overall health. States she \"feels very well\" and has no current complaints.\nWeight is within the recommended range. Blood pressure normal.",
    createdAt: new Date("2025-09-10T09:15:00Z"),
    updatedAt: new Date("2025-09-10T09:15:00Z")
  },
  {
    patId: 1,
    patient: "Doe",
    note: "Follow-up visit. Patient reports mild fatigue over the past two weeks.\nSuggested blood panel — results pending.\nRecommended increased hydration and regular sleep schedule.",
    createdAt: new Date("2025-11-22T10:30:00Z"),
    updatedAt: new Date("2025-11-22T10:30:00Z")
  },

  // Patient 2 — John Shmo
  {
    patId: 2,
    patient: "Shmo",
    note: "Patient complains of persistent lower back pain, particularly after prolonged sitting.\nNo history of injury reported. Referred to physiotherapy.\nPrescribed anti-inflammatory medication.",
    createdAt: new Date("2025-08-05T14:00:00Z"),
    updatedAt: new Date("2025-08-05T14:00:00Z")
  },
  {
    patId: 2,
    patient: "Shmo",
    note: "Patient states stress levels at work remain high.\nAlso reports abnormal hearing in left ear — referred to ENT specialist.\nCholesterol slightly elevated. Dietary changes recommended.",
    createdAt: new Date("2025-10-18T11:45:00Z"),
    updatedAt: new Date("2025-10-18T11:45:00Z")
  },
  {
    patId: 2,
    patient: "Shmo",
    note: "ENT follow-up: mild sensorineural hearing loss confirmed.\nReaction to prescribed anti-inflammatory noted — medication switched.\nBlood pressure: 138/88. Monitoring required.",
    createdAt: new Date("2026-01-07T09:00:00Z"),
    updatedAt: new Date("2026-01-07T09:00:00Z")
  },

  // Patient 5 — Nick Gun
  {
    patId: 5,
    patient: "Gun",
    note: "Initial consultation. Patient is a healthy adult with no significant medical history.\nRoutine check-up requested. All vitals within normal range.\nHeight: 182cm. Weight: 78kg. BMI: 23.6.",
    createdAt: new Date("2025-07-14T08:30:00Z"),
    updatedAt: new Date("2025-07-14T08:30:00Z")
  },
  {
    patId: 5,
    patient: "Gun",
    note: "Patient reports occasional shortness of breath during intense exercise.\nECG results normal. Echocardiogram ordered as precaution.\nAdvised to reduce caffeine intake and monitor symptoms.",
    createdAt: new Date("2025-12-03T15:20:00Z"),
    updatedAt: new Date("2025-12-03T15:20:00Z")
  },
  {
    patId: 5,
    patient: "Gun",
    note: "Echocardiogram results: no structural abnormalities detected.\nHemoglobin A1C within normal range.\nPatient advised to continue current lifestyle — annual check-up scheduled.",
    createdAt: new Date("2026-02-19T10:00:00Z"),
    updatedAt: new Date("2026-02-19T10:00:00Z")
  }
]);

print("Inserted " + 8 + " additional notes.");
print("Total notes in collection: " + db.notes.countDocuments());
