export interface ApiResponse<T> {
  success: boolean;
  data: T;
  requestId: string;
  timestamp: string;
}

export interface ApiError {
  code: string;
  message: string;
  requestId: string;
  timestamp: string;
  fields?: { field: string; message: string }[];
}

export interface PageResponse<T> {
  items: T[];
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export class ApiClient {
  private baseUrl = '/api/v1';

  private get token() { 
    return localStorage.getItem('jwt'); 
  }

  private async request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const headers = new Headers(options.headers);
    if (this.token) {
      headers.set('Authorization', 'Bearer ' + this.token);
    }
    if (!headers.has('Content-Type') && options.body && typeof options.body === 'string') {
      headers.set('Content-Type', 'application/json');
    }

    let response;
    try {
      response = await fetch(this.baseUrl + endpoint, { ...options, headers });
    } catch (networkError) {
      throw new Error('Network error connecting to backend.');
    }

    if (response.status === 401) {
      localStorage.removeItem('jwt');
      localStorage.removeItem('refresh_token');
      window.dispatchEvent(new Event('auth-failed'));
      throw new Error('Authentication required');
    }

    if (!response.ok) {
      let errorMsg = 'An error occurred';
      try {
        const errorData: ApiError = await response.json();
        errorMsg = errorData.message || errorMsg;
      } catch (e) {
        errorMsg = response.statusText;
      }
      throw new Error(errorMsg);
    }

    if (response.status === 204) {
      return {} as T;
    }

    const data: ApiResponse<T> = await response.json();
    return data.data;
  }

  get<T>(endpoint: string) { return this.request<T>(endpoint, { method: 'GET' }); }
  post<T>(endpoint: string, body?: any) { return this.request<T>(endpoint, { method: 'POST', body: JSON.stringify(body) }); }
  patch<T>(endpoint: string, body?: any) { return this.request<T>(endpoint, { method: 'PATCH', body: JSON.stringify(body) }); }
  delete<T>(endpoint: string) { return this.request<T>(endpoint, { method: 'DELETE' }); }
}

export const api = new ApiClient();
