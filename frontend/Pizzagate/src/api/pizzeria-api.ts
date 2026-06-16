import { apiRequest } from './http'
import type { Pizzeria, PizzeriaPayload } from '../types/pizzeria'
import type { Page } from '../types/pagination'

export const listPizzerias = async () => {
  const page = await apiRequest<Page<Pizzeria>>('/api/pizzerias?size=100')
  return page.content
}

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

export const updatePizzeriaStaff = (id: number, staffSubjects: string[]) =>
  apiRequest<Pizzeria>(`/api/pizzerias/${id}/staff`, {
    method: 'PUT',
    body: JSON.stringify({ staffSubjects }),
  })
