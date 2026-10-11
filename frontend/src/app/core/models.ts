// Types partagés avec l'API Spring Boot (miroir des DTO Java)

export type Role = 'CANDIDATE' | 'RECRUITER' | 'ADMIN';
export type ContractType = 'CDI' | 'CDD' | 'FREELANCE' | 'STAGE' | 'ALTERNANCE';
export type OfferStatus = 'OPEN' | 'CLOSED';
export type ApplicationStatus = 'RECEIVED' | 'INTERVIEW' | 'ACCEPTED' | 'REJECTED';

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface PageRequest {
  page: number;
  size: number;
  sort?: string;
}

/** Erreur au format RFC 9457 renvoyée par le GlobalExceptionHandler. */
export interface ProblemDetail {
  title?: string;
  detail?: string;
  status?: number;
  errors?: Record<string, string>;
}

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: Role;
  companyId: number | null;
}

export interface AuthResponse {
  token: string;
  expiresAt: string;
  user: User;
}

export interface RegisterCandidateRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface RegisterRecruiterRequest extends RegisterCandidateRequest {
  companyName: string;
  companyCity: string | null;
}

export interface Skill {
  id: number;
  name: string;
}

export interface Company {
  id: number;
  name: string;
  description: string | null;
  website: string | null;
  city: string | null;
}

export interface CompanyRequest {
  name: string;
  description: string | null;
  website: string | null;
  city: string | null;
}

export interface OfferSummary {
  id: number;
  title: string;
  city: string;
  contractType: ContractType;
  salaryMin: number | null;
  salaryMax: number | null;
  remote: boolean;
  status: OfferStatus;
  companyId: number;
  companyName: string;
  skills: string[];
  createdAt: string;
}

export interface OfferDetail {
  id: number;
  title: string;
  description: string;
  city: string;
  contractType: ContractType;
  salaryMin: number | null;
  salaryMax: number | null;
  remote: boolean;
  status: OfferStatus;
  company: Company;
  skills: Skill[];
  createdAt: string;
  updatedAt: string;
}

export interface OfferRequest {
  title: string;
  description: string;
  city: string;
  contractType: ContractType;
  salaryMin: number | null;
  salaryMax: number | null;
  remote: boolean;
  skillIds: number[];
}

export interface OfferFilters {
  keyword?: string | null;
  city?: string | null;
  contractType?: ContractType | null;
  minSalary?: number | null;
  skillId?: number | null;
  remote?: boolean | null;
}

export interface RecruiterOffer {
  offer: OfferSummary;
  totalApplications: number;
  applicationsByStatus: Record<ApplicationStatus, number>;
}

export interface CandidateApplication {
  id: number;
  offerId: number;
  offerTitle: string;
  companyName: string;
  offerCity: string;
  contractType: ContractType;
  offerStatus: OfferStatus;
  status: ApplicationStatus;
  recruiterMessage: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ApplicationCandidate {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string | null;
  city: string | null;
  headline: string | null;
  skills: string[];
  hasCv: boolean;
}

export interface RecruiterApplication {
  id: number;
  offerId: number;
  offerTitle: string;
  status: ApplicationStatus;
  coverLetter: string | null;
  recruiterMessage: string | null;
  candidate: ApplicationCandidate;
  createdAt: string;
  updatedAt: string;
}

export interface CandidateProfile {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phone: string | null;
  city: string | null;
  headline: string | null;
  bio: string | null;
  skills: Skill[];
  cv: { fileName: string; uploadedAt: string } | null;
}

export interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
  phone: string | null;
  city: string | null;
  headline: string | null;
  bio: string | null;
  skillIds: number[];
}

export interface AdminStats {
  totalUsers: number;
  usersByRole: Record<Role, number>;
  totalCompanies: number;
  openOffers: number;
  closedOffers: number;
  totalApplications: number;
  applicationsByStatus: Record<ApplicationStatus, number>;
  applicationsPerMonth: { month: string; count: number }[];
  offersByContractType: Record<ContractType, number>;
  topCities: { city: string; count: number }[];
}

export interface AdminUser {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: Role;
  enabled: boolean;
  companyName: string | null;
  createdAt: string;
}
