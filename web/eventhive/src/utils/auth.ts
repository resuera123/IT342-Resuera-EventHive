export interface UserAuth {
  id: number
  firstname: string
  lastname: string
  email: string
}

const STORAGE_KEY = 'eventhive_user'

export function saveUser(user: UserAuth) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(user))
}

export function getUser(): UserAuth | null {
  const saved = localStorage.getItem(STORAGE_KEY)
  return saved ? (JSON.parse(saved) as UserAuth) : null
}

export function clearUser() {
  localStorage.removeItem(STORAGE_KEY)
}
