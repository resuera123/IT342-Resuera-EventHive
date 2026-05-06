import { API_BASE_URL } from '../api/client'

const PLACEHOLDER = '/placeholder.jpg'

export function resolveImageUrl(imageUrl?: string | null): string {
  if (!imageUrl || imageUrl.trim() === '') {
    return PLACEHOLDER
  }
  // Already a full URL (Supabase or any external CDN)
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl
  }
  // Relative path from the legacy local upload code path
  return `${API_BASE_URL}${imageUrl}`
}