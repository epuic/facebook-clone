import React, { useEffect, useState } from 'react';
import AuthService from '../services/AuthService';
import { encryptId } from '../utils/aes';
import { useNavigate } from 'react-router-dom';
import './AdminPanel.css';

function AdminAuthPanel() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionLoading, setActionLoading] = useState({});
  const navigate = useNavigate();

  useEffect(() => {
    const role = AuthService.getCurrentUserRole();
    if (role !== 'ADMIN') {
      navigate('/');
      return;
    }
    fetchUsers();
    // eslint-disable-next-line
  }, []);

  const fetchUsers = async () => {
    setLoading(true);
    setError(null);
    try {
      const currentUser = AuthService.getCurrentUser();
      const response = await fetch('http://localhost:8080/user', {
        headers: {
          'Authorization': `Bearer ${currentUser?.token}`
        }
      });
      if (!response.ok) throw new Error('Eroare la încărcarea utilizatorilor');
      const data = await response.json();
      setUsers(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleBan = async (userId) => {
    setActionLoading(prev => ({ ...prev, [userId]: true }));
    setError(null);
    try {
      const response = await fetch('http://localhost:8080/user/ban', {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain' },
        body: userId.toString()
      });
      if (!response.ok) {
        const errMsg = await response.text();
        throw new Error('Eroare la banare: ' + errMsg);
      }
      fetchUsers();
    } catch (err) {
      setError(err.message);
    } finally {
      setActionLoading(prev => ({ ...prev, [userId]: false }));
    }
  };

  const handleUnban = async (userId) => {
    setActionLoading(prev => ({ ...prev, [userId]: true }));
    setError(null);
    try {
      const response = await fetch('http://localhost:8080/user/unban', {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain' },
        body: userId.toString()
      });
      if (!response.ok) {
        const errMsg = await response.text();
        throw new Error('Eroare la debanare: ' + errMsg);
      }
      fetchUsers();
    } catch (err) {
      setError(err.message);
    } finally {
      setActionLoading(prev => ({ ...prev, [userId]: false }));
    }
  };

  if (loading) return <div className="admin-panel-container"><p>Se încarcă utilizatorii...</p></div>;
  if (error) return <div className="admin-panel-container"><p className="error-message">Eroare: {error}</p></div>;

  return (
    <div className="admin-panel-container">
      <h2>Admin Auth Panel - User Management (Direct AUTH)</h2>
      <table className="admin-users-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Username</th>
            <th>Email</th>
            <th>Role</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {users.map(user => (
            <tr key={user.id} className={user.banned ? 'banned-row' : ''}>
              <td>{user.id}</td>
              <td>{user.username}</td>
              <td>{user.email}</td>
              <td>{user.role}</td>
              <td>{user.banned ? <span className="banned-status">BANNED</span> : 'Active'}</td>
              <td>
                {user.role !== 'ADMIN' && (
                  user.banned ? (
                    <button
                      className="unban-btn"
                      onClick={() => handleUnban(user.id)}
                      disabled={actionLoading[user.id]}
                    >
                      {actionLoading[user.id] ? 'Unbanning...' : 'Unban'}
                    </button>
                  ) : (
                    <button
                      className="ban-btn"
                      onClick={() => handleBan(user.id)}
                      disabled={actionLoading[user.id]}
                    >
                      {actionLoading[user.id] ? 'Banning...' : 'Ban'}
                    </button>
                  )
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default AdminAuthPanel; 