import React, { useEffect, useState } from 'react';
import './CommentModal.css';

function CommentModal({ open, onClose, postId, userToken, onCommentAdded }) {
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [newComment, setNewComment] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [photo, setPhoto] = useState(null);
  const [photoPreview, setPhotoPreview] = useState(null);
  const [imageModal, setImageModal] = useState({ open: false, src: null });
  const [userVotes, setUserVotes] = useState(() => {
    const savedVotes = localStorage.getItem('commentVotes');
    return savedVotes ? JSON.parse(savedVotes) : {};
  });
  const [isAdmin, setIsAdmin] = useState(false);
  const [editComment, setEditComment] = useState({ show: false, comment: null, text: '' });
  const [avatarUrls, setAvatarUrls] = useState({});

  useEffect(() => {
    const user = JSON.parse(localStorage.getItem('user'));
    setIsAdmin(user?.user?.role === 'ADMIN');
  }, []);

  useEffect(() => {
    let intervalId;
    
    if (open && postId) {
      fetchComments();
      
      intervalId = setInterval(() => {
        fetchComments();
      }, 5000);
    }

    return () => {
      if (intervalId) {
        clearInterval(intervalId);
      }
    };
  }, [open, postId]);

  useEffect(() => {
    localStorage.setItem('commentVotes', JSON.stringify(userVotes));
  }, [userVotes]);

  const fetchAvatars = async (commentsData) => {
    const user = JSON.parse(localStorage.getItem('user'));
    if (!user?.token) return;

    const newAvatarUrls = {};
    await Promise.all(commentsData.map(async (comment) => {
      if (comment.user && comment.user.url_photo) {
        if (comment.user.url_photo.startsWith('http://') || comment.user.url_photo.startsWith('https://')) {
          newAvatarUrls[comment.id] = comment.user.url_photo;
        } else {
          try {
            const response = await fetch(`http://localhost:8081/user/photo/${comment.user.url_photo}`, {
              headers: {
                'Authorization': `Bearer ${user.token}`
              }
            });
            if (response.ok) {
              const blob = await response.blob();
              newAvatarUrls[comment.id] = URL.createObjectURL(blob);
            }
          } catch (err) {
            console.error('Error fetching avatar:', err);
            newAvatarUrls[comment.id] = null;
          }
        }
      }
    }));
    setAvatarUrls(newAvatarUrls);
  };

  const fetchComments = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(`http://localhost:8081/content/${postId}/comments`, {
        headers: { 'Authorization': `Bearer ${userToken}` }
      });
      if (!response.ok) throw new Error('Eroare la încărcarea comentariilor');
      const data = await response.json();
      
      const newUserVotes = { ...userVotes };
      data.forEach(comment => {
        const userVote = comment.votes.find(v => v.user.id === JSON.parse(localStorage.getItem('user'))?.user?.id);
        if (userVote) {
          newUserVotes[comment.id] = userVote.type;
        }
      });
      setUserVotes(newUserVotes);
      
      setComments(data.sort((a, b) => {
        if (b.nrVotes !== a.nrVotes) {
          return b.nrVotes - a.nrVotes;
        }
        return new Date(b.createdAt) - new Date(a.createdAt);
      }));

      // Fetch avatars for all comments
      await fetchAvatars(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleVote = async (commentId, voteType) => {
    try {
      const user = JSON.parse(localStorage.getItem('user'));
      if (!user?.token) {
        alert('You must be logged in to vote');
        return;
      }

      const comment = comments.find(c => c.id === commentId);
      if (!comment) return;

      const response = await fetch('http://localhost:8081/vote/add', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${user.token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          type: voteType,
          content: { id: commentId }
        })
      });

      if (response.ok) {
        let newVote = null;
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.indexOf("application/json") !== -1) {
          newVote = await response.json();
        }
        
        setComments(prev => prev.map(c => {
          if (c.id === commentId) {
            if (newVote === null) {
              const oldVote = c.votes.find(v => v.user.id === user.user.id);
              return {
                ...c,
                votes: c.votes.filter(v => v.id !== oldVote?.id),
                nrVotes: c.nrVotes - (userVotes[commentId] === 'UPVOTE' ? 1 : -1)
              };
            } else {
              return {
                ...c,
                votes: [...c.votes, newVote],
                nrVotes: c.nrVotes + (voteType === 'UPVOTE' ? 1 : -1)
              };
            }
          }
          return c;
        }));

        setUserVotes(prev => {
          const newVotes = { ...prev };
          if (newVote === null) {
            delete newVotes[commentId];
          } else {
            newVotes[commentId] = voteType;
          }
          return newVotes;
        });
      } else {
        const errorText = await response.text();
        throw new Error(`Failed to add vote: ${errorText}`);
      }
    } catch (error) {
      console.error('Error voting:', error);
      alert('Error voting: ' + error.message);
    }
  };

  const renderVoteButtons = (comment) => {
    const userVote = userVotes[comment.id];
    const voteCount = comment.nrVotes;
    const currentUser = JSON.parse(localStorage.getItem('user'));

    return (
      <div className="vote-buttons">
        <button
          className={`vote-button ${userVote === 'UPVOTE' ? 'active' : ''}`}
          onClick={() => handleVote(comment.id, 'UPVOTE')}
          disabled={comment.user?.id === currentUser?.user?.id || userVote === 'DOWNVOTE'}
        >
          <i className="fas fa-arrow-up"></i>
        </button>
        <span className="vote-count">{voteCount}</span>
        <button
          className={`vote-button ${userVote === 'DOWNVOTE' ? 'active' : ''}`}
          onClick={() => handleVote(comment.id, 'DOWNVOTE')}
          disabled={comment.user?.id === currentUser?.user?.id || userVote === 'UPVOTE'}
        >
          <i className="fas fa-arrow-down"></i>
        </button>
      </div>
    );
  };

  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!newComment.trim()) return;
    setSubmitting(true);
    setError(null);
    try {
      const formData = new FormData();
      const contentData = {
        content: {
          text: newComment,
          typeContent: false,
          status: 'active',
          parentContentId: postId,
          nrComments: 0,
          nrVotes: 0,
          votes: [],
          tags: []
        },
        tag: []
      };
      formData.append('content', new Blob([JSON.stringify(contentData)], { type: 'application/json' }));
      if (photo) {
        formData.append('photo', photo);
      } else {
        formData.append('photo', new Blob([''], { type: 'image/png' }));
      }
      const response = await fetch('http://localhost:8081/content', {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${userToken}` },
        body: formData
      });
      if (!response.ok) throw new Error('Eroare la adăugarea comentariului');
      setNewComment('');
      setPhoto(null);
      setPhotoPreview(null);
      fetchComments();
      if (onCommentAdded) onCommentAdded();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setPhoto(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setPhotoPreview(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleDeleteComment = async (commentId) => {
    if (!window.confirm('Are you sure you want to delete this comment?')) {
      return;
    }

    try {
      const response = await fetch(`http://localhost:8081/content/${commentId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${userToken}`
        }
      });

      if (response.ok) {
        // Remove the deleted comment from the state
        setComments(prev => prev.filter(comment => comment.id !== commentId));
      } else {
        const errorText = await response.text();
        throw new Error(`Failed to delete comment: ${errorText}`);
      }
    } catch (error) {
      console.error('Error deleting comment:', error);
      alert('Error deleting comment: ' + error.message);
    }
  };

  const handleEditComment = (comment) => {
    setEditComment({ show: true, comment, text: comment.text });
  };

  const handleEditCommentSubmit = async (e) => {
    e.preventDefault();
    if (!editComment.text.trim()) return;

    try {
      const payload = {
        id: editComment.comment.id,
        text: editComment.text,
        user: editComment.comment.user ? { id: editComment.comment.user.id } : undefined,
        parentContentId: editComment.comment.parentContentId || postId,
        status: editComment.comment.status || 'active',
        typeContent: false
      };
      const response = await fetch(`http://localhost:8081/content/${editComment.comment.id}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${userToken}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      });

      if (response.ok) {
        setComments(prev => prev.map(c => 
          c.id === editComment.comment.id 
            ? { ...c, text: editComment.text }
            : c
        ));
        setEditComment({ show: false, comment: null, text: '' });
      } else {
        const errorText = await response.text();
        throw new Error(`Failed to edit comment: ${errorText}`);
      }
    } catch (error) {
      console.error('Error editing comment:', error);
      alert('Error editing comment: ' + error.message);
    }
  };

  if (!open) return null;

  return (
    <div className="comment-modal-overlay" onClick={onClose}>
      <div className="comment-modal-content" onClick={e => e.stopPropagation()}>
        <div className="comment-modal-header">
          <h3>Comentarii</h3>
          <button className="close-button" onClick={onClose}>&times;</button>
        </div>
        <div className="comment-modal-body">
          {loading ? (
            <div className="comment-loading">Se încarcă...</div>
          ) : error ? (
            <div className="comment-error">{error}</div>
          ) : comments.length === 0 ? (
            <div className="comment-empty">Nu există comentarii.</div>
          ) : (
            <ul className="comment-list">
              {comments.map(comment => (
                <li key={comment.id} className="comment-item">
                  <div className="comment-header">
                    <div className="comment-user-info">
                      <div className="comment-user-avatar">
                        {avatarUrls[comment.id] ? (
                          <img src={avatarUrls[comment.id]} alt={comment.user?.username} />
                        ) : (
                          <div className="avatar-placeholder">
                            <i className="fas fa-user"></i>
                          </div>
                        )}
                      </div>
                      <div className="comment-user">{comment.user?.username || 'Anonim'}</div>
                    </div>
                    <div className="comment-actions-buttons">
                      {comment.user?.id === JSON.parse(localStorage.getItem('user'))?.user?.id && (
                        <>
                          <button
                            className="edit-comment-button"
                            onClick={() => handleEditComment(comment)}
                            title="Edit comment"
                          >
                            <i className="fas fa-edit"></i>
                          </button>
                          <button
                            className="delete-comment-button"
                            onClick={() => handleDeleteComment(comment.id)}
                            title="Delete comment"
                          >
                            <i className="fas fa-trash"></i>
                          </button>
                        </>
                      )}
                      {isAdmin && comment.user?.id !== JSON.parse(localStorage.getItem('user'))?.user?.id && (
                        <>
                          <button
                            className="edit-comment-button"
                            onClick={() => handleEditComment(comment)}
                            title="Edit comment"
                          >
                            <i className="fas fa-edit"></i>
                          </button>
                          <button
                            className="delete-comment-button"
                            onClick={() => handleDeleteComment(comment.id)}
                            title="Delete comment"
                          >
                            <i className="fas fa-trash"></i>
                          </button>
                        </>
                      )}
                    </div>
                  </div>
                  {editComment.show && editComment.comment.id === comment.id ? (
                    <form onSubmit={handleEditCommentSubmit} className="edit-comment-form">
                      <textarea
                        value={editComment.text}
                        onChange={(e) => setEditComment(prev => ({ ...prev, text: e.target.value }))}
                        className="edit-comment-input"
                      />
                      <div className="edit-comment-buttons">
                        <button type="submit" className="save-comment-button">Save</button>
                        <button 
                          type="button" 
                          className="cancel-comment-button"
                          onClick={() => setEditComment({ show: false, comment: null, text: '' })}
                        >
                          Cancel
                        </button>
                      </div>
                    </form>
                  ) : (
                    <div className="comment-text">{comment.text}</div>
                  )}
                  {comment.urlPhoto && (
                    <div className="comment-image-container">
                      <img
                        src={comment.urlPhoto}
                        alt="Comentariu"
                        className="comment-image"
                        onClick={() => setImageModal({ open: true, src: comment.urlPhoto })}
                      />
                    </div>
                  )}
                  <div className="comment-actions">
                    {renderVoteButtons(comment)}
                    <div className="comment-date">
                      {comment.createdAt ? new Date(comment.createdAt).toLocaleString('ro-RO', { 
                        day: '2-digit', 
                        month: '2-digit', 
                        year: 'numeric', 
                        hour: '2-digit', 
                        minute: '2-digit' 
                      }) : ''}
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
        <form className="comment-form" onSubmit={handleAddComment}>
          <label className="comment-photo-upload">
            <input
              type="file"
              accept="image/*"
              onChange={handleFileChange}
              disabled={submitting}
              style={{ display: 'none' }}
            />
            <span role="img" aria-label="Adaugă poză" style={{ cursor: 'pointer', fontSize: 18, color: photoPreview ? '#1877f2' : '#65676b' }}>📷</span>
          </label>
          <input
            type="text"
            placeholder="Scrie un comentariu..."
            value={newComment}
            onChange={e => setNewComment(e.target.value)}
            disabled={submitting}
            maxLength={300}
            className="comment-input"
          />
          <button type="submit" disabled={submitting || !newComment.trim()} className="send-comment-btn">
            {submitting ? 'Se trimite...' : 'Trimite'}
          </button>
          {photoPreview && (
            <div className="comment-photo-preview">
              <img src={photoPreview} alt="Preview" />
              <button
                type="button"
                className="remove-photo"
                onClick={() => {
                  setPhoto(null);
                  setPhotoPreview(null);
                }}
                tabIndex={-1}
              >
                <i className="fas fa-times"></i>
              </button>
            </div>
          )}
        </form>
      </div>
      {imageModal.open && (
        <div className="comment-image-modal-overlay" onClick={() => setImageModal({ open: false, src: null })}>
          <div className="comment-image-modal-content" onClick={e => e.stopPropagation()}>
            <img src={imageModal.src} alt="Preview mare" />
            <button className="close-button" onClick={() => setImageModal({ open: false, src: null })}>&times;</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default CommentModal; 