// User Profile API utility functions
const UserAPI = {
    baseUrl: '/api/user',
    
    async request(url, options = {}) {
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        };
        
        try {
            const response = await fetch(url, config);
            const data = await response.json();
            
            if (!response.ok) {
                throw new Error(data.message || 'Request failed');
            }
            
            return data;
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    },
    
    async getProfile() {
        return this.request(`${this.baseUrl}/profile`);
    },
    
    async updateProfile(profileData) {
        return this.request(`${this.baseUrl}/profile`, {
            method: 'PUT',
            body: JSON.stringify(profileData)
        });
    }
};

// UI utility functions for user profile
const UserUI = {
    showAlert(message, type = 'success') {
        const alertContainer = document.getElementById('alertContainer');
        const alertId = 'alert_' + Date.now();
        
        const alertHtml = `
            <div id="${alertId}" class="alert alert-${type} alert-dismissible fade show" role="alert">
                <strong>${message}</strong>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        `;
        
        alertContainer.insertAdjacentHTML('beforeend', alertHtml);
        
        // Auto-dismiss after 5 seconds
        setTimeout(() => {
            const alert = document.getElementById(alertId);
            if (alert) {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            }
        }, 5000);
    },
    
    setLoading(elementId, isLoading) {
        const element = document.getElementById(elementId);
        const spinner = element.querySelector('.spinner-border');
        
        if (isLoading) {
            element.disabled = true;
            if (spinner) spinner.classList.remove('d-none');
        } else {
            element.disabled = false;
            if (spinner) spinner.classList.add('d-none');
        }
    },
    
    showConfirmDialog(title, text, confirmButtonText = 'Yes') {
        return Swal.fire({
            title: title,
            text: text,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: confirmButtonText,
            cancelButtonText: 'Cancel',
            confirmButtonColor: '#0d6efd',
            cancelButtonColor: '#6c757d'
        });
    }
};

// User Profile Application
class UserProfileApp {
    constructor() {
        this.user = null;
        this.init();
    }
    
    async init() {
        await this.loadProfile();
        this.bindEvents();
    }
    
    async loadProfile() {
        try {
            const response = await UserAPI.getProfile();
            this.user = response.data;
            this.updateProfileDisplay();
        } catch (error) {
            UserUI.showAlert('Error loading profile: ' + error.message, 'danger');
        }
    }
    
    updateProfileDisplay() {
        if (!this.user) return;
        
        // Update profile table
        document.getElementById('userId').textContent = this.user.id || '';
        document.getElementById('userFirstName').textContent = this.user.firstName || '';
        document.getElementById('userLastName').textContent = this.user.lastName || '';
        document.getElementById('userAge').textContent = this.user.age || '';
        document.getElementById('userEmailDisplay').textContent = this.user.email || '';
        
        // Update roles display
        const rolesElement = document.getElementById('userRolesDisplay');
        if (this.user.roles && this.user.roles.length > 0) {
            rolesElement.innerHTML = this.user.roles.map(role => `[${role.name}]`).join(' ');
        } else {
            rolesElement.textContent = 'No roles assigned';
        }
        
        // Update navbar
        const navbarEmail = document.getElementById('userEmail');
        const navbarRoles = document.getElementById('userRoles');
        if (navbarEmail) navbarEmail.textContent = this.user.email || '';
        if (navbarRoles && this.user.roles) {
            navbarRoles.innerHTML = this.user.roles.map(role => `[${role.name}]`).join(' ');
        }
    }
    
    bindEvents() {
        // Edit profile form
        const editProfileForm = document.getElementById('editProfileForm');
        if (editProfileForm) {
            editProfileForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleEditProfile();
            });
        }
        
        // Edit profile modal show event
        const editProfileModal = document.getElementById('editProfileModal');
        if (editProfileModal) {
            editProfileModal.addEventListener('show.bs.modal', () => {
                this.populateEditForm();
            });
        }
    }
    
    populateEditForm() {
        if (!this.user) return;
        
        document.getElementById('edit-firstName').value = this.user.firstName || '';
        document.getElementById('edit-lastName').value = this.user.lastName || '';
        document.getElementById('edit-age').value = this.user.age || '';
        document.getElementById('edit-email').value = this.user.email || '';
        
        // Clear password fields
        document.getElementById('edit-currentPassword').value = '';
        document.getElementById('edit-newPassword').value = '';
    }
    
    async handleEditProfile() {
        try {
            UserUI.setLoading('editProfileBtn', true);
            
            const formData = new FormData(document.getElementById('editProfileForm'));
            const profileData = {
                firstName: formData.get('firstName'),
                lastName: formData.get('lastName'),
                age: parseInt(formData.get('age'))
            };
            
            // Only include password fields if both are provided
            const currentPassword = formData.get('currentPassword');
            const newPassword = formData.get('newPassword');
            
            if (currentPassword && newPassword) {
                profileData.currentPassword = currentPassword;
                profileData.newPassword = newPassword;
            }
            
            const response = await UserAPI.updateProfile(profileData);
            
            UserUI.showAlert(response.message);
            
            // Update local user data and display
            this.user = response.data;
            this.updateProfileDisplay();
            
            // Hide modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('editProfileModal'));
            if (modal) modal.hide();
            
        } catch (error) {
            UserUI.showAlert('Error updating profile: ' + error.message, 'danger');
        } finally {
            UserUI.setLoading('editProfileBtn', false);
        }
    }
    
    // Refresh profile data
    async refresh() {
        await this.loadProfile();
    }
}

// Initialize application when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.userProfileApp = new UserProfileApp();
});