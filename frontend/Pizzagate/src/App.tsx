import { useState } from 'react'
import { PizzasPage } from './pages/PizzasPage'
import { PizzeriasPage } from './pages/PizzeriasPage'
import './App.css'

type View = 'pizzerias' | 'pizzas'

function App() {
  const [view, setView] = useState<View>('pizzerias')
  const [selectedPizzeriaId, setSelectedPizzeriaId] = useState<number | null>(null)

  return (
    <main className="app-shell">
      <header className="app-header">
        <div>
          <p className="eyebrow">Pizzagate Admin</p>
          <h1>Pizza & Pizzeria Management</h1>
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
