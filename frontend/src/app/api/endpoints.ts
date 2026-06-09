import { environment } from '../../environments/environment';

const API = environment.apiUrl;

export const Endpoints = {
  auth: {
    register: `${API}/auth/register`,
    login: `${API}/auth/login`,
    refresh: `${API}/auth/refresh`,
    forgotPassword: `${API}/auth/forgot-password`,
    resetPassword: `${API}/auth/reset-password`,
  },
  users: {
    me: `${API}/users/me`,
    byId: (id: number) => `${API}/users/${id}`,
    preferences: `${API}/users/me/preferences`,
    kyc: {
      selfie: `${API}/users/me/kyc/selfie`,
      idFront: `${API}/users/me/kyc/id-front`,
      idBack: `${API}/users/me/kyc/id-back`,
      data: `${API}/users/me/kyc/data`,
      status: `${API}/users/me/kyc`,
    },
    gdpr: `${API}/users/me/gdpr`,
  },
  listings: {
    base: `${API}/listings`,
    byId: (id: number) => `${API}/listings/${id}`,
    images: (id: number) => `${API}/listings/${id}/images`,
    publish: (id: number) => `${API}/listings/${id}/publish`,
    nearby: `${API}/listings/nearby`,
    search: `${API}/listings`,
    my: `${API}/listings/mine`,
    paymentPreview: `${API}/listings/payment-preview`,
  },
  rentals: {
    base: `${API}/rentals`,
    byId: (id: number) => `${API}/rentals/${id}`,
    asTenant: `${API}/rentals/as-tenant`,
    asOwner: `${API}/rentals/as-owner`,
    readyToPickup: (id: number) => `${API}/rentals/${id}/ready-to-pickup`,
    confirmPickup: (id: number) => `${API}/rentals/${id}/confirm-pickup`,
    return: `${API}/rentals/return`,
    cancel: (id: number) => `${API}/rentals/${id}/cancel`,
    qr: (id: number) => `${API}/rentals/${id}/qr`,
  },
  payments: {
    intent: (rentalId: number) => `${API}/payments/intent/${rentalId}`,
    preview: `${API}/payments/preview`,
    webhook: `${API}/payments/webhook`,
  },
  chat: {
    rooms: `${API}/chat/rooms`,
    roomById: (id: number) => `${API}/chat/rooms/${id}`,
    messages: (roomId: number) => `${API}/chat/rooms/${roomId}/messages`,
    send: `${API}/chat/messages`,
    sendFile: (roomId: number) => `${API}/chat/rooms/${roomId}/files`,
    markRead: (roomId: number) => `${API}/chat/rooms/${roomId}/read`,
    directRoom: (otherUserId: number) => `${API}/chat/rooms/direct/${otherUserId}`,
  },
  reviews: {
    base: `${API}/reviews`,
    byUserId: (userId: number) => `${API}/reviews/user/${userId}`,
  },
  wishlist: {
    base: `${API}/wishlist`,
    add: (listingId: number) => `${API}/wishlist/${listingId}`,
    remove: (listingId: number) => `${API}/wishlist/${listingId}`,
    check: (listingId: number) => `${API}/wishlist/${listingId}/check`,
  },
  geo: {
    search: `${API}/geo/search`,
  },
  dashboard: {
    overview: `${API}/dashboard`,
    reports: `${API}/dashboard/reports`,
    notifications: `${API}/dashboard/notifications`,
    notificationsUnread: `${API}/dashboard/notifications/unread-count`,
    markAllRead: `${API}/dashboard/notifications/read-all`,
  },
  admin: {
    users: `${API}/admin/users`,
    userById: (id: number) => `${API}/admin/users/${id}`,
    kycReview: (id: number) => `${API}/admin/users/${id}/kyc/review`,
    suspendUser: (id: number) => `${API}/admin/users/${id}/suspend`,
    activateUser: (id: number) => `${API}/admin/users/${id}/activate`,
    reports: `${API}/admin/reports`,
    smtp: `${API}/admin/smtp`,
    smtpTest: `${API}/admin/smtp/test`,
    smtpStatus: `${API}/admin/smtp/status`,
  },
} as const;
