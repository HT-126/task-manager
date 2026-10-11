import { useState } from 'react'
import { STATUSES } from './constants'

const EMPTY = { title: '', description: '', status: 'TODO', dueDate: '' }

// Used for both "add" (no `initial`) and "edit" (`initial` = existing task).
export default function TaskForm({ initial, onSubmit, onCancel }) {
  const [form, setForm] = useState(
    initial
      ? {
          title: initial.title,
          description: initial.description ?? '',
          status: initial.status,
          dueDate: initial.dueDate ?? '',
        }
      : EMPTY,
  )

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value })

  function submit(e) {
    e.preventDefault()
    if (!form.title.trim()) return
    // Empty strings become null so the backend stores "no value".
    onSubmit({
      title: form.title.trim(),
      description: form.description || null,
      status: form.status,
      dueDate: form.dueDate || null,
    })
    if (!initial) setForm(EMPTY)
  }

  return (
    <form className="task-form" onSubmit={submit}>
      <input placeholder="Title (required)" value={form.title} onChange={set('title')} required />
      <input placeholder="Description" value={form.description} onChange={set('description')} />
      <select value={form.status} onChange={set('status')}>
        {STATUSES.map((s) => (
          <option key={s}>{s}</option>
        ))}
      </select>
      <input type="date" value={form.dueDate} onChange={set('dueDate')} />
      <button type="submit">{initial ? 'Save' : 'Add'}</button>
      {onCancel && (
        <button type="button" onClick={onCancel}>
          Cancel
        </button>
      )}
    </form>
  )
}
