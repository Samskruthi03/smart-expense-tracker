import { apiClient } from './client'

import axios from 'axios'

const analyticsClient = axios.create({
  baseURL: 'http://localhost:8083',
  headers: { 'Content-Type': 'application/json' },
})

analyticsClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export interface CategoryBreakdown {
  category: string
  totalAmount: number
  expenseCount: number
  percentage: number
}

export interface MonthlySummary {
  year: number
  month: number
  totalSpent: number
  categories: CategoryBreakdown[]
}

export const analyticsApi = {
  monthly: (year: number, month: number) =>
    analyticsClient.get<MonthlySummary>(`/api/v1/analytics/monthly/${year}/${month}`),
  yearly: (year: number) =>
    analyticsClient.get<MonthlySummary[]>(`/api/v1/analytics/yearly/${year}`),
}