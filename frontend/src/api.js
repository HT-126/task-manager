// Thin wrapper over the backend REST API. Paths are relative: Vite (dev) or nginx (Docker) proxies /api.
const BASE = '/api/tasks'

async function request(url, options) {
  const res = await fetch(url, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })
  if (!res.ok) throw new Error(`Request failed: ${res.status}`)
  return res.status === 204 ? null : res.json()
}

export const listTasks = (status) =>
  request(status ? `${BASE}?status=${status}` : BASE)

export const createTask = (task) =>
  request(BASE, { method: 'POST', body: JSON.stringify(task) })

export const updateTask = (id, task) =>
  request(`${BASE}/${id}`, { method: 'PUT', body: JSON.stringify(task) })

export const deleteTask = (id) => request(`${BASE}/${id}`, { method: 'DELETE' })
