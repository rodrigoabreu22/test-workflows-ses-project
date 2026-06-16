import { useState } from 'react'
import { useAuthBootstrap } from './auth/AuthSession'
import { getDisplayIdentity, login, logout } from './auth/oidc'
import { PizzasPage } from './pages/PizzasPage'
import { PizzeriasPage } from './pages/PizzeriasPage'
import './App.css'

type View = 'pizzerias' | 'pizzas'

// Anonymous visitors get a plain "Log in" button here rather than being
// walled off behind a full-screen gate — the dashboard is the landing
// experience for everyone. Signed-out users are sent to Keycloak only when
// they attempt something that actually requires an account, via
// requireLogin() in the pages.
function SessionBar() {
  const identity = getDisplayIdentity()

  if (!identity) {
    return (
      <div className="session-bar">
        <span>Browsing as a guest</span>
        <button type="button" className="button-primary" onClick={() => void login()}>
          Log in
        </button>
      </div>
    )
  }

  return (
    <div className="session-bar">
      <span>
        Signed in as <strong>{identity.username}</strong>
      </span>
      {identity.roles.length > 0 && (
        <span className="roles">
          {identity.roles.map((role) => (
            <span key={role} className="role-chip">
              {role}
            </span>
          ))}
        </span>
      )}
      <button type="button" className="button-secondary" onClick={logout}>
        Log out
      </button>
    </div>
  )
}

function App() {
  const callbackError = useAuthBootstrap()
  const [view, setView] = useState<View>('pizzerias')
  const [selectedPizzeriaId, setSelectedPizzeriaId] = useState<number | null>(null)

  return (
    <main className="app-shell">
      {callbackError && (
        <p className="message message-error auth-callback-error">
          Signing you in didn't work. Please try logging in again.
        </p>
      )}
      <SessionBar />
      <header className="app-header">
        <div>
          <p className="eyebrow">Pizzagate Admin</p>
          <h1>Pizza &amp; Pizzeria Management</h1>
        </div>
        <nav className="tab-nav" aria-label="Entity navigation">
          <button
            type="button"
            className={view === 'pizzerias' ? 'tab tab-active' : 'tab'}
            onClick={() => setView('pizzerias')}
          >
            Pizzerias
          </button>
          <button
            type="button"
            className={view === 'pizzas' ? 'tab tab-active' : 'tab'}
            onClick={() => setView('pizzas')}
          >
            Pizzas
          </button>
        </nav>
      </header>

      {view === 'pizzerias' ? (
        <PizzeriasPage
          onShowPizzas={(pizzeriaId) => {
            setSelectedPizzeriaId(pizzeriaId)
            setView('pizzas')
          }}
        />
      ) : (
        <PizzasPage selectedPizzeriaId={selectedPizzeriaId} />
      )}
    </main>
  )
}

export default App
