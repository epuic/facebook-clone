import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import AuthService from '../services/AuthService';
import './Login.css';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    // Check if user is already logged in
    if (AuthService.isAuthenticated()) {
      navigate('/');
    }

    // Check for registration success message
    if (location.state?.message) {
      setError(location.state.message);
    }
  }, [navigate, location]);

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await AuthService.login(email, password);
      if (response) {
        navigate('/');
      }
    } catch (err) {
      console.log('Login error caught:', err.message);

      setError(err.message || 'An error occurred during login');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <h1 className="facebook-logo">facebook</h1>
        <form onSubmit={handleLogin}>
          <input
            type="email"
            placeholder="Email address"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            disabled={loading}
            required
          />
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={loading}
            required
          />
          {error && (
            <div className={`message ${error.includes('banned') ? 'banned-message' : error.includes('successful') ? 'success-message' : 'error-message'}`}>
              {error}
            </div>
          )}
          <button 
            type="submit" 
            className="login-button"
            disabled={loading}
          >
            {loading ? 'Logging in...' : 'Log In'}
          </button>
          <div className="forgot-link">
            <a href="/forgot">Forgot password?</a>
          </div>
          <div className="divider">
            <span>or</span>
          </div>
          <button
            type="button"
            className="create-account-button"
            onClick={() => navigate('/register')}
            disabled={loading}
          >
            Create New Account
          </button>
        </form>
      </div>
    </div>
  );
}

export default Login; 