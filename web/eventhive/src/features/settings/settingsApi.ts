import { apiFetch } from '../../shared/api/client'

export interface UpdateProfileRequest {
  firstname: string
  lastname: string
  email: string
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}

export const settingsApi = {
  updateProfile: (data: UpdateProfileRequest) =>
    apiFetch<any>('/api/users/profile', {
      method: 'PUT',
      body: data,
    }),

  changePassword: (data: ChangePasswordRequest) =>
    apiFetch<any>('/api/users/password', {
      method: 'PUT',
      body: data,
    }),

  uploadProfilePic: (image: File) => {
    const formData = new FormData()
    formData.append('image', image)
    return apiFetch<{ profilePicUrl: string }>('/api/users/profile-pic', {
      method: 'POST',
      body: formData,
    })
  },
}