// API utility functions
const API = {
    baseUrl: '/api/admin',
    
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
    
    async getUsers() {
        return this.request(`${this.baseUrl}/users`);
    },
    
    async getUser(id) {
        return this.request(`${this.baseUrl}/users/${id}`);
    },
    
    async createUser(userData) {
        return this.request(`${this.baseUrl}/users`, {
            method: 'POST',
            body: JSON.stringify(userData)
        });
    },
    
    async updateUser(id, userData) {
        return this.request(`${this.baseUrl}/users/${id}`, {
            method: 'PUT',
            body: JSON.stringify(userData)
        });
    },
    
    async deleteUser(id) {
        return this.request(`${this.baseUrl}/users/${id}`, {
            method: 'DELETE'
        });
    },
    
    async getRoles() {
        return this.request(`${this.baseUrl}/roles`);
    }
};

// UI utility functions
const UI = {
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
    
    setTableLoading(isLoading) {
        const table = document.getElementById('usersTable');
        const loading = document.getElementById('tableLoading');
        
        if (isLoading) {
            table.classList.add('loading');
            loading.classList.remove('d-none');
        } else {
            table.classList.remove('loading');
            loading.classList.add('d-none');
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
            confirmButtonColor: '#dc3545',
            cancelButtonColor: '#6c757d'
        });
    }
};

// Application logic
class AdminApp {
    constructor() {
        this.users = [];
        this.roles = [];
        this.init();
    }
    
    async init() {
        await this.loadRoles();
        await this.loadUsers();
        this.bindEvents();
    }
    
    async loadUsers() {
        try {
            UI.setTableLoading(true);
            const response = await API.getUsers();
            this.users = response.data;
            this.renderUsersTable();
        } catch (error) {
            UI.showAlert('Error loading users: ' + error.message, 'danger');
        } finally {
            UI.setTableLoading(false);
        }
    }
    
    async loadRoles() {
        try {
            const response = await API.getRoles();
            this.roles = response.data;
            this.populateRoleSelects();
        } catch (error) {
            UI.showAlert('Error loading roles: ' + error.message, 'danger');
        }
    }
    
    populateRoleSelects() {
        const addRolesSelect = document.getElementById('add-roles');
        const editRolesSelect = document.getElementById('edit-roles');
        
        const optionsHtml = this.roles.map(role => 
            `<option value="${role.id}">${role.name}</option>`
        ).join('');
        
        if (addRolesSelect) addRolesSelect.innerHTML = optionsHtml;
        if (editRolesSelect) editRolesSelect.innerHTML = optionsHtml;
    }
    
    renderUsersTable() {
        const tbody = document.getElementById('usersTableBody');
        
        if (!tbody) {
            console.error('Users table body not found');
            return;
        }
        
        if (this.users.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="8" class="text-center">No users found</td>
                </tr>
            `;
            return;
        }
        
        const usersHtml = this.users.map(user => `
            <tr>
                <td>${user.id}</td>
                <td>${user.firstName || ''}</td>
                <td>${user.lastName || ''}</td>
                <td>${user.age || ''}</td>
                <td>${user.email || ''}</td>
                <td>${user.roles ? user.roles.map(role => role.name).join(', ') : ''}</td>
                <td>
                    <button type="button" 
                            class="btn btn-info text-white btn-sm edit-user-btn"
                            data-user-id="${user.id}">
                        <i class="bi bi-pencil"></i> Edit
                    </button>
                </td>
                <td>
                    <button type="button" 
                            class="btn btn-danger btn-sm delete-user-btn"
                            data-user-id="${user.id}">
                        <i class="bi bi-trash"></i> Delete
                    </button>
                </td>
            </tr>
        `).join('');
        
        tbody.innerHTML = usersHtml;
    }
    
    bindEvents() {
        // Add user form
        const addUserForm = document.getElementById('addUserForm');
        if (addUserForm) {
            addUserForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleAddUser();
            });
        }
        
        // Edit user form
        const editUserForm = document.getElementById('editUserForm');
        if (editUserForm) {
            editUserForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleEditUser();
            });
        }
        
        // Event delegation for edit and delete buttons
        const usersTableBody = document.getElementById('usersTableBody');
        if (usersTableBody) {
            usersTableBody.addEventListener('click', (e) => {
                if (e.target.closest('.edit-user-btn')) {
                    const userId = e.target.closest('.edit-user-btn').dataset.userId;
                    this.openEditModal(userId);
                } else if (e.target.closest('.delete-user-btn')) {
                    const userId = e.target.closest('.delete-user-btn').dataset.userId;
                    this.confirmDeleteUser(userId);
                }
            });
        }
    }
    
    async handleAddUser() {
        try {
            UI.setLoading('addUserBtn', true);
            
            const formData = new FormData(document.getElementById('addUserForm'));
            const userData = {
                firstName: formData.get('firstName'),
                lastName: formData.get('lastName'),
                age: parseInt(formData.get('age')),
                email: formData.get('email'),
                password: formData.get('password'),
                roleIds: formData.getAll('roleIds')
            };
            
            const response = await API.createUser(userData);
            
            UI.showAlert(response.message);
            document.getElementById('addUserForm').reset();
            const modal = bootstrap.Modal.getInstance(document.getElementById('addUserModal'));
            if (modal) modal.hide();
            await this.loadUsers();
            
        } catch (error) {
            UI.showAlert('Error creating user: ' + error.message, 'danger');
        } finally {
            UI.setLoading('addUserBtn', false);
        }
    }
    
    async openEditModal(userId) {
        try {
            const user = this.users.find(u => u.id == userId);
            if (!user) {
                UI.showAlert('User not found', 'danger');
                return;
            }
            
            // Populate form fields
            document.getElementById('edit-id').value = user.id;
            document.getElementById('edit-firstName').value = user.firstName || '';
            document.getElementById('edit-lastName').value = user.lastName || '';
            document.getElementById('edit-age').value = user.age || '';
            document.getElementById('edit-email').value = user.email || '';
            document.getElementById('edit-password').value = '';
            
            // Select user roles
            const editRolesSelect = document.getElementById('edit-roles');
            if (editRolesSelect && user.roles) {
                Array.from(editRolesSelect.options).forEach(option => {
                    option.selected = user.roles.some(role => role.id == option.value);
                });
            }
            
            // Show modal
            const modal = new bootstrap.Modal(document.getElementById('editUserModal'));
            modal.show();
            
        } catch (error) {
            UI.showAlert('Error opening edit modal: ' + error.message, 'danger');
        }
    }
    
    async handleEditUser() {
        try {
            UI.setLoading('editUserBtn', true);
            
            const formData = new FormData(document.getElementById('editUserForm'));
            const userId = formData.get('id');
            const userData = {
                firstName: formData.get('firstName'),
                lastName: formData.get('lastName'),
                age: parseInt(formData.get('age')),
                email: formData.get('email'),
                roleIds: formData.getAll('roleIds')
            };
            
            // Only include password if provided
            const password = formData.get('password');
            if (password && password.trim()) {
                userData.password = password;
            }
            
            const response = await API.updateUser(userId, userData);
            
            UI.showAlert(response.message);
            const modal = bootstrap.Modal.getInstance(document.getElementById('editUserModal'));
            if (modal) modal.hide();
            await this.loadUsers();
            
        } catch (error) {
            UI.showAlert('Error updating user: ' + error.message, 'danger');
        } finally {
            UI.setLoading('editUserBtn', false);
        }
    }
    
    async confirmDeleteUser(userId) {
        const result = await UI.showConfirmDialog(
            'Are you sure?',
            'This user will be permanently deleted!',
            'Yes, delete!'
        );
        
        if (result.isConfirmed) {
            await this.deleteUser(userId);
        }
    }
    
    async deleteUser(userId) {
        try {
            const response = await API.deleteUser(userId);
            UI.showAlert(response.message);
            await this.loadUsers();
        } catch (error) {
            UI.showAlert('Error deleting user: ' + error.message, 'danger');
        }
    }
    
    // Refresh data method
    async refresh() {
        await this.loadRoles();
        await this.loadUsers();
    }
}

// Initialize application when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.adminApp = new AdminApp();
});