// Keeps the login session (token + username) in localStorage so a page reload stays logged in.
// Trade-off: any script on the page can read localStorage (XSS), so keep the page free of
// untrusted HTML. Wrapped in try/catch because storage can be blocked by the browser.
const KEY = 'taskmanager.session'

export function loadSession() {
  try {
    const raw = localStorage.getItem(KEY)
    if (!raw) return null
    const session = JSON.parse(raw)
    // Drop a token that has already expired.
    if (!session.token || new Date(session.expiresAt) <= new Date()) return null
    return session
  } catch {
    return null
  }
}

export function saveSession(session) {
  try {
    localStorage.setItem(KEY, JSON.stringify(session))
  } catch {
    // Storage unavailable: the session just lasts until the page is closed.
  }
}

export function clearSession() {
  try {
    localStorage.removeItem(KEY)
  } catch {
    // ignore
  }
}
