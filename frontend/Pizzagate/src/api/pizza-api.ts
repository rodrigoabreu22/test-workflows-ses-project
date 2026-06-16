import { apiRequest } from './http'
import type { Pizza, PizzaPayload } from '../types/pizza'
import type { Page } from '../types/pagination'

export const listPizzas = async (pizzeriaId?: number) => {
  const query = pizzeriaId == null ? 'size=100' : `pizzeriaId=${pizzeriaId}&size=100`
  const page = await apiRequest<Page<Pizza>>(`/api/pizzas?${query}`)
  return page.content
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
