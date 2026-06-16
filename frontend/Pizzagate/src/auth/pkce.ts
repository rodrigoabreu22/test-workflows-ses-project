// RFC 7636 (PKCE) helpers for the Authorization Code + PKCE S256 flow.
// Keycloak's `pizzagate-spa` client has `pkce.code.challenge.method: S256`,
// so a plain Authorization Code flow without PKCE would be rejected outright.

const toBase64Url = (bytes: ArrayBuffer | Uint8Array): string => {
  const view = bytes instanceof Uint8Array ? bytes : new Uint8Array(bytes)
  let binary = ''
  view.forEach((byte) => {
    binary += String.fromCharCode(byte)
  })
  return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '')
}

// 32 random bytes -> base64url is well within the 43-128 char range RFC 7636 requires.
export const generateRandomToken = (): string => {
  const bytes = new Uint8Array(32)
  crypto.getRandomValues(bytes)
  return toBase64Url(bytes)
}

export const deriveCodeChallenge = async (verifier: string): Promise<string> => {
  const data = new TextEncoder().encode(verifier)
  const digest = await crypto.subtle.digest('SHA-256', data)
  return toBase64Url(digest)
}
