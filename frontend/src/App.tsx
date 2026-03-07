import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/Layout'
import PatientList from './pages/PatientList'
import PatientAdd from './pages/PatientAdd'
import PatientEdit from './pages/PatientEdit'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Navigate to="/patients" replace />} />
          <Route path="patients" element={<PatientList />} />
          <Route path="patients/new" element={<PatientAdd />} />
          <Route path="patients/:id/edit" element={<PatientEdit />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
