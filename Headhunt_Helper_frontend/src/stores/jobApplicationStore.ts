import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { JobApplication } from '../types/jobApplication';
import { jobApplicationApi } from '../services/api';

export const useJobApplicationStore = defineStore('jobApplication', () => {
  const applications = ref<JobApplication[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // Fetch all applications
  const fetchApplications = async () => {
    loading.value = true;
    error.value = null;
    try {
      applications.value = await jobApplicationApi.getAllApplications();
    } catch (e) {
      error.value = 'Failed to fetch applications';
      console.error(e);
    } finally {
      loading.value = false;
    }
  };

  // Create new application
  const createApplication = async (application: JobApplication) => {
    loading.value = true;
    error.value = null;
    try {
      const newApplication = await jobApplicationApi.createApplication(application);
      applications.value.push(newApplication);
      return newApplication;
    } catch (e) {
      error.value = 'Failed to create application';
      console.error(e);
      throw e;
    } finally {
      loading.value = false;
    }
  };

  // Update application
  const updateApplication = async (id: number, application: JobApplication) => {
    loading.value = true;
    error.value = null;
    try {
      const updatedApplication = await jobApplicationApi.updateApplication(id, application);
      const index = applications.value.findIndex(app => app.id === id);
      if (index !== -1) {
        applications.value[index] = updatedApplication;
      }
      return updatedApplication;
    } catch (e) {
      error.value = 'Failed to update application';
      console.error(e);
      throw e;
    } finally {
      loading.value = false;
    }
  };

  // Delete application
  const deleteApplication = async (id: number) => {
    loading.value = true;
    error.value = null;
    try {
      await jobApplicationApi.deleteApplication(id);
      applications.value = applications.value.filter(app => app.id !== id);
    } catch (e) {
      error.value = 'Failed to delete application';
      console.error(e);
      throw e;
    } finally {
      loading.value = false;
    }
  };

  return {
    applications,
    loading,
    error,
    fetchApplications,
    createApplication,
    updateApplication,
    deleteApplication,
  };
}); 