import { useEffect, useState } from 'react'
import { handleAuthorizationCallback } from './oidc'

// Bootstraps the Keycloak session in the background — it does NOT gate
// rendering. The dashboard shell (header, tab nav, session bar) is the
// landing experience for everyone (see App.tsx), so there is no "Sign in to
// continue" wall here any more: the data pages decide for themselves how to
// handle an anonymous visitor (GET reads on pizzerias/pizzas are public;
// mutation routes still require a token).
//
// What still has to happen on every page load, regardless of whether the
// visitor ends up authenticated: if the browser just landed back from
// Keycloak with `code`/`state` in the URL, complete the PKCE code exchange
// (handleAuthorizationCallback) so the access/refresh tokens land in storage.
//
// `getDisplayIdentity()`/`isAuthenticated()` read directly from localStorage
// rather than React state, so storing the tokens alone wouldn't cause
// `SessionBar`/the pages to notice the new session — something has to force
// a re-render once the exchange completes. This is a *hook* (not a wrapper
// component holding `{children}`) deliberately: a wrapper's re-render would
// leave `props.children` referentially unchanged (it was created by `App`'s
// last render), so React would bail out of re-rendering `SessionBar`/the
// pages entirely — the classic "stale children" trap. Calling this hook
// directly inside `App` means the state bump re-renders `App`, which
// recreates `SessionBar`/the pages fresh, so they re-read the new session.
//
// We surface only a generic failure banner if the exchange fails — never the
// raw OIDC error detail, for the same information-disclosure reasons as
// `api/http.ts`.
export function useAuthBootstrap(): boolean {
  const [callbackError, setCallbackError] = useState(false)
  const [, setSessionVersion] = useState(0)

  useEffect(() => {
    let cancelled = false

    const resolveCallback = async () => {
      try {
        const handled = await handleAuthorizationCallback()
        if (!cancelled && handled) setSessionVersion((version) => version + 1)
      } catch {
        if (!cancelled) setCallbackError(true)
      }
    }

    void resolveCallback()
    return () => {
      cancelled = true
    }
  }, [])

  return callbackError
}
