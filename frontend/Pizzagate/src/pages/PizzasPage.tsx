import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { listPizzerias } from '../api/pizzeria-api'
import { createPizza, deletePizza, getPizzaById, listPizzas, updatePizza } from '../api/pizza-api'
import type { Pizza, PizzaPayload, PizzaSize } from '../types/pizza'
import type { Pizzeria } from '../types/pizzeria'

interface PizzaFormState {
  name: string
  ingredients: string
  pizzeriaId: string
  price: string
  size: PizzaSize
  isAvailable: boolean
  photoUrl: string
}

const defaultPizzaForm: PizzaFormState = {
  name: '',
  ingredients: '',
  pizzeriaId: '',
  price: '',
  size: 'medium',
  isAvailable: true,
  photoUrl: '',
}

const formatTimestamp = (value: string) => new Date(value).toLocaleString()

const mapToPayload = (form: PizzaFormState): PizzaPayload | null => {
  const pizzeriaId = Number(form.pizzeriaId)
  const price = Number(form.price)

  if (!Number.isInteger(pizzeriaId) || pizzeriaId <= 0) {
    return null
  }

  if (Number.isNaN(price) || price < 0) {
    return null
  }

  return {
    name: form.name.trim(),
    ingredients: form.ingredients.trim(),
    pizzeriaId,
    price,
    size: form.size,
    isAvailable: form.isAvailable,
    photoUrl: form.photoUrl.trim(),
  }
}

type PizzasPageProps = {
  selectedPizzeriaId?: number | null
}

export function PizzasPage({ selectedPizzeriaId }: PizzasPageProps) {
  const [pizzas, setPizzas] = useState<Pizza[]>([])
  const [pizzerias, setPizzerias] = useState<Pizzeria[]>([])
  const [createForm, setCreateForm] = useState<PizzaFormState>(defaultPizzaForm)
  const [editForm, setEditForm] = useState<PizzaFormState>(defaultPizzaForm)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [lookupId, setLookupId] = useState('')
  const [lookupResult, setLookupResult] = useState<Pizza | null>(null)
  const [searchTerm, setSearchTerm] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [expandedImage, setExpandedImage] = useState<{ src: string; alt: string } | null>(null)
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

  const refreshPizzas = async (pizzeriaFilter?: number) => {
    setIsLoading(true)
    setError('')
    try {
      const data = await listPizzas(pizzeriaFilter)
      setPizzas(data)
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setIsLoading(false)
    }
  }

  const refreshPizzerias = async () => {
    try {
      const data = await listPizzerias()
      setPizzerias(data)
    } catch {
      setPizzerias([])
    }
  }

  useEffect(() => {
    void refreshPizzerias()
  }, [])

  useEffect(() => {
    if (selectedPizzeriaId) {
      const selectedPizzeria = pizzerias.find((pizzeria) => pizzeria.id === selectedPizzeriaId)
      setSearchTerm(selectedPizzeria?.name ?? String(selectedPizzeriaId))
    }
    void refreshPizzas(selectedPizzeriaId ?? undefined)
  }, [selectedPizzeriaId, pizzerias])

  useEffect(() => {
    if (!expandedImage) return
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setExpandedImage(null)
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [expandedImage])

  const handleCreate = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setNotice('')
    const payload = mapToPayload(createForm)
    if (!payload || !payload.name) {
      setError('Valid name, pizzeria ID and price are required to create a pizza.')
      return
    }
    try {
      await createPizza(payload)
      setCreateForm(defaultPizzaForm)
      setNotice('Pizza created.')
      await refreshPizzas()
    } catch (err) {
      setError((err as Error).message)
    }
  }

  const startEditing = (pizza: Pizza) => {
    setEditingId(pizza.id)
    setEditForm({
      name: pizza.name,
      ingredients: pizza.ingredients ?? '',
      pizzeriaId: String(pizza.pizzeriaId),
      price: String(pizza.price),
      size: pizza.size,
      isAvailable: pizza.isAvailable,
      photoUrl: pizza.photoUrl ?? '',
    })
  }

  const cancelEditing = () => {
    setEditingId(null)
    setEditForm(defaultPizzaForm)
  }

  const handleUpdate = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (editingId == null) return
    setError('')
    setNotice('')
    const payload = mapToPayload(editForm)
    if (!payload || !payload.name) {
      setError('Valid name, pizzeria ID and price are required to update a pizza.')
      return
    }
    try {
      await updatePizza(editingId, payload)
      setNotice(`Pizza #${editingId} updated.`)
      cancelEditing()
      await refreshPizzas()
    } catch (err) {
      setError((err as Error).message)
    }
  }

  const handleDelete = async (id: number) => {
    if (!window.confirm(`Delete pizza #${id}?`)) return
    setError('')
    setNotice('')
    try {
      await deletePizza(id)
      if (editingId === id) cancelEditing()
      if (lookupResult?.id === id) setLookupResult(null)
      setNotice(`Pizza #${id} deleted.`)
      await refreshPizzas()
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
        const pizza = await getPizzaById(id)
        setLookupResult(pizza)
        return
      } catch (err) {
        setLookupResult(null)
        setError((err as Error).message)
        return
      }
    }
    const match = pizzas.find((pizza) => pizza.name.toLowerCase().includes(trimmed.toLowerCase()))
    if (!match) {
      setLookupResult(null)
      setError('No pizza found with that name.')
      return
    }
    setLookupResult(match)
  }

  const normalizedSearch = searchTerm.trim().toLowerCase()
  const searchId = normalizedSearch ? Number(normalizedSearch) : NaN
  const pizzeriaNameById = new Map(pizzerias.map((pizzeria) => [pizzeria.id, pizzeria.name]))
  const filteredPizzas =
    normalizedSearch.length === 0
      ? pizzas
      : pizzas.filter((pizza) => {
          const nameMatch = pizza.name.toLowerCase().includes(normalizedSearch)
          const pizzeriaName = pizzeriaNameById.get(pizza.pizzeriaId)?.toLowerCase() ?? ''
          const pizzeriaMatch = pizzeriaName.includes(normalizedSearch)
          if (Number.isInteger(searchId)) {
            return pizza.id === searchId || nameMatch || pizzeriaMatch
          }
          return nameMatch || pizzeriaMatch
        })

  return (
    <section className="page-grid">
      <div className="panel">
        <h2>Create Pizza</h2>
        <form className="form-grid" onSubmit={handleCreate}>
          <label>
            Name
            <input
              value={createForm.name}
              onChange={(event) => setCreateForm({ ...createForm, name: event.target.value })}
              placeholder="Margherita"
              required
            />
          </label>
          <label>
            Ingredients
            <input
              value={createForm.ingredients}
              onChange={(event) => setCreateForm({ ...createForm, ingredients: event.target.value })}
              placeholder="Tomato, mozzarella, basil"
            />
          </label>
          <label>
            Pizzeria
            <select
              value={createForm.pizzeriaId}
              onChange={(event) => setCreateForm({ ...createForm, pizzeriaId: event.target.value })}
              required
            >
              <option value="">Select a pizzeria</option>
              {pizzerias.map((pizzeria) => (
                <option key={pizzeria.id} value={pizzeria.id}>
                  #{pizzeria.id} - {pizzeria.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Price
            <input
              value={createForm.price}
              onChange={(event) => setCreateForm({ ...createForm, price: event.target.value })}
              placeholder="12.50"
              type="number"
              min="0"
              step="0.01"
              required
            />
          </label>
          <label>
            Size
            <select
              value={createForm.size}
              onChange={(event) =>
                setCreateForm({
                  ...createForm,
                  size: event.target.value as PizzaSize,
                })
              }
            >
              <option value="small">small</option>
              <option value="medium">medium</option>
              <option value="large">large</option>
            </select>
          </label>
          <label className="checkbox-field">
            <input
              type="checkbox"
              checked={createForm.isAvailable}
              onChange={(event) =>
                setCreateForm({
                  ...createForm,
                  isAvailable: event.target.checked,
                })
              }
            />
            Available
          </label>
          <label>
            Photo URL
            <input
              value={createForm.photoUrl}
              onChange={(event) => setCreateForm({ ...createForm, photoUrl: event.target.value })}
              placeholder="https://example.com/pizza.jpg"
            />
          </label>
          <button type="submit">Create</button>
        </form>
      </div>

      <div className="panel">
        <h2>Get Pizza By ID or Name</h2>
        <form className="inline-form" onSubmit={handleLookup}>
          <input
            value={lookupId}
            onChange={(event) => setLookupId(event.target.value)}
            placeholder="Pizza ID or name"
          />
          <button type="submit">Fetch</button>
        </form>
        {lookupResult && (
          <div className="result-card">
            {lookupResult.photoUrl && (
              <img
                src={lookupResult.photoUrl}
                alt={lookupResult.name}
                className="pizza-image pizza-image-large"
                loading="lazy"
              />
            )}
            <p>
              <strong>ID:</strong> {lookupResult.id}
            </p>
            <p>
              <strong>Name:</strong> {lookupResult.name}
            </p>
            <p>
              <strong>Pizzeria ID:</strong> {lookupResult.pizzeriaId}
            </p>
            <p>
              <strong>Size:</strong> {lookupResult.size}
            </p>
            <p>
              <strong>Price:</strong> {Number(lookupResult.price).toFixed(2)}
            </p>
            <p>
              <strong>Available:</strong> {lookupResult.isAvailable ? 'Yes' : 'No'}
            </p>
            <p>
              <strong>Created:</strong> {formatTimestamp(lookupResult.createdAt)}
            </p>
          </div>
        )}
      </div>

      <div className="panel panel-full">
        <div className="panel-header">
          <h2>All Pizzas</h2>
        </div>
        <form className="inline-form" onSubmit={(event) => event.preventDefault()}>
          <input
            value={searchTerm}
            onChange={(event) => setSearchTerm(event.target.value)}
            placeholder="Search by pizza ID, pizza name, or pizzeria name"
          />
          <button
            type="button"
            className="button-secondary"
            onClick={() => {
              setSearchTerm('')
              void refreshPizzas()
            }}
          >
            Clear
          </button>
        </form>

        {editingId != null && (
          <form className="form-grid form-grid-inline" onSubmit={handleUpdate}>
            <h3>Update Pizza #{editingId}</h3>
            <label>
              Name
              <input
                value={editForm.name}
                onChange={(event) => setEditForm({ ...editForm, name: event.target.value })}
                required
              />
            </label>
            <label>
              Ingredients
              <input
                value={editForm.ingredients}
                onChange={(event) => setEditForm({ ...editForm, ingredients: event.target.value })}
              />
            </label>
            <label>
              Pizzeria ID
              <input
                value={editForm.pizzeriaId}
                onChange={(event) => setEditForm({ ...editForm, pizzeriaId: event.target.value })}
                type="number"
                min="1"
                required
              />
            </label>
            <label>
              Price
              <input
                value={editForm.price}
                onChange={(event) => setEditForm({ ...editForm, price: event.target.value })}
                type="number"
                min="0"
                step="0.01"
                required
              />
            </label>
            <label>
              Size
              <select
                value={editForm.size}
                onChange={(event) =>
                  setEditForm({
                    ...editForm,
                    size: event.target.value as PizzaSize,
                  })
                }
              >
                <option value="small">small</option>
                <option value="medium">medium</option>
                <option value="large">large</option>
              </select>
            </label>
            <label className="checkbox-field">
              <input
                type="checkbox"
                checked={editForm.isAvailable}
                onChange={(event) =>
                  setEditForm({
                    ...editForm,
                    isAvailable: event.target.checked,
                  })
                }
              />
              Available
            </label>
            <label>
              Photo URL
              <input
                value={editForm.photoUrl}
                onChange={(event) => setEditForm({ ...editForm, photoUrl: event.target.value })}
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
                <th>Image</th>
                <th>Pizzeria ID</th>
                <th>Size</th>
                <th>Price</th>
                <th>Available</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredPizzas.length === 0 ? (
                <tr>
                  <td colSpan={8}>No pizzas found.</td>
                </tr>
              ) : (
                filteredPizzas.map((pizza) => (
                  <tr key={pizza.id}>
                    <td>{pizza.id}</td>
                    <td>{pizza.name}</td>
                    <td>
                      {pizza.photoUrl ? (
                        <button
                          type="button"
                          className="image-button"
                          onClick={() =>
                            setExpandedImage({
                              src: pizza.photoUrl!,
                              alt: pizza.name,
                            })
                          }
                        >
                          <img
                            src={pizza.photoUrl}
                            alt={pizza.name}
                            className="pizza-image"
                            loading="lazy"
                          />
                        </button>
                      ) : (
                        <span className="image-placeholder">No image</span>
                      )}
                    </td>
                    <td>{pizza.pizzeriaId}</td>
                    <td>{pizza.size}</td>
                    <td>{Number(pizza.price).toFixed(2)}</td>
                    <td>{pizza.isAvailable ? 'Yes' : 'No'}</td>
                    <td className="row-actions">
                      <button type="button" onClick={() => startEditing(pizza)}>
                        Edit
                      </button>
                      <button
                        type="button"
                        className="button-danger"
                        onClick={() => void handleDelete(pizza.id)}
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

        <div className="table-actions">
          <button
            type="button"
            className="button-secondary"
            onClick={() => void refreshPizzas()}
            disabled={isLoading}
          >
            {isLoading ? 'Refreshing...' : 'Refresh'}
          </button>
        </div>
      </div>

      {notice && <p className="message message-success">{notice}</p>}
      {error && <p className="message message-error">{error}</p>}
      {expandedImage && (
        <div
          className="image-overlay"
          role="dialog"
          aria-modal="true"
          onClick={() => setExpandedImage(null)}
        >
          <div className="image-overlay-card" onClick={(event) => event.stopPropagation()}>
            <button
              type="button"
              className="image-overlay-close"
              onClick={() => setExpandedImage(null)}
              aria-label="Close image"
            >
              Close
            </button>
            <img src={expandedImage.src} alt={expandedImage.alt} />
          </div>
        </div>
      )}
    </section>
  )
}
