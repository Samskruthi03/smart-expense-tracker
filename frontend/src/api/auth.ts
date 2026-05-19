import { apiClient } from './client'
import axios from 'axios'

const authClient = axios.create({
  baseURL: 'http://localhost:8081',
  headers: { 'Content-Type': 'application/json' },
})

export interface RegisterRequest {
  email: string
  password: string
  firstName: string
  lastName: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface AuthResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
  userId: string
  email: string
  firstName: string
  lastName: string
}

export const authApi = {
  register: (data: RegisterRequest) =>
    authClient.post<AuthResponse>('/api/v1/auth/register', data),
  login: (data: LoginRequest) =>
    authClient.post<AuthResponse>('/api/v1/auth/login', data),
  me: () => authClient.get('/api/v1/auth/me'),
}