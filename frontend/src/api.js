import { clearSession, loadSession } from './auth'

const TASKS = '/api/tasks'
const AUTH = '/api/auth'

export class ApiError extends Error {
  constructor(status, message) {
    super(message)
    this.status = status
  }
}

const MESSAGES = {
  400: 'Please check the values you entered.',
  401: 'Invalid username or password, or your session has expired.',
  404: 'Not found.',
  409: 'That username is already taken.',
}

// Sends JSON, adds the Authorization header when logged in, and turns errors into ApiError.
async function request(url, options = {}) {
  const session = loadSession()
  const headers = { 'Content-Type': 'application/json' }
  if (session) headers.Authorization = `Bearer ${session.token}`
  const res = await fetch(url, { ...options, headers })
  if (!res.ok) {
    // A 401 on a task call means the token is gone or expired: forget it.
    if (res.status === 401 && session && !url.startsWith(AUTH)) clearSession()
    throw new ApiError(res.status, MESSAGES[res.status] ?? `Request failed (${res.status})`)
  }
  if (res.status === 204) return null
  const text = await res.text()
  return text ? JSON.parse(text) : null
}

export const register = (username, password) =>
  request(`${AUTH}/register`, { method: 'POST', body: JSON.stringify({ username, password }) })

export const login = (username, password) =>
  request(`${AUTH}/login`, { method: 'POST', body: JSON.stringify({ username, password }) })

export const logout = () => request(`${AUTH}/logout`, { method: 'POST' })

export const listTasks = (status) =>
  request(status ? `${TASKS}?status=${status}` : TASKS)

export const createTask = (task) =>
  request(TASKS, { method: 'POST', body: JSON.stringify(task) })

export const updateTask = (id, task) =>
  request(`${TASKS}/${id}`, { method: 'PUT', body: JSON.stringify(task) })

export const deleteTask = (id) => request(`${TASKS}/${id}`, { method: 'DELETE' })
