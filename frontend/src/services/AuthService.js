import axios from 'axios';

const API_URL = 'http://localhost:8080/auth/';

// Configure axios defaults
axios.defaults.withCredentials = true;
axios.defaults.headers.common['Content-Type'] = 'application/json';

class AuthService {
  async login(email, password) {
    try {
      console.log('Attempting login with:', { email });
      const response = await axios.post(API_URL + 'login', {
        email,
        password
      });
      
      console.log('Login response:', response.data);
      
      if (response.data.statusCode === 200 && response.data.token) {
        const userResponse = await fetch('http://localhost:8081/user/me', {
          headers: { 'Authorization': `Bearer ${response.data.token}` }
        });
        const userData = await userResponse.json();
        const userObj = {
          token: response.data.token,
          refreshToken: response.data.refreshToken,
          expirationTime: response.data.expirationTime,
          user: userData,
          message: response.data.message
        };
        localStorage.setItem('user', JSON.stringify(userObj));
        return userObj;

      } else {
        throw new Error(response.data.message || 'Login failed');
      }
    } catch (error) {
      console.error('Login error:', {
        message: error.message,
        response: error.response?.data,
        status: error.response?.status
      });
      
      // Handle specific error cases
      if (error.response?.data?.message === "User is banned!") {
        console.log('Caught banned user message');
        throw new Error('Your account has been banned. Please contact the administrator for more information.');
      } else if (error.response?.status === 401) {
        throw new Error('Invalid email or password');
      } else if (error.response?.status === 404) {
        throw new Error('Account not found');
      } else if (error.response?.status === 403) {
        console.log('Caught 403 status');
        throw new Error('Your account has been banned. Please contact the administrator for more information.');

      } else if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      } else {
        throw new Error('Login failed. Please try again.');
      }
    }
  }

  logout() {
    localStorage.removeItem('user');
  }

  async register(username, email, password, phoneNumber = "") {
    try {
      console.log('Sending register request:', { username, email, password, phoneNumber });

      const response = await axios.post(API_URL + 'register', {
        username,
        email,
        password,
        phoneNumber
      });
      console.log('Register response:', response.data);
      return response.data;
    } catch (error) {
      console.error('Register error:', {
          message: error.message,
        response: error.response?.data,
        status: error.response?.status
      });
      throw error;
    }
  }

  getCurrentUser() {
    const user = localStorage.getItem('user');
    if (user) {
      return JSON.parse(user);
  }
    return null;
  }

  isAuthenticated() {
    const user = this.getCurrentUser();
    return user !== null;
  }

  getCurrentUserRole() {
    const user = this.getCurrentUser();
    if (user && user.user && user.user.role) {
      return user.user.role;
    }
    return null;
  }

  async forgotPassword(email) {
    try {
      const response = await axios.post(API_URL + 'forgot', email, {
        headers: { 'Content-Type': 'text/plain' }
      });
      return response.status === 200;
    } catch (error) {
      if (error.response?.status === 400) {
        throw new Error('Please enter a valid email address.');
      } else {
        throw new Error('Failed to send reset email. Please try again.');
      }
    }
  }

  async resetPassword(email, newPassword, resetCode) {
    try {
      const response = await axios.post(API_URL + 'reset', {
        email,
        newPassword,
        resetCode
      });
      return response.status === 200;
    } catch (error) {
      if (error.response?.data) {
        throw new Error(error.response.data);
      }
      throw new Error('Failed to reset password. Please try again.');
    }
  }
}

export default new AuthService(); 