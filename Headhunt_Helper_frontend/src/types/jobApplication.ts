export interface JobApplication {
  id?: number;
  companyName: string;
  position: string;
  jobUrl: string;
  status: 'APPLIED' | 'INTERVIEWING' | 'OFFERED' | 'REJECTED' | 'ACCEPTED';
  appliedTime: string;
  appliedDate: string | null;
  notes: string;
  location: string;
  salary: string;
  contactPerson: string;
  contactEmail: string;
} 