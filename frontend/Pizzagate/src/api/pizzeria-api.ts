import { apiRequest } from './http'
import type { Pizzeria, PizzeriaPayload } from '../types/pizzeria'

export const listPizzerias = () => apiRequest<Pizzeria[]>('/api/pizzerias')

export const getPizzeriaById = (id: number) => apiRequest<Pizzeria>(`/api/pizzerias/${id}`)

export const createPizzeria = (payload: PizzeriaPayload) =>
  apiRequest<Pizzeria>('/api/pizzerias', {
    method: 'POST',
    body: JSON.stringify(payload),
  })

export const updatePizzeria = (id: number, payload: PizzeriaPayload) =>
  apiRequest<Pizzeria>(`/api/pizzerias/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })

export const deletePizzeria = (id: number) =>
  apiRequest<void>(`/api/pizzerias/${id}`, {
    method: 'DELETE',
  })
