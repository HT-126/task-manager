import { useEffect, useState } from 'react'
import { createTask, deleteTask, listTasks, updateTask } from './api'
import { STATUSES } from './constants'
import TaskForm from './TaskForm'
import './App.css'

export default function App() {
  const [tasks, setTasks] = useState([])
  const [filter, setFilter] = useState('')
  const [editingId, setEditingId] = useState(null)
  const [error, setError] = useState('')

  // Bumping `reloads` re-runs the effect below, which refetches the list.
  const [reloads, setReloads] = useState(0)

  useEffect(() => {
    listTasks(filter)
      .then((data) => {
        setTasks(data)
        setError('')
      })
      .catch((e) => setError(e.message))
  }, [filter, reloads])

  // Run an API call, then reload the list; show any error.
  async function act(fn) {
    try {
      await fn()
      setEditingId(null)
      setReloads((n) => n + 1)
    } catch (e) {
      setError(e.message)
    }
  }

  return (
    <main>
      <h1>Task Manager</h1>

      <TaskForm onSubmit={(t) => act(() => createTask(t))} />

      <label className="filter">
        Filter:{' '}
        <select value={filter} onChange={(e) => setFilter(e.target.value)}>
          <option value="">All</option>
          {STATUSES.map((s) => (
            <option key={s}>{s}</option>
          ))}
        </select>
      </label>

      {error && <p className="error">{error}</p>}

      <ul className="tasks">
        {tasks.map((t) =>
          editingId === t.id ? (
            <li key={t.id}>
              <TaskForm
                initial={t}
                onSubmit={(body) => act(() => updateTask(t.id, body))}
                onCancel={() => setEditingId(null)}
              />
            </li>
          ) : (
            <li key={t.id}>
              <strong>{t.title}</strong> <span className={`badge ${t.status}`}>{t.status}</span>
              {t.dueDate && <span> due {t.dueDate}</span>}
              {t.description && <div className="desc">{t.description}</div>}
              <div>
                <button onClick={() => setEditingId(t.id)}>Edit</button>{' '}
                <button onClick={() => act(() => deleteTask(t.id))}>Delete</button>
              </div>
            </li>
          ),
        )}
        {tasks.length === 0 && <li className="empty">No tasks.</li>}
      </ul>
    </main>
  )
}
