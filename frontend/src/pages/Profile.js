import React, { useEffect, useState, useCallback } from 'react';
import AuthService from '../services/AuthService';
import './Profile.css';

function Profile() {
  const [userInfo, setUserInfo] = useState(null);
  const [imageBlobUrl, setImageBlobUrl] = useState(null);
  const [isImageLoading, setIsImageLoading] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [isUploading, setIsUploading] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchImage = useCallback(async (photoName, token) => {
    console.log("[fetchImage] Called with photoName:", photoName, "Token present:", !!token);
    if (!photoName) {
      setImageBlobUrl(null);
      console.log("[fetchImage] No photoName provided, imageBlobUrl set to null.");
      return;
    }
    setIsImageLoading(true);
          try {
      const response = await fetch(`http://localhost:8081/user/photo/${photoName}`, {
              headers: {
          'Authorization': `Bearer ${token}`
              }
            });
            if (response.ok) {
              const blob = await response.blob();
        const blobUrl = URL.createObjectURL(blob);
        setImageBlobUrl(blobUrl);
        console.log("[fetchImage] Image fetched successfully. Blob URL:", blobUrl);
            } else {
        console.error("[fetchImage] Failed to fetch profile image. Status:", response.status, "Text:", response.statusText);
              setImageBlobUrl(null);
            }
          } catch (err) {
      console.error('Error fetching profile image:', err);
            setImageBlobUrl(null);
    } finally {
      setIsImageLoading(false);
    }
  }, []); // Empty dependency array for useCallback as it doesn't depend on props/state outside its scope

  useEffect(() => {
    const fetchUserProfile = async () => {
      setLoading(true);
      setError(null);
      const currentUser = AuthService.getCurrentUser();
      if (!currentUser || !currentUser.token) {
        setError("No authentication token found. Please login.");
        setLoading(false);
        return;
      }
      try {
        const response = await fetch('http://localhost:8081/user/me', {
          headers: {
            'Authorization': `Bearer ${currentUser.token}`,
            'Content-Type': 'application/json'
          }
        });
        if (!response.ok) {
          const errorData = await response.text();
          throw new Error(`Failed to fetch user profile: ${response.status} ${errorData || response.statusText}`);
        }
        const data = await response.json();
        setUserInfo(data);
        if (data && data.url_photo && !(data.url_photo.startsWith('http://') || data.url_photo.startsWith('https://'))) {
          fetchImage(data.url_photo, currentUser.token);
        } else {
          setImageBlobUrl(null);
    }
      } catch (err) {
        console.error("Error fetching profile:", err);
        setError(err.message || "An error occurred while fetching user data.");
      } finally {
        setLoading(false);
      }
    };
    fetchUserProfile();
  }, [fetchImage]);

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file && file.type.startsWith('image/')) {
      setSelectedFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewUrl(reader.result);
      };
      reader.readAsDataURL(file);
      setError(null); // Clear previous errors like "no file selected"
    } else {
      setSelectedFile(null);
      setPreviewUrl(null);
      // Optionally set an error if the file is not an image
      // setError("Please select a valid image file.");

    }
  };

  const handleUpload = async () => {
    if (!selectedFile) {
      setError("Please select an image to upload.");
      return;
    }

    setIsUploading(true);
    setError(null);
    const formData = new FormData();
    formData.append('photo', selectedFile);

    const currentUser = AuthService.getCurrentUser();
    if (!currentUser || !currentUser.token) {
      setError("Authentication token not found. Please re-login.");
      setIsUploading(false);
      return;
    }

    try {
      const uploadResponse = await fetch('http://localhost:8081/user/photo', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${currentUser.token}`,
          // 'Content-Type': 'multipart/form-data' is set automatically by browser for FormData
        },
        body: formData
      });

      if (!uploadResponse.ok) {
        const errorText = await uploadResponse.text();
        throw new Error(`Upload failed: ${uploadResponse.status} ${errorText || uploadResponse.statusText}`);
      }

      // If upload is OK (200), backend has updated the user's url_photo.
      // Now, re-fetch user data to get the new url_photo.
      const refreshedUserResponse = await fetch('http://localhost:8081/user/me', {
            headers: {
          'Authorization': `Bearer ${currentUser.token}`
            }
          });

      if (!refreshedUserResponse.ok) {
        throw new Error('Failed to refresh user data after photo upload.');
      }

      const refreshedUserData = await refreshedUserResponse.json(); // This is the direct User object
      
      // Update userInfo state which will trigger a re-render
      setUserInfo(refreshedUserData);
      
      // Update localStorage with the new user details (including new url_photo)
      const updatedUserForStorage = {
        ...currentUser, // Retain token, etc.
        user: refreshedUserData // Update the nested user object
      };
      localStorage.setItem('user', JSON.stringify(updatedUserForStorage));

      // Fetch and display the new image using the new url_photo from refreshedUserData
      if (refreshedUserData.url_photo && !(refreshedUserData.url_photo.startsWith('http://') || refreshedUserData.url_photo.startsWith('https://'))) {
        fetchImage(refreshedUserData.url_photo, currentUser.token);
      } else {
        setImageBlobUrl(null);
      }

      setSelectedFile(null);
      setPreviewUrl(null);
      // console.log("Photo uploaded and user data refreshed successfully.");

    } catch (err) {
      console.error("Error during photo upload or data refresh:", err);
      setError(err.message || "An error occurred during the upload process.");
    } finally {
      setIsUploading(false);
    }
  };

  // Decide image source: direct URL or fetch from backend
  const getProfileImageSrc = () => {
    if (previewUrl) return previewUrl;
    if (isImageLoading || isUploading) return null;
    if (userInfo && userInfo.url_photo) {
      if (userInfo.url_photo.startsWith('http://') || userInfo.url_photo.startsWith('https://')) {
        return userInfo.url_photo;
      } else if (imageBlobUrl) {
        return imageBlobUrl;
      }
    }
    return null;
  };

  if (loading) {
    return <div className="profile-container"><p>Loading profile...</p></div>;
  }

  if (error) {
    return <div className="profile-container profile-error-banner"><p>Error: {error}</p></div>;
  }

  if (!userInfo) {
    return <div className="profile-container"><p>No user information available.</p></div>;
  }

  // Format createdAt date
  let memberSince = 'Not available';
  if (userInfo.created_at) {
    let dateString = userInfo.created_at;
    console.log("Raw created_at string from backend:", dateString); // Log for debugging

    // Remove trailing parenthesis if present
    if (typeof dateString === 'string' && dateString.endsWith(')')) {
      dateString = dateString.slice(0, -1);
      console.log("Cleaned created_at string for parsing:", dateString);
    }
    
    const dateObj = new Date(dateString);
    
    // Check if the date object is valid
    if (!isNaN(dateObj.getTime())) {
      memberSince = dateObj.toLocaleDateString('en-US', {
        day: 'numeric',
        month: 'long',
        year: 'numeric'
      });
    } else {
      console.error("Failed to parse created_at string:", dateString, "Resulting date object:", dateObj);
      memberSince = "Invalid Date Format"; // Display a more specific message in the UI
    }
  } else {
    console.log("userInfo.created_at is null or undefined.");

 }

  return (
    <div className="profile-container">
      {error && <div className="profile-error-banner"><p>{error}</p></div>} {/* Display errors here */}
      <div className="profile-header">
        <div className="profile-picture-section">
          <div className="profile-picture-container">
            {isUploading ? (
              <p>Uploading...</p>
            ) : previewUrl ? (
              <img src={previewUrl} alt="Preview" className="profile-picture" />
            ) : isImageLoading ? (
              <p>Loading image...</p>
            ) : getProfileImageSrc() ? (
              <img src={getProfileImageSrc()} alt="Profile" className="profile-picture" />
            ) : (
              <div className="profile-picture-placeholder">
                <i className="fas fa-user"></i>
              </div>
            )}
          </div>
          {/* Upload Controls */}
          <div className="upload-controls">
            <input
              type="file"
              id="photo-upload-input"
              accept="image/*"
              onChange={handleFileChange}
              style={{ display: 'none' }}
              disabled={isUploading}
            />
            <label htmlFor="photo-upload-input" className={`upload-button ${isUploading ? 'disabled' : ''}`}>
              <i className="fas fa-camera"></i> Choose Photo
            </label>
            {selectedFile && (
              <button onClick={handleUpload} disabled={isUploading || !previewUrl} className="save-photo-button">
                {isUploading ? 'Saving...' : 'Save Photo'}

              </button>
            )}
          </div>
        </div>
      </div>

      <div className="profile-content">
        <div className="profile-section">
          <h2>About</h2>
          <div className="info-item">
            <span className="info-label">Username:</span>
            <span>{userInfo.username || 'Not provided'}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Email:</span>
            <span>{userInfo.email || 'Not provided'}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Member since:</span>
            <span>{memberSince}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Score:</span>
            <span className="info-score">{userInfo.score !== undefined ? userInfo.score.toFixed(1) : 'N/A'}</span>
          </div>
          {/* Placeholder for other info if needed */}
          {/* 
          <div className="info-item">
            <span className="info-label">Role:</span>
            <span>{userInfo.role || 'Not provided'}</span>
          </div>
          */}
        </div>

        {/* We will add image upload and display here later */}
        {/* 
        <div className="profile-section">
          <h2>Profile Picture</h2>
          <p>(Image functionality will be added here)</p>
        </div>
        */}
      </div>
    </div>
  );
}

export default Profile; 