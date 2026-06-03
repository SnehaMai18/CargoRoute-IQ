import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';

export const AlertContext = createContext();

export function useAlert() {
  return useContext(AlertContext);
}

export default function AlertProvider({ children }) {
  const [alert, setAlert] = useState({ visible: false, type: 'success', message: '', duration: 5000 });

  const showAlert = useCallback((type, message, duration = 5000) => {
    if (!message) return;
    setAlert({ visible: true, type, message, duration });
  }, []);

  const clearAlert = useCallback(() => {
    setAlert((prev) => ({ ...prev, visible: false }));
  }, []);

  useEffect(() => {
    if (!alert.visible) return undefined;
    const timer = window.setTimeout(() => {
      setAlert((prev) => ({ ...prev, visible: false }));
    }, alert.duration);
    return () => window.clearTimeout(timer);
  }, [alert.visible, alert.duration]);

  return (
    <AlertContext.Provider value={{ alert, showAlert, clearAlert }}>
      {children}
    </AlertContext.Provider>
  );
}
