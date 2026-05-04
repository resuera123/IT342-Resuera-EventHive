import { apiFetch } from '../../shared/api/client'
import type { UserAuth } from '../../shared/types'

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  firstname: string
  lastname: string
  email: string
  password: string
}

export interface AuthResponse extends UserAuth {
  status: string
}

export const authApi = {
  login: (data: LoginRequest) =>
    apiFetch<AuthResponse>('/api/auth/login', {
      method: 'POST',
      body: data,
    }),

  register: (data: RegisterRequest) =>
    apiFetch<AuthResponse>('/api/auth/register', {
      method: 'POST',
      body: data,
    }),

  getCurrentUser: () =>
    apiFetch<AuthResponse>('/api/auth/me'),
}