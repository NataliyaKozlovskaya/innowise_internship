import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8093/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

// Интерцептор для JWT токена
apiClient.interceptors.request.use(
    (config) => {
      const token = localStorage.getItem('token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
);

// Обработка ошибок
apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
      }
      return Promise.reject(error);
    }
);

// ========== ТИПЫ ==========
export interface LoginRequest {
  login: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
}

// Обновленный RegisterRequest - одна форма для всего
export interface RegisterRequest {
  login: string;
  password: string;
  email: string;
  name?: string;
  surname?: string;
  birthDate?: string;
  // uuid будет сгенерирован на бэкенде
}

// ========== API МЕТОДЫ ==========
export const authApi = {
  // Логин
  login: (data: LoginRequest) =>
      apiClient.post<LoginResponse>('/auth/login', data),

  // Регистрация (одна конечная точка)
  register: (data: RegisterRequest) =>
      apiClient.post<void>('/auth/register', data),
  // Может быть void или LoginResponse, если сразу возвращает токены

  // Выход
  logout: () =>
      apiClient.post('/auth/logout'),

  // Обновление токена
  refreshToken: (refreshToken: string) =>
      apiClient.post<LoginResponse>('/auth/refresh', { refreshToken }),
};

export default apiClient;