import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import {
  createPizzeria,
  deletePizzeria,
  getPizzeriaById,
  listPizzerias,
  updatePizzeria,
} from '../api/pizzeria-api'
import type { Pizzeria, PizzeriaPayload } from '../types/pizzeria'

interface PizzeriaFormState {
  name: string
  address: string
  phone: string
}

const emptyForm: PizzeriaFormState = {
  name: '',
  address: '',
  phone: '',
}

const toPayload = (form: PizzeriaFormState): PizzeriaPayload => ({
  name: form.name.trim(),
  address: form.address.trim(),
  phone: form.phone.trim(),
})

const formatTimestamp = (value: string) => new Date(value).toLocaleString()

type PizzeriasPageProps = {
  onShowPizzas?: (pizzeriaId: number) => void
}

export function PizzeriasPage({ onShowPizzas }: PizzeriasPageProps) {
  const [pizzerias, setPizzerias] = useState<Pizzeria[]>([])
  const [createForm, setCreateForm] = useState<PizzeriaFormState>(emptyForm)
  const [editForm, setEditForm] = useState<PizzeriaFormState>(emptyForm)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [lookupId, setLookupId] = useState('')
  const [lookupResult, setLookupResult] = useState<Pizzeria | null>(null)
  const [searchTerm, setSearchTerm] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [noticeTimer, setNoticeTimer] = useState<number | null>(null)
  const [errorTimer, setErrorTimer] = useState<number | null>(null)

  useEffect(() => {
    if (!notice) return
    if (noticeTimer) window.clearTimeout(noticeTimer)
    const timer = window.setTimeout(() => setNotice(''), 2500)
    setNoticeTimer(timer)
    return () => window.clearTimeout(timer)
  }, [notice])

  useEffect(() => {
    if (!error) return
    if (errorTimer) window.clearTimeout(errorTimer)
    const timer = window.setTimeout(() => setError(''), 2500)
    setErrorTimer(timer)
    return () => window.clearTimeout(timer)
  }, [error])

  const refreshPizzerias = async () => {
    setIsLoading(true)
    setError('')
    try {
      const data = await listPizzerias()
      setPizzerias(data)
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    void refreshPizzerias()
  }, [])

  const handleCreate = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setNotice('')
    if (!createForm.name.trim()) {
      setError('Name is required to create a pizzeria.')
      return
    }
    try {
      await createPizzeria(toPayload(createForm))
      setCreateForm(emptyForm)
      setNotice('Pizzeria created.')
      await refreshPizzerias()
    } catch (err) {
      setError((err as Error).message)
    }
  }

  const startEditing = (pizzeria: Pizzeria) => {
    setEditingId(pizzeria.id)
    setEditForm({
      name: pizzeria.name,
      address: pizzeria.address ?? '',
      phone: pizzeria.phone ?? '',
    })
  }

  const cancelEditing = () => {
    setEditingId(null)
    setEditForm(emptyForm)
  }

  const handleUpdate = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (editingId == null) return
    setError('')
    setNotice('')
    if (!editForm.name.trim()) {
      setError('Name is required to update a pizzeria.')
      return
    }
    try {
      await updatePizzeria(editingId, toPayload(editForm))
      setNotice(`Pizzeria #${editingId} updated.`)
      cancelEditing()
      await refreshPizzerias()
    } catch (err) {
      setError((err as Error).message)
    }
  }

  const handleDelete = async (id: number) => {
    if (!window.confirm(`Delete pizzeria #${id}?`)) return
    setError('')
    setNotice('')
    try {
      await deletePizzeria(id)
      if (editingId === id) cancelEditing()
      if (lookupResult?.id === id) setLookupResult(null)
      setNotice(`Pizzeria #${id} deleted.`)
      await refreshPizzerias()
    } catch (err) {
      setError((err as Error).message)
    }
  }

  const handleLookup = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setNotice('')
    const trimmed = lookupId.trim()
    if (!trimmed) {
      setError('Lookup value is required.')
      return
    }
    const id = Number(trimmed)
    if (Number.isInteger(id) && id > 0) {
      try {
        const pizzeria = await getPizzeriaById(id)
        setLookupResult(pizzeria)
        return
      } catch (err) {
        setLookupResult(null)
        setError((err as Error).message)
        return
      }
    }
    const match = pizzerias.find((pizzeria) =>
      pizzeria.name.toLowerCase().includes(trimmed.toLowerCase()),
    )
    if (!match) {
      setLookupResult(null)
      setError('No pizzeria found with that name.')
      return
    }
    setLookupResult(match)
  }

  const normalizedSearch = searchTerm.trim().toLowerCase()
  const searchId = normalizedSearch ? Number(normalizedSearch) : NaN
  const filteredPizzerias =
    normalizedSearch.length === 0
      ? pizzerias
      : pizzerias.filter((pizzeria) => {
          const nameMatch = pizzeria.name.toLowerCase().includes(normalizedSearch)
          if (Number.isInteger(searchId)) {
            return pizzeria.id === searchId || nameMatch
          }
          return nameMatch
        })

  return (
    <section className="page-grid">
      <div className="panel">
        <h2>Create Pizzeria</h2>
        <form className="form-grid" onSubmit={handleCreate}>
          <label>
            Name
            <input
              value={createForm.name}
              onChange={(event) => setCreateForm({ ...createForm, name: event.target.value })}
              placeholder="La Piazza"
              required
            />
          </label>
          <label>
            Address
            <input
              value={createForm.address}
              onChange={(event) => setCreateForm({ ...createForm, address: event.target.value })}
              placeholder="123 Main St"
            />
          </label>
          <label>
            Phone
            <input
              value={createForm.phone}
              onChange={(event) => setCreateForm({ ...createForm, phone: event.target.value })}
              placeholder="+1 555 0100"
            />
          </label>
          <button type="submit">Create</button>
        </form>
      </div>

      <div className="panel">
        <h2>Get Pizzeria By ID or Name</h2>
        <form className="inline-form" onSubmit={handleLookup}>
          <input
            value={lookupId}
            onChange={(event) => setLookupId(event.target.value)}
            placeholder="Pizzeria ID or name"
          />
          <button type="submit">Fetch</button>
        </form>
        {lookupResult && (
          <div className="result-card">
            <p>
              <strong>ID:</strong> {lookupResult.id}
            </p>
            <p>
              <strong>Name:</strong> {lookupResult.name}
            </p>
            <p>
              <strong>Address:</strong> {lookupResult.address ?? '-'}
            </p>
            <p>
              <strong>Phone:</strong> {lookupResult.phone ?? '-'}
            </p>
            <p>
              <strong>Created:</strong> {formatTimestamp(lookupResult.createdAt)}
            </p>
          </div>
        )}
      </div>

      <div className="panel panel-full">
        <div className="panel-header">
          <h2>All Pizzerias</h2>
          <button type="button" onClick={() => void refreshPizzerias()} disabled={isLoading}>
            {isLoading ? 'Refreshing...' : 'Refresh'}
          </button>
        </div>
        <form className="inline-form" onSubmit={(event) => event.preventDefault()}>
          <input
            value={searchTerm}
            onChange={(event) => setSearchTerm(event.target.value)}
            placeholder="Search by pizzeria ID or name"
          />
          <button type="button" className="button-secondary" onClick={() => setSearchTerm('')}>
            Clear Search
          </button>
        </form>

        {editingId != null && (
          <form className="form-grid form-grid-inline" onSubmit={handleUpdate}>
            <h3>Update Pizzeria #{editingId}</h3>
            <label>
              Name
              <input
                value={editForm.name}
                onChange={(event) => setEditForm({ ...editForm, name: event.target.value })}
                required
              />
            </label>
            <label>
              Address
              <input
                value={editForm.address}
                onChange={(event) => setEditForm({ ...editForm, address: event.target.value })}
              />
            </label>
            <label>
              Phone
              <input
                value={editForm.phone}
                onChange={(event) => setEditForm({ ...editForm, phone: event.target.value })}
              />
            </label>
            <div className="row-actions">
              <button type="submit">Save Update</button>
              <button type="button" className="button-secondary" onClick={cancelEditing}>
                Cancel
              </button>
            </div>
          </form>
        )}

        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Address</th>
                <th>Phone</th>
                <th>Created At</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredPizzerias.length === 0 ? (
                <tr>
                  <td colSpan={6}>No pizzerias found.</td>
                </tr>
              ) : (
                filteredPizzerias.map((pizzeria) => (
                  <tr key={pizzeria.id}>
                    <td>{pizzeria.id}</td>
                    <td>
                      <button
                        type="button"
                        className="link-button"
                        onClick={() => onShowPizzas?.(pizzeria.id)}
                      >
                        {pizzeria.name}
                      </button>
                    </td>
                    <td>{pizzeria.address ?? '-'}</td>
                    <td>{pizzeria.phone ?? '-'}</td>
                    <td>{formatTimestamp(pizzeria.createdAt)}</td>
                    <td className="row-actions">
                      <button type="button" onClick={() => onShowPizzas?.(pizzeria.id)}>
                        View Pizzas
                      </button>
                      <button type="button" onClick={() => startEditing(pizzeria)}>
                        Edit
                      </button>
                      <button
                        type="button"
                        className="button-danger"
                        onClick={() => void handleDelete(pizzeria.id)}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {notice && <p className="message message-success">{notice}</p>}
      {error && <p className="message message-error">{error}</p>}
    </section>
  )
}
