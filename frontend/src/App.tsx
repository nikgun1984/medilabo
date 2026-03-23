import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import PatientList from './pages/PatientList'
import PatientAdd from './pages/PatientAdd'
import PatientEdit from './pages/PatientEdit'
import PatientView from './pages/PatientView'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public route */}
          <Route path="/login" element={<LoginPage />} />

          {/* Protected routes */}
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="/patients" replace />} />
            <Route path="patients" element={<PatientList />} />
            <Route path="patients/new" element={<PatientAdd />} />
            <Route path="patients/:id/view" element={<PatientView />} />
            <Route path="patients/:id/edit" element={<PatientEdit />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
