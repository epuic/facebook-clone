import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import AuthService from '../services/AuthService';
import './Register.css';

function ResetPassword() {
  const location = useLocation();
  const [form, setForm] = useState({ email: '', resetCode: '', newPassword: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    if (location.state?.email) {
      setForm(f => ({ ...f, email: location.state.email }));
    }
  }, [location.state]);

  const handleChange = e => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async e => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      await AuthService.resetPassword(form.email, form.newPassword, form.resetCode);
      setSuccess('Password reset successfully! You can now log in.');
    } catch (err) {
      setError(err.message || 'Failed to reset password.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="register-container">
      <div className="register-box">
        <h1 className="facebook-logo">facebook</h1>
        <h2 className="register-title">Reset Password</h2>
        <form onSubmit={handleSubmit}>
          <input
            type="email"
            name="email"
            placeholder="Email address"
            value={form.email}
            onChange={handleChange}
            required
            disabled={loading}
          />
          <input
            type="text"
            name="resetCode"
            placeholder="Reset code"
            value={form.resetCode}
            onChange={handleChange}
            required
            disabled={loading}
          />
          <input
            type="password"
            name="newPassword"
            placeholder="New password"
            value={form.newPassword}
            onChange={handleChange}
            required
            minLength={6}
            disabled={loading}
          />
          {error && <div className="error-message">{error}</div>}
          {success && <div className="success-message">{success}</div>}
          <button type="submit" className="register-button" disabled={loading}>
            {loading ? 'Resetting...' : 'Reset Password'}
          </button>
        </form>
        <div className="login-link">
          <a href="/login">Back to login</a>
        </div>
      </div>
    </div>
  );
}

export default ResetPassword; 