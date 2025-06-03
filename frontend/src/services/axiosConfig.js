import axios from 'axios';
import AuthService from './AuthService';

// Add a request interceptor
axios.interceptors.request.use(
  (config) => {
    const user = AuthService.getCurrentUser();
    if (user && user.token) {
      config.headers.Authorization = `Bearer ${user.token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add a response interceptor
axios.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // If the error is 401 and we haven't tried to refresh the token yet
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const user = AuthService.getCurrentUser();
        if (user && user.refreshToken) {
          // Try to refresh the token
          const response = await AuthService.refreshToken(user.refreshToken);
          if (response.token) {
            // Update the token in localStorage
            const updatedUser = { ...user, token: response.token };
            localStorage.setItem('user', JSON.stringify(updatedUser));

            // Retry the original request with the new token
            originalRequest.headers.Authorization = `Bearer ${response.token}`;
            return axios(originalRequest);
          }
        }
      } catch (refreshError) {
        // If refresh token fails, logout the user
        AuthService.logout();
        window.location.href = '/login';
      }
    }

    return Promise.reject(error);
  }
); 