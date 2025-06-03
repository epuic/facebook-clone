import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthService from '../services/AuthService';
import UsersPanel from '../components/UsersPanel';
import CommentModal from '../components/CommentModal';

import './Home.css';
import './EditPost.css';

function Home() {
  const navigate = useNavigate();
  const [userData, setUserData] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editModal, setEditModal] = useState({ show: false, post: null });
  const [newPost, setNewPost] = useState({
    text: '',
    photo: null
  });
  const [editPost, setEditPost] = useState({
    text: '',
    photo: null
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [photoPreview, setPhotoPreview] = useState(null);
  const [editPhotoPreview, setEditPhotoPreview] = useState(null);
  const [posts, setPosts] = useState([]);
  const [avatarUrls, setAvatarUrls] = useState({});
  const [tagInput, setTagInput] = useState('');
  const [editTagInput, setEditTagInput] = useState('');
  const [tags, setTags] = useState([]);
  const [editTags, setEditTags] = useState([]);
  const [allTags, setAllTags] = useState([]);
  const [selectedTag, setSelectedTag] = useState(null);
  const [cursor, setCursor] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [friendIds, setFriendIds] = useState([]);
  const [commentModalOpen, setCommentModalOpen] = useState(false);
  const [activePostId, setActivePostId] = useState(null);
  const [userVotes, setUserVotes] = useState(() => {
    const savedVotes = localStorage.getItem('userVotes');
    return savedVotes ? JSON.parse(savedVotes) : {};
  });
  const [searchText, setSearchText] = useState('');
  const [showOnlyMyPosts, setShowOnlyMyPosts] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [allUsers, setAllUsers] = useState([]);
  const [isAdmin, setIsAdmin] = useState(false);

  useEffect(() => {
    const user = AuthService.getCurrentUser();
    if (user) {
      console.log('Current user data:', user);
      setUserData(user);
      fetchPosts(0, true);
    }
  }, []);

  useEffect(() => {
    const fetchTags = async () => {
      try {
        const user = AuthService.getCurrentUser();
        const response = await fetch('http://localhost:8081/tag', {
          headers: {
            'Authorization': `Bearer ${user?.token}`
          }
        });
        if (response.ok) {
          const data = await response.json();
          setAllTags(data);
        }
      } catch (error) {
        console.error('Error fetching tags:', error);
      }
    };
    fetchTags();
  }, []);

  useEffect(() => {
    // Fetch friend ids
    const fetchFriends = async () => {
      const user = AuthService.getCurrentUser();
      if (!user?.token) return;
      const response = await fetch('http://localhost:8081/friend/mutual', {
        headers: { 'Authorization': `Bearer ${user.token}` }
      });
      if (response.ok) {
        const friends = await response.json();
        setFriendIds(friends.map(f => f.id));
      }
    };
    fetchFriends();
  }, []);

  useEffect(() => {
    // Save votes to localStorage whenever they change
    localStorage.setItem('userVotes', JSON.stringify(userVotes));
  }, [userVotes]);

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const user = AuthService.getCurrentUser();
        const response = await fetch('http://localhost:8081/user', {
          headers: {
            'Authorization': `Bearer ${user?.token}`
          }
        });
        if (response.ok) {
          const data = await response.json();
          setAllUsers(data);
        }
      } catch (error) {
        console.error('Error fetching users:', error);
      }
    };
    fetchUsers();
  }, []);

  useEffect(() => {
    const user = JSON.parse(localStorage.getItem('user'));
    setIsAdmin(user?.user?.role === 'ADMIN');
  }, []);

  const fetchPosts = async (page = 0, initial = false) => {
    try {
      const user = AuthService.getCurrentUser();
      const response = await fetch(`http://localhost:8081/content/from/${page}`, {
        headers: {
          'Authorization': `Bearer ${user?.token}`
        }
      });
      if (response.ok) {
        const data = await response.json();
        console.log('Fetched posts:', data);
        
        // Update userVotes based on fetched posts
        const newUserVotes = { ...userVotes };
        data.forEach(post => {
          const userVote = post.votes.find(v => v.user.id === user?.user?.id);
          if (userVote) {
            newUserVotes[post.id] = userVote.type;
          }
        });
        setUserVotes(newUserVotes);

        if (initial) {
        setPosts(data);
          setCursor(1);
          setHasMore(data.length === 5);
        fetchAvatars(data);
        } else {
          setPosts(prev => [...prev, ...data]);
          setCursor(prev => prev + 1);
          setHasMore(data.length === 5);
          fetchAvatars([...posts, ...data]);
        }
      }
    } catch (error) {
      console.error('Error fetching posts:', error);
    } finally {
      setLoadingMore(false);
    }
  };

  const fetchAvatars = async (postsData) => {
    const user = AuthService.getCurrentUser();
    if (!user?.token) return;

    const newAvatarUrls = {};
    await Promise.all(postsData.map(async (post) => {
      if (post.user && post.user.url_photo) {
        if (post.user.url_photo.startsWith('http://') || post.user.url_photo.startsWith('https://')) {
          newAvatarUrls[post.id] = post.user.url_photo;
        } else {
        try {
          const response = await fetch(`http://localhost:8081/user/photo/${post.user.url_photo}`, {
            headers: {
                'Authorization': `Bearer ${user.token}`
            }
          });
          if (response.ok) {
            const blob = await response.blob();
            newAvatarUrls[post.id] = URL.createObjectURL(blob);
          }
        } catch (err) {
            console.error('Error fetching avatar:', err);
          newAvatarUrls[post.id] = null;
          }

        }
      }
    }));
    setAvatarUrls(newAvatarUrls);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNewPost(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setNewPost(prev => ({
        ...prev,
        photo: file
      }));
      // Create preview URL for the image
      const reader = new FileReader();
      reader.onloadend = () => {
        setPhotoPreview(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleTagInputChange = (e) => {
    setTagInput(e.target.value);
  };

  const handleTagKeyDown = (e) => {
    if ((e.key === 'Enter' || e.key === ',') && tagInput.trim() !== '') {
      e.preventDefault();
      addTag(tagInput.trim());
    }
  };

  const addTag = (tag) => {
    if (tag && !tags.includes(tag)) {
      setTags([...tags, tag]);
    }
    setTagInput('');
  };

  const removeTag = (tagToRemove) => {
    setTags(tags.filter(tag => tag !== tagToRemove));
  };
  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    
    const user = AuthService.getCurrentUser();
    if (!user?.token) {
      alert('You must be logged in to create a post');
      setIsSubmitting(false);
      return;
    }

    const formData = new FormData();
    const contentData = {
      content: {
        text: newPost.text,
        typeContent: true,
        status: "active",
        nrComments: 0,
        nrVotes: 0,
        votes: [],
        tags: []
      },
      tag: tags.map(tag => ({ name: tag }))
    };

    formData.append('content', new Blob([JSON.stringify(contentData)], {
      type: 'application/json'
    }));

    if (newPost.photo) {
      formData.append('photo', newPost.photo);
    } else {
      formData.append('photo', new Blob([''], { type: 'image/png' }));
    }

    try {
      const response = await fetch('http://localhost:8081/content', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${user.token}`
        },
        body: formData
      });

      if (response.ok) {
        setNewPost({ text: '', photo: null });
        setPhotoPreview(null);
        setShowModal(false);
        setTags([]);
        setTagInput('');
        fetchPosts(0, true);

      } else {
        const errorText = await response.text();
        console.error('Failed to create post:', errorText);
        alert('Failed to create post: ' + errorText);
      }
    } catch (error) {
      console.error('Error creating post:', error);
      alert('Error creating post: ' + error.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setNewPost({ text: '', photo: null });
    setPhotoPreview(null);
    setTags([]);
    setTagInput('');
  };

  const handleLoadMore = () => {
    setLoadingMore(true);
    fetchPosts(cursor, false);
  };

  const filteredPosts = posts.filter(post => {
    // Filter by tag
    const tagMatch = selectedTag ? 
      post.tags && post.tags.some(tag => tag.name === selectedTag) : true;
    
    // Filter by text search (description)
    const textMatch = searchText ? 
      post.text && post.text.toLowerCase().includes(searchText.toLowerCase()) : true;
    
    // Filter by user's own posts
    const myPostsMatch = showOnlyMyPosts ? 
      post.user && post.user.id === userData?.user?.id : true;
    
    // Filter by selected user
    const userMatch = selectedUser ? 
      post.user && post.user.id === selectedUser : true;
    
    // Filter by friends (existing logic)
    const friendMatch = post.user && 
      (friendIds.includes(post.user.id) || post.user.id === userData?.user?.id);

    return tagMatch && textMatch && myPostsMatch && userMatch && friendMatch;
  });

  console.log('Friend IDs:', friendIds);
  console.log('User ID:', userData?.user?.id);
  console.log('Filtered posts:', filteredPosts);

  const sortedPosts = [...filteredPosts].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

  const user = AuthService.getCurrentUser();

  const handleVote = async (postId, voteType) => {
    try {
      const user = AuthService.getCurrentUser();
      if (!user?.token) {
        alert('You must be logged in to vote');
        return;
      }

      const post = posts.find(p => p.id === postId);
      if (!post) return;

      // Add new vote
      console.log('Adding new vote:', { type: voteType, contentId: postId });
      const response = await fetch('http://localhost:8081/vote/add', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${user.token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          type: voteType,
          content: { id: postId }
        })
      });

      if (response.ok) {
        let newVote = null;
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.indexOf("application/json") !== -1) {
          newVote = await response.json();
        }
        
        // Update posts state
        setPosts(prev => prev.map(p => {
          if (p.id === postId) {
            if (newVote === null) {
              // Vote was removed
              const oldVote = p.votes.find(v => v.user.id === user.user.id);
              return {
                ...p,
                votes: p.votes.filter(v => v.id !== oldVote?.id),
                nrVotes: p.nrVotes - (userVotes[postId] === 'UPVOTE' ? 1 : -1)
              };
            } else {
              // New vote was added
              return {
                ...p,
                votes: [...p.votes, newVote],
                nrVotes: p.nrVotes + (voteType === 'UPVOTE' ? 1 : -1)
              };
            }
          }
          return p;
        }));

        // Update user votes state
        setUserVotes(prev => {
          const newVotes = { ...prev };
          if (newVote === null) {
            delete newVotes[postId];
          } else {
            newVotes[postId] = voteType;
          }
          return newVotes;
        });

        // Update user score
        try {
          const userResponse = await fetch('http://localhost:8081/user/me', {
            headers: {
              'Authorization': `Bearer ${user.token}`
            }
          });
          if (userResponse.ok) {
            const updatedUser = await userResponse.json();
            setUserData(prev => ({
              ...prev,
              user: updatedUser
            }));
          }
        } catch (error) {
          console.error('Error updating user score:', error);
        }
      } else {
        const errorText = await response.text();
        throw new Error(`Failed to add vote: ${errorText}`);
      }
    } catch (error) {
      console.error('Error voting:', error);
      alert('Error voting: ' + error.message);
    }
  };

  const renderVoteButtons = (post) => {
    const userVote = userVotes[post.id];
    const voteCount = post.nrVotes;

    return (
      <div className="vote-buttons">
        <button
          className={`vote-button ${userVote === 'UPVOTE' ? 'active' : ''}`}
          onClick={() => handleVote(post.id, 'UPVOTE')}
          disabled={post.user?.id === userData?.user?.id || userVote === 'DOWNVOTE'}
        >
          <i className="fas fa-arrow-up"></i>
        </button>
        <span className="vote-count">{voteCount}</span>
        <button
          className={`vote-button ${userVote === 'DOWNVOTE' ? 'active' : ''}`}
          onClick={() => handleVote(post.id, 'DOWNVOTE')}
          disabled={post.user?.id === userData?.user?.id || userVote === 'UPVOTE'}
        >
          <i className="fas fa-arrow-down"></i>
        </button>
      </div>
    );
  };

  const handleEditPost = (post) => {
    setEditModal({ show: true, post });
    setEditPost({
      text: post.text,
      photo: null
    });
    setEditTags(post.tags.map(tag => tag.name));
    setEditPhotoPreview(post.urlPhoto);
  };

  const handleEditInputChange = (e) => {
    const { name, value } = e.target;
    setEditPost(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleEditFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setEditPost(prev => ({
        ...prev,
        photo: file
      }));
      const reader = new FileReader();
      reader.onloadend = () => {
        setEditPhotoPreview(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleEditTagInputChange = (e) => {
    setEditTagInput(e.target.value);
  };

  const handleEditTagKeyDown = (e) => {
    if ((e.key === 'Enter' || e.key === ',') && editTagInput.trim() !== '') {
      e.preventDefault();
      addEditTag(editTagInput.trim());
    }
  };

  const addEditTag = (tag) => {
    if (tag && !editTags.includes(tag)) {
      setEditTags([...editTags, tag]);
    }
    setEditTagInput('');
  };

  const removeEditTag = (tagToRemove) => {
    setEditTags(editTags.filter(tag => tag !== tagToRemove));
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    
    const user = AuthService.getCurrentUser();
    if (!user?.token) {
      alert('You must be logged in to edit a post');
      setIsSubmitting(false);
      return;
    }

    try {
      const contentData = {
        id: editModal.post.id,
        text: editPost.text,
        typeContent: true,
        status: "active",
        tags: editTags.map(tagName => ({ name: tagName })),
        user: editModal.post.user,
        title: editModal.post.title || null,
        votes: editModal.post.votes,
        nrComments: editModal.post.nrComments,
        nrVotes: editModal.post.nrVotes,
        createdAt: editModal.post.createdAt,
        urlPhoto: editPost.photo ? null : editModal.post.urlPhoto
      };

      const response = await fetch(`http://localhost:8081/content/${editModal.post.id}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${user.token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(contentData)
      });

      if (response.ok) {
        setEditModal({ show: false, post: null });
        setEditPost({ text: '', photo: null });
        setEditPhotoPreview(null);
        setEditTags([]);
        setEditTagInput('');
        fetchPosts(0, true);
      } else {
        const errorText = await response.text();
        console.error('Failed to edit post:', errorText);
        alert('Failed to edit post: ' + errorText);
      }
    } catch (error) {
      console.error('Error editing post:', error);
      alert('Error editing post: ' + error.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCloseEditModal = () => {
    setEditModal({ show: false, post: null });
    setEditPost({ text: '', photo: null });
    setEditPhotoPreview(null);
    setEditTags([]);
    setEditTagInput('');
  };

  const handleDeletePost = async (postId) => {
    if (!window.confirm('Are you sure you want to delete this post?')) {
      return;
    }

    try {
      const response = await fetch(`http://localhost:8081/content/${postId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${user?.token}`
        }
      });

      if (response.ok) {
        // Remove the deleted post from the state
        setPosts(prev => prev.filter(post => post.id !== postId));
      } else {
        const errorText = await response.text();
        throw new Error(`Failed to delete post: ${errorText}`);
      }
    } catch (error) {
      console.error('Error deleting post:', error);
      alert('Error deleting post: ' + error.message);
    }
  };

  if (!userData) {
    return <div>Loading...</div>;
  }

  return (
    <div className="home-container">
      <div className="home-main-content">

      <div className="feed-container">
        <div className="create-post-button-container">
          <button className="create-post-button" onClick={() => setShowModal(true)}>
            <i className="fas fa-plus"></i>
            Create Post
          </button>
        </div>
        <div className="filter-container">
          <div className="search-section">
            <input
              type="text"
              placeholder="Search by description..."
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              className="search-input"
            />
            <button
              className={`filter-button ${showOnlyMyPosts ? 'active' : ''}`}
              onClick={() => setShowOnlyMyPosts(!showOnlyMyPosts)}
            >
              <i className="fas fa-user"></i> My Posts
            </button>
            <select
              value={selectedUser || ''}
              onChange={(e) => setSelectedUser(e.target.value ? Number(e.target.value) : null)}
              className="user-select"
            >
              <option value="">All Friends</option>
              {allUsers
                .filter(user => friendIds.includes(user.id) || user.id === userData?.user?.id)
                .map(user => (
                  <option key={user.id} value={user.id}>
                    {user.username}
                  </option>
                ))}
            </select>
          </div>
          <div className="tag-cloud">
            <button
              className={`tag-cloud-btn${selectedTag === null ? ' selected' : ''}`}
              onClick={() => setSelectedTag(null)}
            >
              All Tags
            </button>
            {allTags.map(tag => (
              <button
                key={tag.id}
                className={`tag-cloud-btn${selectedTag === tag.name ? ' selected' : ''}`}
                onClick={() => setSelectedTag(tag.name)}
              >
                {tag.name}
              </button>
            ))}
          </div>
        </div>

        <div className="posts-feed">
            {sortedPosts.length > 0 ? (
              <>
                {sortedPosts.map(post => (
            <div key={post.id} className="post-card">
              <div className="post-header">
                <div className="post-user-info">
                  <div className="user-avatar">
                    {avatarUrls[post.id] ? (
                      <img src={avatarUrls[post.id]} alt={post.user?.username} className="user-avatar-image" />
                    ) : (
                      <i className="fas fa-user"></i>
                    )}
                  </div>
                  <div className="user-details">
                    <span className="username">{post.user?.username || 'Anonymous'}</span>
                    <span className="post-time">
                            {post.createdAt ?
                              new Date(post.createdAt).toLocaleString('ro-RO', {
                                day: '2-digit', month: '2-digit', year: 'numeric',
                                hour: '2-digit', minute: '2-digit'
                              }) : ''}
                    </span>
                  </div>
                </div>
                {post.user?.id === userData?.user?.id && (
                  <div className="post-actions-buttons">
                    <button
                      className="edit-post-button"
                      onClick={() => handleEditPost(post)}
                    >
                      <i className="fas fa-edit"></i>
                    </button>
                    <button
                      className="delete-post-button"
                      onClick={() => handleDeletePost(post.id)}
                    >
                      <i className="fas fa-trash"></i>
                    </button>
                  </div>
                )}
                {isAdmin && post.user?.id !== userData?.user?.id && (
                  <div className="post-actions-buttons">
                    <button
                      className="edit-post-button"
                      onClick={() => handleEditPost(post)}
                    >
                      <i className="fas fa-edit"></i>
                    </button>
                    <button
                      className="delete-post-button"
                      onClick={() => handleDeletePost(post.id)}
                    >
                      <i className="fas fa-trash"></i>
                    </button>
                  </div>
                )}
              </div>
              <div className="post-content">
                      <p className="post-description">{post.text}</p>
                      {post.tags && post.tags.length > 0 && (
                        <div className="post-tag-list">
                          {post.tags.map(tag => (
                            <span key={tag.id || tag.name} className="post-tag-item">#{tag.name}</span>
                          ))}
                    </div>
                      )}
                {post.urlPhoto && (
                  <div className="post-image-container">
                    <img src={post.urlPhoto} alt="Post" className="post-image" />
                  </div>
                )}
                <div className="post-actions-row">
                  {renderVoteButtons(post)}
                  <button
                    className="post-comment-btn"
                    onClick={() => { setActivePostId(post.id); setCommentModalOpen(true); }}
                  >
                    <i className="fas fa-comment-alt"></i>
                    <span className="comment-count">{post.nrComments}</span>
                  </button>
                </div>
              </div>
            </div>
          ))}
                {hasMore && (
                  <button 
                    className="post-button" 
                    style={{margin: '24px auto 0 auto', display: 'block'}} 
                    onClick={handleLoadMore} 
                    disabled={loadingMore}
                  >
                    {loadingMore ? 'Loading...' : 'Load More'}
                  </button>
                )}
              </>
            ) : (
              <div style={{ textAlign: 'center', padding: '20px', color: '#65676b' }}>
                No posts to display
              </div>
            )}
          </div>
        </div>
        <UsersPanel />
        <CommentModal
          open={commentModalOpen}
          onClose={() => setCommentModalOpen(false)}
          postId={activePostId}
          userToken={user?.token}
          onCommentAdded={() => {
            setPosts(posts =>
              posts.map(post =>
                post.id === activePostId
                  ? { ...post, nrComments: post.nrComments + 1 }
                  : post
              )
            );
          }}
        />
      </div>

      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h2>Create New Post</h2>
              <button className="close-button" onClick={handleCloseModal}>
                <i className="fas fa-times"></i>
              </button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="post-input">
                <textarea
                  name="text"
                  placeholder="What's on your mind?"
                  value={newPost.text}
                  onChange={handleInputChange}
                  required
                />
                {photoPreview && (
                  <div className="photo-preview">
                    <img src={photoPreview} alt="Preview" />
                    <button 
                      type="button" 
                      className="remove-photo"
                      onClick={() => {
                        setPhotoPreview(null);
                        setNewPost(prev => ({ ...prev, photo: null }));
                      }}
                    >
                      <i className="fas fa-times"></i>
                    </button>
                  </div>
                )}
                <div className="post-actions">
                  <label className="photo-upload">
                    <i className="fas fa-image"></i>
                    <span>{photoPreview ? 'Change Photo' : 'Add Photo'}</span>
                    <input
                      type="file"
                      accept="image/*"
                      onChange={handleFileChange}
                      style={{ display: 'none' }}
                    />
                  </label>
                  <div className="tag-input-container">
                    <input
                      type="text"
                      placeholder="Add tag and press Enter or ,"
                      value={tagInput}
                      onChange={handleTagInputChange}
                      onKeyDown={handleTagKeyDown}
                      disabled={isSubmitting}
                    />
                    <button type="button" onClick={() => addTag(tagInput.trim())} disabled={!tagInput.trim() || isSubmitting}>
                      Add Tag
                    </button>
                  </div>
                  <div className="tag-list">
                    {tags.map(tag => (
                      <span key={tag} className="tag-item">
                        {tag}
                        <button type="button" className="remove-tag" onClick={() => removeTag(tag)}>&times;</button>
                      </span>
                    ))}
                  </div>
                  <button 
                    type="submit" 
                    className={`post-button ${isSubmitting ? 'submitting' : ''}`}
                    disabled={isSubmitting}
                  >
                    {isSubmitting ? (
                      <>
                        <i className="fas fa-spinner fa-spin"></i>
                        Posting...
                      </>
                    ) : (
                      'Post'
                    )}
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      )}
      {editModal.show && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h2>Edit Post</h2>
              <button className="close-button" onClick={handleCloseEditModal}>
                <i className="fas fa-times"></i>
              </button>
            </div>
            <form onSubmit={handleEditSubmit}>
              <div className="post-input">
                <textarea
                  name="text"
                  placeholder="What's on your mind?"
                  value={editPost.text}
                  onChange={handleEditInputChange}
                  required
                />
                {editPhotoPreview && (
                  <div className="photo-preview">
                    <img src={editPhotoPreview} alt="Preview" />
                  </div>
                )}
                <div className="post-actions">
                  <div className="tag-input-container">
                    <input
                      type="text"
                      placeholder="Add tag and press Enter or ,"
                      value={editTagInput}
                      onChange={handleEditTagInputChange}
                      onKeyDown={handleEditTagKeyDown}
                      disabled={isSubmitting}
                    />
                    <button type="button" onClick={() => addEditTag(editTagInput.trim())} disabled={!editTagInput.trim() || isSubmitting}>
                      Add Tag
                    </button>
                  </div>
                  <div className="tag-list">
                    {editTags.map(tag => (
                      <span key={tag} className="tag-item">
                        {tag}
                        <button type="button" className="remove-tag" onClick={() => removeEditTag(tag)}>&times;</button>
                      </span>
                    ))}
                  </div>
                  <button 
                    type="submit" 
                    className={`post-button ${isSubmitting ? 'submitting' : ''}`}
                    disabled={isSubmitting}
                  >
                    {isSubmitting ? (
                      <>
                        <i className="fas fa-spinner fa-spin"></i>
                        Saving...
                      </>
                    ) : (
                      'Save Changes'
                    )}
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Home; 