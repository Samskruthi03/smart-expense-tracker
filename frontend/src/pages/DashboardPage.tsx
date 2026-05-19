import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { expensesApi, CreateExpenseRequest, ExpenseCategory } from '../api/expenses'
import { useAuth } from '../context/AuthContext'
import { Link } from 'react-router-dom'

const CATEGORIES: ExpenseCategory[] = [
  'FOOD_AND_DRINK', 'TRANSPORT', 'ACCOMMODATION', 'ENTERTAINMENT',
  'SHOPPING', 'HEALTH', 'UTILITIES', 'SALARY', 'FREELANCE', 'INVESTMENT', 'OTHER'
]

const CATEGORY_COLORS: Record<string, string> = {
  FOOD_AND_DRINK: 'bg-orange-100 text-orange-700',
  TRANSPORT: 'bg-blue-100 text-blue-700',
  ACCOMMODATION: 'bg-purple-100 text-purple-700',
  ENTERTAINMENT: 'bg-pink-100 text-pink-700',
  SHOPPING: 'bg-yellow-100 text-yellow-700',
  HEALTH: 'bg-green-100 text-green-700',
  UTILITIES: 'bg-gray-100 text-gray-700',
  SALARY: 'bg-emerald-100 text-emerald-700',
  FREELANCE: 'bg-teal-100 text-teal-700',
  INVESTMENT: 'bg-indigo-100 text-indigo-700',
  OTHER: 'bg-slate-100 text-slate-700',
}

export default function DashboardPage() {
  const { user, logout } = useAuth()
  const queryClient = useQueryClient()
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState<CreateExpenseRequest>({
    amount: 0, currency: 'EUR', category: 'OTHER',
    description: '', expenseDate: new Date().toISOString().split('T')[0]
  })

  const { data, isLoading } = useQuery({
    queryKey: ['expenses'],
    queryFn: () => expensesApi.list({ pageSize: 50 }),
  })

  const createMutation = useMutation({
    mutationFn: expensesApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['expenses'] })
      setShowForm(false)
      setForm({
        amount: 0, currency: 'EUR', category: 'OTHER',
        description: '', expenseDate: new Date().toISOString().split('T')[0]
      })
    },
  })

  const deleteMutation = useMutation({
    mutationFn: expensesApi.delete,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['expenses'] }),
  })

  const expenses = data?.data.data ?? []
  const total = expenses.reduce((sum, e) => sum + Number(e.amount), 0)

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white border-b border-gray-200 px-6 py-4">
        <div className="max-w-5xl mx-auto flex items-center justify-between">
          <h1 className="text-xl font-bold text-brand-700">SmartExpense</h1>
          <div className="flex items-center gap-6">
            <Link to="/analytics"
              className="text-sm text-gray-600 hover:text-brand-600 font-medium">
              Analytics
            </Link>
            <span className="text-sm text-gray-500">
              {user?.firstName} {user?.lastName}
            </span>
            <button onClick={logout}
              className="text-sm text-gray-500 hover:text-red-600">
              Sign out
            </button>
          </div>
        </div>
      </nav>

      <main className="max-w-5xl mx-auto px-6 py-8">
        <div className="grid grid-cols-3 gap-4 mb-8">
          <div className="bg-white rounded-2xl border border-gray-200 p-6">
            <p className="text-sm text-gray-500">Total spent</p>
            <p className="text-3xl font-bold text-gray-900 mt-1">
              €{total.toFixed(2)}
            </p>
          </div>
          <div className="bg-white rounded-2xl border border-gray-200 p-6">
            <p className="text-sm text-gray-500">Transactions</p>
            <p className="text-3xl font-bold text-gray-900 mt-1">
              {expenses.length}
            </p>
          </div>
          <div className="bg-white rounded-2xl border border-gray-200 p-6">
            <p className="text-sm text-gray-500">Average</p>
            <p className="text-3xl font-bold text-gray-900 mt-1">
              €{expenses.length ? (total / expenses.length).toFixed(2) : '0.00'}
            </p>
          </div>
        </div>

        <div className="bg-white rounded-2xl border border-gray-200">
          <div className="flex items-center justify-between px-6 py-4
                        border-b border-gray-100">
            <h2 className="font-semibold text-gray-900">Expenses</h2>
            <button
              onClick={() => setShowForm(v => !v)}
              className="bg-brand-600 text-white text-sm px-4 py-2 rounded-lg
                       hover:bg-brand-700 transition-colors"
            >
              + Add expense
            </button>
          </div>

          {showForm && (
            <div className="px-6 py-4 border-b border-gray-100 bg-gray-50">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">
                    Amount (EUR)
                  </label>
                  <input
                    type="number" step="0.01" min="0.01"
                    value={form.amount || ''}
                    onChange={e => setForm(f => ({
                      ...f, amount: parseFloat(e.target.value)
                    }))}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2
                             text-sm focus:outline-none focus:ring-2
                             focus:ring-brand-500"
                    placeholder="0.00"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">
                    Category
                  </label>
                  <select
                    value={form.category}
                    onChange={e => setForm(f => ({
                      ...f, category: e.target.value as ExpenseCategory
                    }))}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2
                             text-sm focus:outline-none focus:ring-2
                             focus:ring-brand-500"
                  >
                    {CATEGORIES.map(c => (
                      <option key={c} value={c}>
                        {c.replace(/_/g, ' ')}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">
                    Date
                  </label>
                  <input
                    type="date"
                    value={form.expenseDate}
                    onChange={e => setForm(f => ({
                      ...f, expenseDate: e.target.value
                    }))}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2
                             text-sm focus:outline-none focus:ring-2
                             focus:ring-brand-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">
                    Description
                  </label>
                  <input
                    type="text"
                    value={form.description}
                    onChange={e => setForm(f => ({
                      ...f, description: e.target.value
                    }))}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2
                             text-sm focus:outline-none focus:ring-2
                             focus:ring-brand-500"
                    placeholder="What was this for?"
                  />
                </div>
              </div>
              <div className="flex gap-3 mt-4">
                <button
                  onClick={() => createMutation.mutate(form)}
                  disabled={createMutation.isPending || !form.amount}
                  className="bg-brand-600 text-white text-sm px-4 py-2 rounded-lg
                           hover:bg-brand-700 disabled:opacity-50"
                >
                  {createMutation.isPending ? 'Saving...' : 'Save expense'}
                </button>
                <button
                  onClick={() => setShowForm(false)}
                  className="text-sm px-4 py-2 rounded-lg border border-gray-300
                           hover:bg-gray-100"
                >
                  Cancel
                </button>
              </div>
            </div>
          )}

          {isLoading ? (
            <div className="px-6 py-12 text-center text-gray-400">
              Loading expenses...
            </div>
          ) : expenses.length === 0 ? (
            <div className="px-6 py-12 text-center text-gray-400">
              No expenses yet. Add your first one.
            </div>
          ) : (
            <div className="divide-y divide-gray-50">
              {expenses.map(expense => (
                <div key={expense.id}
                  className="flex items-center justify-between px-6 py-4
                           hover:bg-gray-50 transition-colors">
                  <div className="flex items-center gap-4">
                    <span className={`text-xs font-medium px-2.5 py-1 rounded-full
                                   ${CATEGORY_COLORS[expense.category]}`}>
                      {expense.category.replace(/_/g, ' ')}
                    </span>
                    <div>
                      <p className="text-sm font-medium text-gray-900">
                        {expense.description || '—'}
                      </p>
                      <p className="text-xs text-gray-400">{expense.expenseDate}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-4">
                    <span className="font-semibold text-gray-900">
                      €{Number(expense.amount).toFixed(2)}
                    </span>
                    <button
                      onClick={() => deleteMutation.mutate(expense.id)}
                      className="text-gray-300 hover:text-red-500 transition-colors
                               text-lg leading-none"
                    >
                      ×
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  )
}