export interface Pizzeria {
  id: number
  name: string
  address: string | null
  phone: string | null
  createdAt: string
}

export interface PizzeriaPayload {
  name: string
  address?: string
  phone?: string
}
