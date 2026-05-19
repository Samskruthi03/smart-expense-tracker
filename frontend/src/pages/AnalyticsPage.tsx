import { useQuery } from '@tanstack/react-query'
import { analyticsApi } from '../api/analytics'
import { useAuth } from '../context/AuthContext'
import { Link } from 'react-router-dom'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid,
  Tooltip, PieChart, Pie, Cell, ResponsiveContainer, Legend
} from 'recharts'

const COLORS = ['#4f6ef7','#f97316','#22c55e','#a855f7',
                 '#ec4899','#14b8a6','#f59e0b','#64748b']

const MONTHS = ['Jan','Feb','Mar','Apr','May','Jun',
                'Jul','Aug','Sep','Oct','Nov','Dec']

export default function AnalyticsPage() {
  const { user, logout } = useAuth()
  const now = new Date()

  const { data: monthly, isLoading: monthlyLoading } = useQuery({
    queryKey: ['analytics', 'monthly', now.getFullYear(), now.getMonth() + 1],
    queryFn: () => analyticsApi.monthly(now.getFullYear(), now.getMonth() + 1),
  })

  const { data: yearly, isLoading: yearlyLoading } = useQuery({
    queryKey: ['analytics', 'yearly', now.getFullYear()],
    queryFn: () => analyticsApi.yearly(now.getFullYear()),
  })

  const monthlyData = monthly?.data
  const yearlyData = yearly?.data ?? []

  const barData = yearlyData.map(m => ({
    month: MONTHS[m.month - 1],
    total: Number(m.totalSpent),
  }))

  const pieData = monthlyData?.categories.map(c => ({
    name: c.category.replace(/_/g, ' '),
    value: Number(c.totalAmount),
    percentage: c.percentage,
  })) ?? []

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white border-b border-gray-200 px-6 py-4">
        <div className="max-w-5xl mx-auto flex items-center justify-between">
          <h1 className="text-xl font-bold text-brand-700">SmartExpense</h1>
          <div className="flex items-center gap-6">
            <Link to="/dashboard"
              className="text-sm text-gray-600 hover:text-brand-600 font-medium">
              Expenses
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
        <div className="grid grid-cols-2 gap-4 mb-8">
          <div className="bg-white rounded-2xl border border-gray-200 p-6">
            <p className="text-sm text-gray-500">
              This month ({MONTHS[now.getMonth()]})
            </p>
            <p className="text-3xl font-bold text-gray-900 mt-1">
              €{Number(monthlyData?.totalSpent ?? 0).toFixed(2)}
            </p>
          </div>
          <div className="bg-white rounded-2xl border border-gray-200 p-6">
            <p className="text-sm text-gray-500">Categories this month</p>
            <p className="text-3xl font-bold text-gray-900 mt-1">
              {monthlyData?.categories.length ?? 0}
            </p>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-6 mb-6">
          <div className="bg-white rounded-2xl border border-gray-200 p-6">
            <h2 className="font-semibold text-gray-900 mb-4">
              Spending by category — {MONTHS[now.getMonth()]}
            </h2>
            {monthlyLoading ? (
              <p className="text-gray-400 text-sm">Loading...</p>
            ) : pieData.length === 0 ? (
              <p className="text-gray-400 text-sm">No data yet</p>
            ) : (
              <ResponsiveContainer width="100%" height={220}>
                <PieChart>
                  <Pie data={pieData} dataKey="value" nameKey="name"
                    cx="50%" cy="50%" outerRadius={80} label={
                      ({ name, percentage }) =>
                        `${name} ${percentage.toFixed(0)}%`
                    }>
                    {pieData.map((_, i) => (
                      <Cell key={i} fill={COLORS[i % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(v: number) => `€${v.toFixed(2)}`} />
                </PieChart>
              </ResponsiveContainer>
            )}
          </div>

          <div className="bg-white rounded-2xl border border-gray-200 p-6">
            <h2 className="font-semibold text-gray-900 mb-4">
              Monthly spending — {now.getFullYear()}
            </h2>
            {yearlyLoading ? (
              <p className="text-gray-400 text-sm">Loading...</p>
            ) : barData.length === 0 ? (
              <p className="text-gray-400 text-sm">No data yet</p>
            ) : (
              <ResponsiveContainer width="100%" height={220}>
                <BarChart data={barData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                  <XAxis dataKey="month" tick={{ fontSize: 12 }} />
                  <YAxis tick={{ fontSize: 12 }}
                    tickFormatter={v => `€${v}`} />
                  <Tooltip formatter={(v: number) => `€${v.toFixed(2)}`} />
                  <Bar dataKey="total" fill="#4f6ef7" radius={[4,4,0,0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>

        <div className="bg-white rounded-2xl border border-gray-200 p-6">
          <h2 className="font-semibold text-gray-900 mb-4">Category breakdown</h2>
          {pieData.length === 0 ? (
            <p className="text-gray-400 text-sm">No data yet</p>
          ) : (
            <div className="space-y-3">
              {monthlyData?.categories.map((c, i) => (
                <div key={c.category} className="flex items-center gap-3">
                  <div className="w-3 h-3 rounded-full flex-shrink-0"
                    style={{ background: COLORS[i % COLORS.length] }} />
                  <span className="text-sm text-gray-600 w-40 truncate">
                    {c.category.replace(/_/g, ' ')}
                  </span>
                  <div className="flex-1 bg-gray-100 rounded-full h-2">
                    <div className="h-2 rounded-full"
                      style={{
                        width: `${c.percentage}%`,
                        background: COLORS[i % COLORS.length]
                      }} />
                  </div>
                  <span className="text-sm font-medium text-gray-900 w-20 text-right">
                    €{Number(c.totalAmount).toFixed(2)}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  )
}