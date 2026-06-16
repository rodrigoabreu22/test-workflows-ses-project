import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError } from '../api/http'
import {
  createPizzeria,
  deletePizzeria,
  getPizzeriaById,
  listPizzerias,
  updatePizzeria,
  updatePizzeriaStaff,
} from '../api/pizzeria-api'
import { getDisplayIdentity } from '../auth/oidc'
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
  // The backend hides `ownerSubject` from every JSON response (see
  // docs/security-flows.md "API Hardening" — deliberate, to avoid leaking
  // who owns what). So the frontend can never *proactively* know "is this
  // mine"; the best it can do is:
  //   1. Role-based gating: CUSTOMER accounts hold none of the create/update/
  //      delete grants in permissions.csv, so their attempts are guaranteed
  //      to be refused — hide those controls for them entirely.
  //   2. Reactive ownership learning: PIZZERIA_OWNER accounts legitimately
  //      own *some* pizzerias and not others. The only authoritative signal
  //      for "not this one" is the backend's own 403 on update/delete — once
  //      we see that for an id, we remember it and hide that row's controls.
  // ADMIN bypasses ownership checks entirely (ownership_required=false), so
  // it never grows the "not mine" set.
  const identity = getDisplayIdentity()
  const authenticated = identity !== null
  const roles = identity?.roles ?? []
  const isAdmin = roles.includes('ADMIN')
  const isOwner = roles.includes('PIZZERIA_OWNER')
  const canMutate = isAdmin || isOwner

  const [pizzerias, setPizzerias] = useState<Pizzeria[]>([])
  const [isCreateOpen, setIsCreateOpen] = useState(false)
  const [createForm, setCreateForm] = useState<PizzeriaFormState>(emptyForm)
  const [editForm, setEditForm] = useState<PizzeriaFormState>(emptyForm)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editingStaffId, setEditingStaffId] = useState<number | null>(null)
  const [staffForm, setStaffForm] = useState('')
  const [lookupId, setLookupId] = useState('')
  const [lookupResult, setLookupResult] = useState<Pizzeria | null>(null)
  const [searchTerm, setSearchTerm] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const noticeTimerRef = useRef<number | null>(null)
  const errorTimerRef = useRef<number | null>(null)

  useEffect(() => {
    if (!notice) return
    if (noticeTimerRef.current) window.clearTimeout(noticeTimerRef.current)
    const timer = window.setTimeout(() => setNotice(''), 2500)
    noticeTimerRef.current = timer
    return () => window.clearTimeout(timer)
  }, [notice])

  useEffect(() => {
    if (!error) return
    if (errorTimerRef.current) window.clearTimeout(errorTimerRef.current)
    const timer = window.setTimeout(() => setError(''), 2500)
    errorTimerRef.current = timer
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
  }, [authenticated])

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
      setIsCreateOpen(false)
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
      if (err instanceof ApiError && err.status === 403) cancelEditing()
      setError((err as Error).message)
    }
  }

  const startEditingStaff = (pizzeria: Pizzeria) => {
    setEditingStaffId(pizzeria.id)
    setStaffForm((pizzeria.staffSubjects ?? []).join(', '))
  }

  const cancelEditingStaff = () => {
    setEditingStaffId(null)
    setStaffForm('')
  }

  const handleStaffUpdate = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (editingStaffId == null) return
    setError('')
    setNotice('')
    const staffSubjects = staffForm
      .split(',')
      .map((subject) => subject.trim())
      .filter((subject) => subject.length > 0)
    try {
      await updatePizzeriaStaff(editingStaffId, staffSubjects)
      setNotice(`Staff for pizzeria #${editingStaffId} updated.`)
      cancelEditingStaff()
      await refreshPizzerias()
    } catch (err) {
      if (err instanceof ApiError && err.status === 403) cancelEditingStaff()
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
      if (editingStaffId === id) cancelEditingStaff()
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
      {canMutate && (
        <div className="panel">
          <div className="panel-header">
            <h2>Create Pizzeria</h2>
            <button
              type="button"
              className="button-secondary"
              onClick={() => setIsCreateOpen((open) => !open)}
              aria-expanded={isCreateOpen}
            >
              {isCreateOpen ? 'Cancel' : '+ New Pizzeria'}
            </button>
          </div>
          {isCreateOpen && (
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
                  onChange={(event) =>
                    setCreateForm({ ...createForm, address: event.target.value })
                  }
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
          )}
        </div>
      )}

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

        {editingStaffId != null && (
          <form className="form-grid form-grid-inline" onSubmit={handleStaffUpdate}>
            <h3>Manage Staff for Pizzeria #{editingStaffId}</h3>
            <label>
              Staff usernames (comma-separated)
              <input
                value={staffForm}
                onChange={(event) => setStaffForm(event.target.value)}
                placeholder="staff-a, staff-b"
              />
            </label>
            <div className="row-actions">
              <button type="submit">Save Staff</button>
              <button type="button" className="button-secondary" onClick={cancelEditingStaff}>
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
                      {pizzeria.canEdit && (
                        <>
                          <button type="button" onClick={() => startEditing(pizzeria)}>
                            Edit
                          </button>
                          <button type="button" onClick={() => startEditingStaff(pizzeria)}>
                            Manage Staff
                          </button>
                          <button
                            type="button"
                            className="button-danger"
                            onClick={() => void handleDelete(pizzeria.id)}
                          >
                            Delete
                          </button>
                        </>
                      )}
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
