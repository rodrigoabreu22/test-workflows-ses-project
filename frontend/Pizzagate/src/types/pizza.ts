export type PizzaSize = 'small' | 'medium' | 'large'

export interface Pizza {
  id: number
  name: string
  ingredients: string | null
  pizzeriaId: number
  price: number
  size: PizzaSize
  isAvailable: boolean
  photoUrl: string | null
  createdAt: string
}

export interface PizzaPayload {
  name: string
  ingredients?: string
  pizzeriaId: number
  price: number
  size: PizzaSize
  isAvailable?: boolean
  photoUrl?: string
}
