import React, { useState, useEffect } from 'react';
import AuthService from '../services/AuthService';
import './Friends.css';

function Friends() {
  const [activeTab, setActiveTab] = useState('friends');
  const [friends, setFriends] = useState([]);
  const [requests, setRequests] = useState([]);
  const [pending, setPending] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchFriendsData();
  }, [activeTab]);

  const fetchFriendsData = async () => {
    setLoading(true);
    setError(null);
    const currentUser = AuthService.getCurrentUser();
    
    if (!currentUser || !currentUser.token) {
      setError("Nu sunteți autentificat. Vă rugăm să vă conectați.");
      setLoading(false);
      return;
    }

    try {
      let endpoint = '';
      switch (activeTab) {
        case 'friends':
          endpoint = 'http://localhost:8081/friend/mutual';
          break;
        case 'requests':
          endpoint = 'http://localhost:8081/friend/request';
          break;
        case 'pending':
          endpoint = 'http://localhost:8081/friend/pending';
          break;
        default:
          endpoint = 'http://localhost:8081/friend/mutual';
      }

      const response = await fetch(endpoint, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${currentUser.token}`,
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        const errorData = await response.text();
        throw new Error(errorData || `Eroare la încărcarea ${activeTab}`);
      }

      const data = await response.json();
      switch (activeTab) {
        case 'friends':
          setFriends(data);
          break;
        case 'requests':
          setRequests(data);
          break;
        case 'pending':
          setPending(data);
          break;
      }
    } catch (err) {
      console.error(`Error fetching ${activeTab}:`, err);
      setError(err.message || "A apărut o eroare la încărcarea datelor. Vă rugăm să încercați din nou.");
    } finally {
      setLoading(false);
    }
  };

  const handleAcceptRequest = async (userId) => {
    try {
      const currentUser = AuthService.getCurrentUser();
      const response = await fetch(`http://localhost:8081/friend/add/${userId}`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${currentUser.token}`,
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        const errorData = await response.text();
        throw new Error(errorData || 'Eroare la acceptarea cererii de prietenie');
      }

      // Refresh the data
      fetchFriendsData();
    } catch (err) {
      console.error('Error accepting friend request:', err);
      setError(err.message || "A apărut o eroare la acceptarea cererii de prietenie");
    }
  };

  const handleRejectRequest = async (userId) => {
    try {
      const currentUser = AuthService.getCurrentUser();
      const response = await fetch(`http://localhost:8081/friend/reject/${userId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${currentUser.token}`,
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        const errorData = await response.text();
        throw new Error(errorData || 'Eroare la respingerea cererii de prietenie');
      }

      // Refresh the data
      fetchFriendsData();
    } catch (err) {
      console.error('Error rejecting friend request:', err);
      setError(err.message || "A apărut o eroare la respingerea cererii de prietenie");
    }
  };

  const handleCancelRequest = async (userId) => {
    try {
      const currentUser = AuthService.getCurrentUser();
      const response = await fetch(`http://localhost:8081/friend/reject/${userId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${currentUser.token}`,
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        const errorData = await response.text();
        throw new Error(errorData || 'Eroare la anularea cererii de prietenie');
      }

      // Refresh the data
      fetchFriendsData();
    } catch (err) {
      console.error('Error canceling friend request:', err);
      setError(err.message || "A apărut o eroare la anularea cererii de prietenie");
    }
  };

  const renderUserCard = (user, type) => {
    return (
      <div key={user.id} className="user-card">
        <div className="user-info">
          <div className="user-avatar">
            {user.url_photo ? (
              <img src={user.url_photo} alt={user.username} />
            ) : (
              <div className="avatar-placeholder">
                <i className="fas fa-user"></i>
              </div>
            )}
          </div>
          <div className="user-details">
            <h3>{user.username}</h3>
            <p>{user.email}</p>
          </div>
        </div>
        <div className="user-actions">
          {type === 'requests' && (
            <>
              <button 
                className="accept-btn"
                onClick={() => handleAcceptRequest(user.id)}
              >
                Accept
              </button>
              <button 
                className="reject-btn"
                onClick={() => handleRejectRequest(user.id)}
              >
                Reject
              </button>
            </>
          )}
          {type === 'pending' && (
            <button 
              className="cancel-btn"
              onClick={() => handleCancelRequest(user.id)}
            >
              Cancel Request
            </button>
          )}
        </div>
      </div>
    );
  };

  if (loading) {
    return <div className="friends-container"><p>Loading...</p></div>;
  }

  if (error) {
    return <div className="friends-container"><p>Error: {error}</p></div>;
  }

  return (
    <div className="friends-container">
      <div className="friends-header">
        <h1>Friends</h1>
        <div className="friends-tabs">
          <button 
            className={activeTab === 'friends' ? 'active' : ''} 
            onClick={() => setActiveTab('friends')}
          >
            Friends
          </button>
          <button 
            className={activeTab === 'requests' ? 'active' : ''} 
            onClick={() => setActiveTab('requests')}
          >
            Requests
          </button>
          <button 
            className={activeTab === 'pending' ? 'active' : ''} 
            onClick={() => setActiveTab('pending')}
          >
            Pending
          </button>
        </div>
      </div>

      <div className="friends-content">
        {activeTab === 'friends' && (
          <div className="friends-list">
            {friends.length === 0 ? (
              <p>No friends yet</p>
            ) : (
              friends.map(user => renderUserCard(user, 'friends'))
            )}
          </div>
        )}

        {activeTab === 'requests' && (
          <div className="requests-list">
            {requests.length === 0 ? (
              <p>No friend requests</p>
            ) : (
              requests.map(user => renderUserCard(user, 'requests'))
            )}
          </div>
        )}

        {activeTab === 'pending' && (
          <div className="pending-list">
            {pending.length === 0 ? (
              <p>No pending requests</p>
            ) : (
              pending.map(user => renderUserCard(user, 'pending'))
            )}
          </div>
        )}
      </div>
    </div>
  );
}

export default Friends; 