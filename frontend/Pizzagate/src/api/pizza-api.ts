import { apiRequest } from './http'
import type { Pizza, PizzaPayload } from '../types/pizza'

export const listPizzas = (pizzeriaId?: number) => {
  const query = pizzeriaId == null ? '' : `?pizzeriaId=${pizzeriaId}`
  return apiRequest<Pizza[]>(`/api/pizzas${query}`)
}

export const getPizzaById = (id: number) => apiRequest<Pizza>(`/api/pizzas/${id}`)

export const createPizza = (payload: PizzaPayload) =>
  apiRequest<Pizza>('/api/pizzas', {
    method: 'POST',
    body: JSON.stringify(payload),
  })

export const updatePizza = (id: number, payload: PizzaPayload) =>
  apiRequest<Pizza>(`/api/pizzas/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })

export const deletePizza = (id: number) =>
  apiRequest<void>(`/api/pizzas/${id}`, {
    method: 'DELETE',
  })
