// Color mappings for event categories and statuses

export const CATEGORIES = [
  'Music', 'Sports', 'Tech', 'Arts',
  'Food & Drink', 'Business', 'Health'
] as const

export function getCategoryColor(category: string): string {
  const map: Record<string, string> = {
    'Music': '#7c3aed',
    'Sports': '#ea580c',
    'Tech': '#0891b2',
    'Arts': '#db2777',
    'Food & Drink': '#d97706',
    'Business': '#1e40af',
    'Health': '#059669',
  }
  return map[category] || '#6c757d'
}

export function getStatusColor(status: string): string {
  const map: Record<string, string> = {
    'UPCOMING': '#0d6efd',
    'ONGOING': '#198754',
    'CANCELLED': '#dc3545',
    'COMPLETED': '#6c757d',
  }
  return map[status] || '#6c757d'
}