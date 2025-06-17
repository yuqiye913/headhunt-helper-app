<template>
  <div class="applications">
    <h1>Job Applications</h1>
    
    <!-- Error message -->
    <div v-if="store.error" class="error-message">
      {{ store.error }}
    </div>

    <!-- Loading state -->
    <div v-if="store.loading" class="loading">
      Loading...
    </div>

    <!-- Application Form -->
    <div class="application-form">
      <h2>{{ editingApplication ? 'Edit Application' : 'New Application' }}</h2>
      <form @submit.prevent="handleSubmit">
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
            required
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
            @click="resetForm"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>

    <!-- Applications List -->
    <div class="applications-list">
      <h2>Applications List</h2>
      <div v-if="store.applications.length === 0" class="no-applications">
        No applications found
      </div>
      <div v-else class="applications-grid">
        <div v-for="app in store.applications" :key="app.id" class="application-card">
          <h3>{{ app.companyName }}</h3>
          <p><strong>Position:</strong> {{ app.position }}</p>
          <p><strong>Location:</strong> {{ app.location }}</p>
          <p><strong>Salary:</strong> {{ app.salary }}</p>
          <p><strong>Status:</strong> {{ app.status }}</p>
          <p><strong>Applied:</strong> {{ new Date(app.appliedTime).toLocaleDateString() }}</p>
          <p><strong>Contact:</strong> {{ app.contactPerson }} ({{ app.contactEmail }})</p>
          <p v-if="app.notes"><strong>Notes:</strong> {{ app.notes }}</p>
          <div class="card-actions">
            <a :href="app.jobUrl" target="_blank" class="btn-primary">View Job</a>
            <button class="btn-edit" @click="editApplication(app)">Edit</button>
            <button class="btn-delete" @click="deleteApplication(app.id!)">Delete</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useJobApplicationStore } from '../stores/jobApplicationStore';
import type { JobApplication } from '../types/jobApplication';

const store = useJobApplicationStore();
const editingApplication = ref<JobApplication | null>(null);

const form = ref<JobApplication>({
  companyName: '',
  position: '',
  jobUrl: '',
  status: 'APPLIED',
  appliedTime: new Date().toISOString(),
  appliedDate: null,
  notes: '',
  location: '',
  salary: '',
  contactPerson: '',
  contactEmail: ''
});

const resetForm = () => {
  form.value = {
    companyName: '',
    position: '',
    jobUrl: '',
    status: 'APPLIED',
    appliedTime: new Date().toISOString(),
    appliedDate: null,
    notes: '',
    location: '',
    salary: '',
    contactPerson: '',
    contactEmail: ''
  };
  editingApplication.value = null;
};

const handleSubmit = async () => {
  try {
    if (editingApplication.value) {
      await store.updateApplication(editingApplication.value.id!, form.value);
    } else {
      await store.createApplication(form.value);
    }
    resetForm();
  } catch (error) {
    console.error('Failed to save application:', error);
  }
};

const editApplication = (application: JobApplication) => {
  editingApplication.value = application;
  form.value = { ...application };
};

const deleteApplication = async (id: number) => {
  if (confirm('Are you sure you want to delete this application?')) {
    try {
      await store.deleteApplication(id);
    } catch (error) {
      console.error('Failed to delete application:', error);
    }
  }
};

onMounted(() => {
  store.fetchApplications();
});
</script>

<style scoped>
.applications {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

h1 {
  margin-bottom: 2rem;
  color: var(--text-color);
}

.error-message {
  background-color: #fee;
  color: #c00;
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 1rem;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.application-form {
  background-color: #f8f9fa;
  padding: 2rem;
  border-radius: 8px;
  margin-bottom: 2rem;
}

.form-group {
  margin-bottom: 1rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
}

input,
select,
textarea {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.btn-primary,
.btn-secondary,
.btn-edit,
.btn-delete {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
}

.btn-primary {
  background-color: var(--primary-color);
  color: white;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-edit {
  background-color: #007bff;
  color: white;
}

.btn-delete {
  background-color: #dc3545;
  color: white;
}

.applications-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
}

.application-card {
  background-color: white;
  padding: 1rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.card-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
}

.no-applications {
  text-align: center;
  padding: 2rem;
  color: #666;
}
</style> 