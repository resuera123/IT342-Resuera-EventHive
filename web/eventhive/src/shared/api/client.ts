// Centralized API configuration
// Falls back to localhost if no env var is set
export const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8081'

interface FetchOptions extends RequestInit {
  body?: any
}

/**
 * Wrapper around fetch() that automatically includes credentials (session cookie)
 * and handles JSON serialization/deserialization.
 */
export async function apiFetch<T = any>(
  path: string,
  options: FetchOptions = {}
): Promise<T> {
  const { body, headers, ...rest } = options

  const config: RequestInit = {
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...headers,
    },
    ...rest,
  }

  if (body && typeof body !== 'string' && !(body instanceof FormData)) {
    config.body = JSON.stringify(body)
  } else {
    config.body = body
  }

  // Don't set Content-Type for FormData — let browser set the boundary
  if (body instanceof FormData) {
    delete (config.headers as any)['Content-Type']
  }

  const response = await fetch(`${API_BASE_URL}${path}`, config)

  if (!response.ok) {
    const errorText = await response.text().catch(() => '')
    throw new Error(errorText || `Request failed: ${response.status}`)
  }

  // Handle empty responses
  const contentType = response.headers.get('content-type')
  if (contentType && contentType.includes('application/json')) {
    return response.json()
  }
  return response as any
}