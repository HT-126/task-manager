import { useCallback, useState } from 'react'
import { logout } from './api'
import AuthForm from './AuthForm'
import { clearSession, loadSession, saveSession } from './auth'
import TaskBoard from './TaskBoard'
import './App.css'

export default function App() {
  const [session, setSession] = useState(loadSession)

  function handleLoggedIn(s) {
    saveSession(s)
    setSession(s)
  }

  // Tell the server to revoke the token, but always forget it locally, even if that call fails.
  async function handleLogout() {
    try {
      await logout()
    } catch {
      // token may already be expired or revoked; nothing more to do
    }
    clearSession()
    setSession(null)
  }

  // useCallback keeps the function identity stable so TaskBoard's effect does not re-run.
  const handleExpired = useCallback(() => {
    clearSession()
    setSession(null)
  }, [])

  return (
    <main>
      <header className="top">
        <h1>Task Manager</h1>
        {session && (
          <span>
            {session.username} <button onClick={handleLogout}>Log out</button>
          </span>
        )}
      </header>
      {session ? (
        <TaskBoard onSessionExpired={handleExpired} />
      ) : (
        <AuthForm onLoggedIn={handleLoggedIn} />
      )}
    </main>
  )
}
