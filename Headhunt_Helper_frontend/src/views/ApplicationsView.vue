<template>
  <div class="applications-view">
    <div class="content">
      <div class="form-section">
        <h2>Add New Application</h2>
        <form @submit.prevent="submitForm" class="application-form">
          <div class="form-group">
            <label for="companyName">Company Name</label>
            <input
              id="companyName"
              v-model="form.companyName"
              type="text"
              required
            />
          </div>

          <div class="form-group">
            <label for="position">Position</label>
            <input
              id="position"
              v-model="form.position"
              type="text"
              required
            />
          </div>

          <div class="form-group">
            <label for="jobUrl">Job URL</label>
            <input
              id="jobUrl"
              v-model="form.jobUrl"
              type="url"
              required
            />
          </div>

          <div class="form-group">
            <label for="jobWebsite">Job Website</label>
            <input
              id="jobWebsite"
              v-model="form.jobWebsite"
              type="url"
            />
          </div>

          <div class="form-group">
            <label for="status">Status</label>
            <select id="status" v-model="form.status" required>
              <option value="APPLIED">Applied</option>
              <option value="INTERVIEWING">Interviewing</option>
              <option value="OFFERED">Offered</option>
              <option value="REJECTED">Rejected</option>
              <option value="ACCEPTED">Accepted</option>
            </select>
          </div>

          <div class="form-group">
            <label for="location">Location</label>
            <input
              id="location"
              v-model="form.location"
              type="text"
              required
            />
          </div>

          <div class="form-group">
            <label for="salary">Salary</label>
            <input
              id="salary"
              v-model="form.salary"
              type="text"
              required
            />
          </div>

          <div class="form-group">
            <label for="contactPerson">Contact Person</label>
            <input
              id="contactPerson"
              v-model="form.contactPerson"
              type="text"
              required
            />
          </div>

          <div class="form-group">
            <label for="contactEmail">Contact Email</label>
            <input
              id="contactEmail"
              v-model="form.contactEmail"
              type="email"
            />
          </div>

          <div class="form-group">
            <label for="notes">Notes</label>
            <textarea
              id="notes"
              v-model="form.notes"
              rows="3"
            ></textarea>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn-primary">
              {{ editingApplication ? 'Update' : 'Create' }}
            </button>
            <button
              v-if="editingApplication"
              type="button"
              class="btn-secondary"
              @click="cancelEdit"
            >
              Cancel
            </button>
          </div>
        </form>
      </div>

      <div class="applications-section">
        <h2>Your Applications</h2>
        <div v-if="loading" class="loading">
          Loading applications...
        </div>
        <div v-else-if="error" class="error">
          {{ error }}
        </div>
        <div v-else-if="applications.length === 0" class="no-applications">
          No applications found. Add your first application using the form above.
        </div>
        <div v-else class="applications-list">
          <div
            v-for="application in applications"
            :key="application.id"
            class="application-card"
          >
            <div class="application-header">
              <h3>{{ application.position }}</h3>
              <span :class="['status-badge', application.status.toLowerCase()]">
                {{ application.status }}
              </span>
            </div>
            <div class="application-details">
              <p><strong>Company:</strong> {{ application.companyName }}</p>
              <p><strong>Location:</strong> {{ application.location }}</p>
              <p><strong>Salary:</strong> {{ application.salary }}</p>
              <p><strong>Contact:</strong> {{ application.contactPerson }}</p>
              <p><strong>Email:</strong> {{ application.contactEmail }}</p>
              <p v-if="application.notes"><strong>Notes:</strong> {{ application.notes }}</p>
            </div>
            <div class="application-actions">
              <a
                v-if="application.jobUrl"
                :href="application.jobUrl"
                target="_blank"
                class="btn-link"
              >
                View Job
              </a>
              <a
                v-if="application.jobWebsite"
                :href="application.jobWebsite"
                target="_blank"
                class="btn-link"
              >
                Visit Website
              </a>
              <button
                class="btn-edit"
                @click="editApplication(application)"
              >
                Edit
              </button>
              <button
                class="btn-delete"
                @click="deleteApplication(application.id)"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useJobApplicationStore } from '../stores/jobApplicationStore'
import type { JobApplication } from '../types/jobApplication'

const store = useJobApplicationStore()
const applications = computed(() => store.applications)
const loading = computed(() => store.loading)
const error = computed(() => store.error)
const editingApplication = ref<JobApplication | null>(null)
const form = ref({
  companyName: '',
  position: '',
  jobUrl: '',
  jobWebsite: '',
  status: 'APPLIED',
  location: '',
  salary: '',
  contactPerson: '',
  contactEmail: '',
  notes: ''
})

onMounted(async () => {
  await store.fetchApplications()
})

const submitForm = async () => {
  try {
    if (editingApplication.value) {
      const updated = await store.updateApplication(editingApplication.value.id, form.value)
      if (updated) {
        await store.fetchApplications()
        editingApplication.value = null
        resetForm()
      }
    } else {
      const created = await store.createApplication(form.value)
      if (created) {
        await store.fetchApplications()
        resetForm()
      }
    }
  } catch (e) {
    console.error('Failed to submit form:', e)
  }
}

const editApplication = (application: JobApplication) => {
  editingApplication.value = application
  form.value = { ...application }
}

const deleteApplication = async (id: number) => {
  if (confirm('Are you sure you want to delete this application?')) {
    try {
      await store.deleteApplication(id)
    } catch (e) {
      console.error('Failed to delete application:', e)
    }
  }
}

const cancelEdit = () => {
  editingApplication.value = null
  resetForm()
}

const resetForm = () => {
  form.value = {
    companyName: '',
    position: '',
    jobUrl: '',
    jobWebsite: '',
    status: 'APPLIED',
    location: '',
    salary: '',
    contactPerson: '',
    contactEmail: '',
    notes: ''
  }
}
</script>

<style scoped>
.applications-view {
  padding: 2rem;
}

.content {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.loading,
.error,
.no-applications {
  text-align: center;
  padding: 2rem;
  background: #f5f5f5;
  border-radius: 8px;
  margin: 1rem 0;
}

.error {
  color: #dc3545;
  background: #f8d7da;
  border: 1px solid #f5c6cb;
}

.no-applications {
  color: #6c757d;
  background: #e9ecef;
  border: 1px solid #dee2e6;
}

.form-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.application-form {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  margin-bottom: 5px;
  font-weight: 500;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-actions {
  grid-column: 1 / -1;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.btn-primary {
  background: #4CAF50;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-secondary {
  background: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
}

.applications-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.applications-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.application-card {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #eee;
}

.application-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.application-header h3 {
  margin: 0;
  font-size: 18px;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.applied {
  background: #e3f2fd;
  color: #1976d2;
}

.status-badge.interviewing {
  background: #fff3e0;
  color: #f57c00;
}

.status-badge.offered {
  background: #e8f5e9;
  color: #388e3c;
}

.status-badge.rejected {
  background: #ffebee;
  color: #d32f2f;
}

.status-badge.accepted {
  background: #e8f5e9;
  color: #388e3c;
}

.application-details {
  margin-bottom: 15px;
}

.application-details p {
  margin: 5px 0;
  font-size: 14px;
}

.application-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.btn-link {
  color: #1976d2;
  text-decoration: none;
  font-size: 14px;
}

.btn-edit {
  background: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-delete {
  background: #ffebee;
  color: #d32f2f;
  border: 1px solid #ffcdd2;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}
</style> 