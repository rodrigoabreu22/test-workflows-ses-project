// Minimal OIDC Authorization Code + PKCE (S256) client for the `pizzagate-spa`
// public client, implementing the flow documented in docs/security-flows.md
// §"Login"/"Refresh"/"Logout and Revocation". No token-authority logic lives
// here beyond what a public SPA must do to talk to Keycloak directly — refresh
// tokens, sessions and revocation remain entirely Keycloak's responsibility,
// matching "Refresh tokens are owned by Keycloak, not by the Pizzagate API."
import { deriveCodeChallenge, generateRandomToken } from './pkce'

const ISSUER = import.meta.env.VITE_OIDC_ISSUER ?? 'http://localhost:8090/realms/pizzagate'
const CLIENT_ID = 'pizzagate-spa'

export const ACCESS_TOKEN_KEY = 'pizzagate_access_token'
const REFRESH_TOKEN_KEY = 'pizzagate_refresh_token'
const ID_TOKEN_KEY = 'pizzagate_id_token'
const PKCE_VERIFIER_KEY = 'pizzagate_pkce_verifier'
const PKCE_STATE_KEY = 'pizzagate_pkce_state'

interface TokenResponse {
  access_token: string
  refresh_token?: string
  id_token?: string
}

// The SPA is served from different origins depending on the path it's reached
// through (WAF at https://localhost/, or the direct dev port at :5173) — the
// redirect_uri must always be the *current* origin, and Keycloak must be told
// to trust both (see keycloak/realm-export.json `redirectUris`/`webOrigins`).
const redirectUri = (): string => `${window.location.origin}/`

const authEndpoint = () => `${ISSUER}/protocol/openid-connect/auth`
const tokenEndpoint = () => `${ISSUER}/protocol/openid-connect/token`
const logoutEndpoint = () => `${ISSUER}/protocol/openid-connect/logout`

const storeTokens = (tokens: TokenResponse): void => {
  window.localStorage.setItem(ACCESS_TOKEN_KEY, tokens.access_token)
  if (tokens.refresh_token) window.localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refresh_token)
  if (tokens.id_token) window.localStorage.setItem(ID_TOKEN_KEY, tokens.id_token)
}

const clearTokens = (): void => {
  window.localStorage.removeItem(ACCESS_TOKEN_KEY)
  window.localStorage.removeItem(REFRESH_TOKEN_KEY)
  window.localStorage.removeItem(ID_TOKEN_KEY)
}

export const getAccessToken = (): string | null => window.localStorage.getItem(ACCESS_TOKEN_KEY)

export const isAuthenticated = (): boolean => getAccessToken() !== null

// Decodes the (already signature-verified-by-the-backend) access token's
// payload purely for display purposes — e.g. showing "logged in as owner-a".
// This is NOT a trust decision; the backend independently validates every
// bearer token against the Keycloak JWKS on every request.
export const getDisplayIdentity = (): { username: string; roles: string[] } | null => {
  const token = getAccessToken()
  if (!token) return null
  try {
    const [, payloadSegment] = token.split('.')
    const normalized = payloadSegment.replace(/-/g, '+').replace(/_/g, '/')
    const payload = JSON.parse(atob(normalized)) as {
      preferred_username?: string
      realm_access?: { roles?: string[] }
    }
    return {
      username: payload.preferred_username ?? 'unknown',
      roles: payload.realm_access?.roles ?? [],
    }
  } catch {
    return null
  }
}

// Step 1 of Authorization Code + PKCE: redirect the browser to Keycloak with
// a freshly generated verifier (kept client-side in sessionStorage) and its
// SHA-256 challenge, plus an anti-CSRF `state`.
export const login = async (): Promise<void> => {
  const verifier = generateRandomToken()
  const challenge = await deriveCodeChallenge(verifier)
  const state = generateRandomToken()

  window.sessionStorage.setItem(PKCE_VERIFIER_KEY, verifier)
  window.sessionStorage.setItem(PKCE_STATE_KEY, state)

  const params = new URLSearchParams({
    client_id: CLIENT_ID,
    response_type: 'code',
    scope: 'openid',
    redirect_uri: redirectUri(),
    code_challenge: challenge,
    code_challenge_method: 'S256',
    state,
  })

  window.location.assign(`${authEndpoint()}?${params.toString()}`)
}

// Step 2: on return from Keycloak, exchange `code` for tokens using the
// verifier that never left the browser. Returns true if a callback was
// handled (whether it succeeded or the state didn't match), so the caller
// knows to stop showing a "checking session" spinner either way.
export const handleAuthorizationCallback = async (): Promise<boolean> => {
  const url = new URL(window.location.href)
  const code = url.searchParams.get('code')
  const returnedState = url.searchParams.get('state')
  const oidcError = url.searchParams.get('error')

  if (!code && !oidcError) return false

  const expectedState = window.sessionStorage.getItem(PKCE_STATE_KEY)
  const verifier = window.sessionStorage.getItem(PKCE_VERIFIER_KEY)
  window.sessionStorage.removeItem(PKCE_STATE_KEY)
  window.sessionStorage.removeItem(PKCE_VERIFIER_KEY)

  // Always scrub OIDC params from the visible URL, success or failure —
  // an authorization `code` left in the address bar/history is a credential.
  ;['code', 'state', 'session_state', 'iss', 'error', 'error_description'].forEach((key) =>
    url.searchParams.delete(key),
  )
  window.history.replaceState({}, document.title, url.toString())

  if (oidcError) {
    throw new Error(`Keycloak returned an error: ${oidcError}`)
  }
  if (!code || !verifier || !returnedState || returnedState !== expectedState) {
    throw new Error('Login response failed CSRF/state validation — please try logging in again.')
  }

  const body = new URLSearchParams({
    grant_type: 'authorization_code',
    client_id: CLIENT_ID,
    code,
    redirect_uri: redirectUri(),
    code_verifier: verifier,
  })

  const response = await fetch(tokenEndpoint(), {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: body.toString(),
  })

  if (!response.ok) {
    throw new Error(`Token exchange with Keycloak failed (HTTP ${response.status})`)
  }

  storeTokens((await response.json()) as TokenResponse)
  return true
}

// Refresh-token rotation lives entirely in Keycloak (security-flows.md
// "Refresh"). The SPA's only job is to redeem the current refresh token for a
// new access/refresh pair when the 300s access token expires, and to drop its
// session if Keycloak rejects the refresh token (rotation/reuse detection,
// revocation, expiry).
export const refreshAccessToken = async (): Promise<string | null> => {
  const refreshToken = window.localStorage.getItem(REFRESH_TOKEN_KEY)
  if (!refreshToken) return null

  const body = new URLSearchParams({
    grant_type: 'refresh_token',
    client_id: CLIENT_ID,
    refresh_token: refreshToken,
  })

  const response = await fetch(tokenEndpoint(), {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: body.toString(),
  })

  if (!response.ok) {
    clearTokens()
    return null
  }

  const tokens = (await response.json()) as TokenResponse
  storeTokens(tokens)
  return tokens.access_token
}

// Logout: drop local tokens and end the Keycloak session too (otherwise a
// fresh login would silently re-authenticate via the still-live SSO cookie).
export const logout = (): void => {
  const idToken = window.localStorage.getItem(ID_TOKEN_KEY)
  clearTokens()

  const params = new URLSearchParams({
    client_id: CLIENT_ID,
    post_logout_redirect_uri: redirectUri(),
  })
  if (idToken) params.set('id_token_hint', idToken)

  window.location.assign(`${logoutEndpoint()}?${params.toString()}`)
}
