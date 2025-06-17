import axios from 'axios';
import type { JobApplication } from '../types/jobApplication';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const jobApplicationApi = {
  // Get all applications
  getAllApplications: async (): Promise<JobApplication[]> => {
    const response = await api.get('/applications');
    return response.data;
  },

  // Get a single application
  getApplication: async (id: number): Promise<JobApplication> => {
    const response = await api.get(`/applications/${id}`);
    return response.data;
  },

  // Create a new application
  createApplication: async (application: JobApplication): Promise<JobApplication> => {
    const response = await api.post('/applications', application);
    return response.data;
  },

  // Update an application
  updateApplication: async (id: number, application: JobApplication): Promise<JobApplication> => {
    const response = await api.put(`/applications/${id}`, application);
    return response.data;
  },

  // Delete an application
  deleteApplication: async (id: number): Promise<void> => {
    await api.delete(`/applications/${id}`);
  },
}; 