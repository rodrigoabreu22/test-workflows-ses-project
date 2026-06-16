import { ACCESS_TOKEN_KEY, refreshAccessToken } from '../auth/oidc'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
const ACCESS_TOKEN_STORAGE_KEY = ACCESS_TOKEN_KEY

// Thrown for any non-2xx API response. Carries only the HTTP status — never
// the raw response body. Forwarding backend error bodies (stack traces,
// validation internals, "user not found" vs. "wrong password" style details)
// straight into the UI is an information-disclosure vulnerability (OWASP
// A05/A09): it can reveal implementation details to an attacker and, for
// authorization failures, confirm or deny the existence of resources the
// caller has no business learning about. `message` below is always one of a
// fixed set of generic, role-appropriate strings — see `messageForStatus`.
export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

const messageForStatus = (status: number): string => {
  switch (status) {
    case 400:
      return 'The request was invalid. Check the form values and try again.'
    case 401:
      return 'You need to be signed in to do that.'
    case 403:
      return "You don't have permission to do that."
    case 404:
      return 'Not found.'
    case 409:
      return 'That action could not be completed because of a conflict. Refresh and try again.'
    case 413:
      return 'That request is too large.'
    case 429:
      return 'Too many requests. Please slow down and try again shortly.'
    default:
      return status >= 500
        ? 'Something went wrong on our end. Please try again later.'
        : 'The request could not be completed. Please try again.'
  }
}

const sendWithToken = (path: string, init: RequestInit | undefined, token: string | null) =>
  fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(init?.headers ?? {}),
    },
  })

export const apiRequest = async <T>(path: string, init?: RequestInit): Promise<T> => {
  const accessToken = window.localStorage.getItem(ACCESS_TOKEN_STORAGE_KEY)
  let response = await sendWithToken(path, init, accessToken)

  // Access tokens are short-lived (300s — see docs/security-flows.md). A 401
  // on a request that *did* carry a token most likely means it just expired:
  // ask Keycloak for a new one via the rotated refresh token and retry once,
  // rather than bouncing the user back to the login screen every 5 minutes.
  if (response.status === 401 && accessToken) {
    const refreshed = await refreshAccessToken()
    if (refreshed) {
      response = await sendWithToken(path, init, refreshed)
    }
  }

  if (!response.ok) {
    throw new ApiError(response.status, messageForStatus(response.status))
  }

  if (response.status === 204) {
    return undefined as T
  }

  const contentType = response.headers.get('content-type') ?? ''
  if (!contentType.includes('application/json')) {
    return undefined as T
  }

  return (await response.json()) as T
}
