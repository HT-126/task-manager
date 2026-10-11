import { useState } from 'react'
import { login, register } from './api'

// One form for both "log in" and "create account".
export default function AuthForm({ onLoggedIn }) {
  const [mode, setMode] = useState('login')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const isRegister = mode === 'register'

  async function submit(e) {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      if (isRegister) await register(username, password)
      // After registering, log in straight away.
      const session = await login(username, password)
      onLoggedIn(session)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <form className="auth-form" onSubmit={submit}>
      <h2>{isRegister ? 'Create account' : 'Log in'}</h2>
      <input
        placeholder="Username"
        autoComplete="username"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
        minLength={isRegister ? 3 : undefined}
        maxLength={50}
        required
      />
      <input
        type="password"
        placeholder={isRegister ? 'Password (min 8 characters)' : 'Password'}
        autoComplete={isRegister ? 'new-password' : 'current-password'}
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        minLength={isRegister ? 8 : undefined}
        maxLength={128}
        required
      />
      {error && <p className="error">{error}</p>}
      <button type="submit" disabled={busy}>
        {isRegister ? 'Register' : 'Log in'}
      </button>
      <button
        type="button"
        className="link"
        onClick={() => {
          setMode(isRegister ? 'login' : 'register')
          setError('')
        }}
      >
        {isRegister ? 'Already have an account? Log in' : 'Need an account? Register'}
      </button>
    </form>
  )
}
