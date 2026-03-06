import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { auth, db } from './firebase';
import { signInWithEmailAndPassword, signOut } from 'firebase/auth';
import { collection, query, where, getDocs, doc, getDoc } from 'firebase/firestore';

// Компоненты
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import Dishes from './components/Dishes';
import Orders from './components/Orders';
import Reviews from './components/Reviews';

const theme = createTheme({
  palette: {
    primary: {
      main: '#f4511e',
    },
  },
});

function App() {
  const [user, setUser] = useState(null);
  const [userRole, setUserRole] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const unsubscribe = auth.onAuthStateChanged(async (firebaseUser) => {
      if (firebaseUser) {
        // Проверяем роль пользователя
        const userDoc = await getDoc(doc(db, 'users', firebaseUser.email));
        if (userDoc.exists()) {
          const role = userDoc.data().role;
          if (role === 'seller') {
            setUser(firebaseUser);
            setUserRole(role);
          }
        }
      } else {
        setUser(null);
        setUserRole(null);
      }
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <Routes>
          <Route
            path="/login"
            element={
              user && userRole === 'seller' ?
              <Navigate to="/dashboard" /> :
              <Login />
            }
          />
          <Route
            path="/dashboard"
            element={
              user && userRole === 'seller' ?
              <Dashboard /> :
              <Navigate to="/login" />
            }
          />
          <Route
            path="/dishes"
            element={
              user && userRole === 'seller' ?
              <Dishes /> :
              <Navigate to="/login" />
            }
          />
          <Route
            path="/orders"
            element={
              user && userRole === 'seller' ?
              <Orders /> :
              <Navigate to="/login" />
            }
          />
          <Route
            path="/reviews"
            element={
              user && userRole === 'seller' ?
              <Reviews /> :
              <Navigate to="/login" />
            }
          />
          <Route path="*" element={<Navigate to="/dashboard" />} />
        </Routes>
      </Router>
    </ThemeProvider>
  );
}

export default App;