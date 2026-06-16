// Mirrors the subset of Spring Data's Page<T> JSON representation we rely on.
export interface Page<T> {
  content: T[]
}
