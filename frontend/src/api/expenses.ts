import { apiClient } from './client'
import { v4 as uuidv4 } from 'uuid'

export type ExpenseCategory =
  | 'FOOD_AND_DRINK' | 'TRANSPORT' | 'ACCOMMODATION'
  | 'ENTERTAINMENT' | 'SHOPPING' | 'HEALTH'
  | 'UTILITIES' | 'SALARY' | 'FREELANCE'
  | 'INVESTMENT' | 'OTHER'

export interface Expense {
  id: string
  userId: string
  amount: number
  currency: string
  category: ExpenseCategory
  description: string
  expenseDate: string
  createdAt: string
  updatedAt: string
}

export interface CreateExpenseRequest {
  amount: number
  currency: string
  category: ExpenseCategory
  description: string
  expenseDate: string
}

export interface PagedResponse<T> {
  data: T[]
  pageSize: number
  hasMore: boolean
  nextCursor: string | null
}

export const expensesApi = {
  list: (params?: {
    category?: string
    fromDate?: string
    toDate?: string
    cursor?: string
    pageSize?: number
  }) => apiClient.get<PagedResponse<Expense>>('/api/v1/expenses', { params }),

  create: (data: CreateExpenseRequest) =>
    apiClient.post<Expense>('/api/v1/expenses', data, {
      headers: { 'Idempotency-Key': uuidv4() },
    }),

  delete: (id: string) =>
    apiClient.delete(`/api/v1/expenses/${id}`),
}