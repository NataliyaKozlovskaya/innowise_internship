import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8077/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

const cleanToken = (token: string | null): string | null => {
  if (!token) return null;

  const cleanedToken = token.replace(/^Bearer\s+/i, '');
  return cleanedToken.trim() || null;
};

apiClient.interceptors.request.use(
  (config) => {
    const rawToken = localStorage.getItem('token');

    console.log('[Request Interceptor] URL:', config.url);
    console.log('[Request Interceptor] Method:', config.method?.toUpperCase());

    if (rawToken) {
      const cleanAccessToken = cleanToken(rawToken);

      if (cleanAccessToken) {
        config.headers.Authorization = `Bearer ${cleanAccessToken}`;
        console.log('[Request Interceptor] Token add to headers');
      } else {
        console.log('[Request Interceptor] Token is empty after clear');
      }
    } else {
      console.log('[Request Interceptor] Token was not find in localStorage');
    }

    console.log('📤 [Request Interceptor] Send request...');
    return config;
  },
  (error) => {
    console.error('❌ [Request Interceptor] Error:', error);
    return Promise.reject(error);
  }
);

apiClient.interceptors.response.use(
  (response) => {
    console.log('✅ [Response Interceptor] Success response:', {
      status: response.status,
      url: response.config.url,
      method: response.config.method?.toUpperCase()
    });

    return response;
  },
  (error) => {
    console.error('❌ [Response Interceptor] Error:', {
      status: error.response?.status,
      url: error.response?.config?.url,
      method: error.response?.config?.method?.toUpperCase(),
      data: error.response?.data,
      message: error.message
    });

    if (error.response?.status === 401) {
      console.log('🔐 [Response Interceptor] 401 - Unauthorized');
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      localStorage.removeItem('refreshToken');
      window.location.href = '/login';
    }

    return Promise.reject(error);
  }
);

export const saveToken = (accessToken: string): void => {
  if (!accessToken) {
    console.error('❌ [saveToken] Token is empty');
    return;
  }

  const cleanTokenValue = cleanToken(accessToken);

  if (cleanTokenValue) {
    localStorage.setItem('token', cleanTokenValue);
    console.log('✅ [saveToken] Token saved in the localStorage');
  } else {
    console.error('❌ [saveToken] Token was not cleaned');
  }
};

export const getToken = (): string | null => {
  const token = localStorage.getItem('token');
  return token;
};


export const clearTokens = (): void => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  localStorage.removeItem('refreshToken');
  console.log('✅ [clearTokens] All tokens were deleted');
};

export interface LoginRequest {
  login: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  email?: string;
  roles?: string[];
}

export interface RegistrationResponse {
  uuid: string;
  login: string;
  email: string;
}

export interface RegisterRequest {
  login: string;
  password: string;
  email: string;
  name?: string;
  surname?: string;
  birthDate?: string;
}

export const authApi = {
  login: async (data: LoginRequest) => {
    console.log('🔑 [authApi.login] Send request for login...');
    const response = await apiClient.post<LoginResponse>('/auth/login', data);

    if (response.data.accessToken) {
      saveToken(response.data.accessToken);

      if (response.data.refreshToken) {
        const cleanRefreshToken = cleanToken(response.data.refreshToken);
        if (cleanRefreshToken) {
          localStorage.setItem('refreshToken', cleanRefreshToken);
        }
      }
    }

    return response;
  },

  register: (data: RegisterRequest) => {
    console.log('📝 [authApi.register] Send request for registration...');
    return apiClient.post<RegistrationResponse>('/auth/register', data);
  },

  logout: () => {
    console.log('👋 [authApi.logout] Exist from system...');
    clearTokens();
    return apiClient.post('/auth/logout');
  },

  refreshToken: (refreshToken: string) => {
    console.log('🔄 [authApi.refreshToken] Refresh token...');
    return apiClient.post<LoginResponse>('/auth/refresh', { refreshToken });
  },
};

export interface OrderItemRequest {
  itemId: number;
  quantity: number;
}

export interface CreateOrderRequest {
  userId: string;
  items: OrderItemRequest[];
}

export enum OrderStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  PAYMENT_FAILED = 'PAYMENT_FAILED',
  CANCELLED = 'CANCELLED'
}

export interface OrderItemDTO {
  itemId: number;
  itemName: string;
  itemPrice: number;
  quantity: number;
}

export interface OrderDTO {
  userId: string;
  status: OrderStatus;
  creationDate: string;
  orderItems: OrderItemDTO[];
}

export interface UpdateOrderRequest {
  status?: OrderStatus;
  items?: OrderItemRequest[];
}

export const orderApi = {
  getAllOrders: () => {
    console.log('📋 [orderApi.getAllOrders] Get all orders...');
    return apiClient.get<OrderDTO[]>('/orders/batch');
  },

  getOrderById: (id: string) => {
    console.log(`📋 [orderApi.getOrderById] Get order ${id}...`);
    return apiClient.get<OrderDTO>(`/orders/${id}`);
  },

  getOrdersByUserId: (userId: string) => {
    console.log(`📋 [orderApi.getOrdersByUserId] Get users orders ${userId}...`);
    return apiClient.get<OrderDTO[]>(`/orders/users/internal/${userId}`);
  },

  createOrder: (data: CreateOrderRequest) => {
    console.log('➕ [orderApi.createOrder] Create new order...', data);
    return apiClient.post<OrderDTO>('/orders/', data);
  },

  updateOrder: (id: string, data: UpdateOrderRequest) => {
    console.log(`✏️ [orderApi.updateOrder] Update order ${id}...`, data);
    return apiClient.put<OrderDTO>(`/orders/${id}`, data);
  },

  deleteOrder: (id: string) => {
    console.log(`🗑️ [orderApi.deleteOrder] Delete order ${id}...`);
    return apiClient.delete(`/orders/${id}`);
  }
};

export enum PaymentStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED',
  CANCELLED = 'CANCELLED'
}

export interface PaymentDTO {
  orderId: number;
  userId: string;
  status: PaymentStatus;
  paymentAmount: number;
}

export interface CreatePaymentRequest {
  orderId: number;
  userId: string;
  paymentAmount: number;
  paymentMethod?: string;
  description?: string;
}

export interface UpdatePaymentRequest {
  status?: PaymentStatus;
  transactionId?: string;
}

export const paymentApi = {
  getAllPayments: () => {
    console.log('💰 [paymentApi.getAllPayments] Get all payments...');
    return apiClient.get<PaymentDTO[]>('/payments');
  },

  getPaymentById: (id: string) => {
    console.log(`💰 [paymentApi.getPaymentById] Get payment ${id}...`);
    return apiClient.get<PaymentDTO>(`/payments/${id}`);
  },

  getPaymentsByUserId: (userId: string) => {
    console.log(`💰 [paymentApi.getPaymentsByUserId] Get users payments ${userId}...`);
    return apiClient.get<PaymentDTO[]>(`/payments/user/${userId}`);
  },
};

export default apiClient;